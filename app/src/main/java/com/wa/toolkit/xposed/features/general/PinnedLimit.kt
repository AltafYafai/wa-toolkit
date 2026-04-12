package com.wa.toolkit.xposed.features.general

import android.annotation.SuppressLint
import com.wa.toolkit.xposed.core.Feature
import com.wa.toolkit.xposed.core.devkit.Unobfuscator
import com.wa.toolkit.xposed.utils.ReflectionUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Modifier
import java.util.*

class PinnedLimit(loader: ClassLoader, preferences: XSharedPreferences) : Feature(loader, preferences) {

    @SuppressLint("DiscouragedApi")
    @Throws(Throwable::class)
    override fun doHook() {
        val pinnedHashSetMethod = Unobfuscator.loadPinnedHashSetMethod(classLoader)
        val pinnedInChatMethod = Unobfuscator.loadPinnedInChatMethod(classLoader)

        // increase pinned limit in chat to 60
        XposedBridge.hookMethod(pinnedInChatMethod, XC_MethodReplacement.returnConstant(60))

        if (prefs.getBoolean("pinnedlimit", false)) {
            // Disable pinned called by Server to prevent it from clearing the pinned list
            val setPinnedLimitMethod = Unobfuscator.loadSetPinnedLimitMethod(classLoader)
            XposedBridge.hookMethod(setPinnedLimitMethod, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (ReflectionUtils.isCalledFromString("SyncResponseHandler")) {
                        param.result = null
                    }
                }
            })
        }

        // Fix bug in initialCapacity of LinkedHashSet
        XposedHelpers.findAndHookConstructor(LinkedHashSet::class.java, Int::class.javaPrimitiveType, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val arg = param.args[0] as Int
                if (arg < 0) {
                    param.args[0] = Math.abs(arg)
                }
            }
        })

        // Fix bug in initialCapacity of ArrayList
        XposedHelpers.findAndHookConstructor(ArrayList::class.java, Int::class.javaPrimitiveType, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val arg = param.args[0] as Int
                if (arg < 0) {
                    param.args[0] = Math.abs(arg)
                }
            }
        })

        // This creates a modified linkedhashMap to return 0 if the fixed list is less than 60.
        XposedBridge.hookMethod(pinnedHashSetMethod, object : XC_MethodHook() {
            @Suppress("UNCHECKED_CAST")
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val map = param.result as MutableMap<Any?, Any?>
                val thisObject = if (Modifier.isStatic(param.method.modifiers)) param.args[0] else param.thisObject

                val pinnedMod: PinnedLinkedHashMap<Any?>
                if (map !is PinnedLinkedHashMap<*>) {
                    pinnedMod = PinnedLinkedHashMap()
                    pinnedMod.putAll(map)
                    param.result = pinnedMod
                } else {
                    pinnedMod = map as PinnedLinkedHashMap<Any?>
                }
                
                pinnedMod.limit = if (prefs.getBoolean("pinnedlimit", false)) 60 else 3
                
                val keySet = map.keys
                val sets = ReflectionUtils.getFieldsByType(thisObject.javaClass, MutableSet::class.java)
                for (setField in sets) {
                    val set = setField.get(thisObject) as? MutableSet<*>
                    if (set == keySet) {
                        val newKeySet = pinnedMod.keys
                        newKeySet.setDisableInterator(false)
                        setField.set(thisObject, newKeySet)
                    }
                }
            }
        })

        val method = Unobfuscator.loadPinnedFilterMethod(classLoader)
        XposedBridge.hookMethod(method, object : XC_MethodHook() {
            @Suppress("UNCHECKED_CAST")
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.args[0] !is PinnedLinkedHashMap.PinnedKeySet<*>) {
                    return
                }
                val set = param.result as Set<Any?>
                val pinnedMod = PinnedLinkedHashMap<Any?>()
                pinnedMod.limit = if (prefs.getBoolean("pinnedlimit", false)) 60 else 3
                for (item in set) {
                    pinnedMod[item] = item
                }
                val newKeySet = pinnedMod.keys
                newKeySet.setDisableInterator(false)
                param.result = newKeySet
            }
        })
    }

    override fun getPluginName(): String {
        return "Pinned Limit"
    }

    private class PinnedLinkedHashMap<T> : LinkedHashMap<T, T>() {
        var limit: Int = 0

        override fun size(): Int {
            return if (super.size >= limit) {
                super.size
            } else {
                -limit
            }
        }

        override val keys: PinnedKeySet<T>
            get() = PinnedKeySet(this, super.keys)

        class PinnedKeySet<T>(private val pinnedLinkedHashMap: PinnedLinkedHashMap<T>, private val set: MutableSet<T>) : MutableSet<T> {

            companion object {
                private var disableInterator = true
            }

            override val size: Int
                get() = pinnedLinkedHashMap.size()

            override fun isEmpty(): Boolean = set.isEmpty()

            override fun contains(element: T): Boolean = set.contains(element)

            override fun containsAll(elements: Collection<T>): Boolean = set.containsAll(elements)

            override fun iterator(): MutableIterator<T> {
                if (disableInterator) {
                    if (pinnedLinkedHashMap.size() < pinnedLinkedHashMap.limit) {
                        return object : MutableIterator<T> {
                            override fun hasNext(): Boolean = false
                            override fun next(): T = throw NoSuchElementException()
                            override fun remove() = throw UnsupportedOperationException()
                        }
                    }
                }
                return set.iterator()
            }

            override fun add(element: T): Boolean {
                val hadKey = pinnedLinkedHashMap.containsKey(element)
                pinnedLinkedHashMap[element] = element
                return !hadKey
            }

            override fun addAll(elements: Collection<T>): Boolean {
                var changed = false
                for (item in elements) {
                    changed = changed or add(item)
                }
                return changed
            }

            override fun clear() = set.clear()

            override fun remove(element: T): Boolean {
                val hadKey = pinnedLinkedHashMap.containsKey(element)
                if (hadKey) {
                    pinnedLinkedHashMap.remove(element)
                }
                return hadKey
            }

            override fun removeAll(elements: Collection<T>): Boolean = set.removeAll(elements)

            override fun retainAll(elements: Collection<T>): Boolean = set.retainAll(elements)

            fun setDisableInterator(b: Boolean) {
                disableInterator = b
            }
        }
    }
}
