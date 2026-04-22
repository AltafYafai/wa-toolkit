package com.wa.toolkit.xposed.downgrade;

import android.os.Build;

import com.wa.toolkit.xposed.core.FeatureLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import de.robv.android.xposed.XSharedPreferences;
import io.github.libxposed.api.XposedModule;

public class Patch {
    public static void handlePackage(XposedModule.PackageReadyParam param, XSharedPreferences prefs, XposedModule module) {
        if (!("android".equals(param.getPackageName())))
            return;

        try {
            ClassLoader classLoader = param.getClassLoader();
            switch (Build.VERSION.SDK_INT) {
                case 36:
                case 35:
                case 34:
                    Class<?> utils = classLoader.loadClass("com.android.server.pm.PackageManagerServiceUtils");
                    Method checkDowngrade = utils.getDeclaredMethod("checkDowngrade", 
                        classLoader.loadClass("com.android.server.pm.pkg.AndroidPackage"),
                        classLoader.loadClass("android.content.pm.PackageInfoLite"));
                    module.hook(checkDowngrade).intercept(chain -> {
                        Object pkgObj = chain.getArgs().get(0);
                        try {
                            Method getPackageName = pkgObj.getClass().getMethod("getPackageName");
                            String pkg = (String) getPackageName.invoke(pkgObj);
                            if (Objects.equals(pkg, FeatureLoader.PACKAGE_WPP) || Objects.equals(pkg, FeatureLoader.PACKAGE_BUSINESS))
                                return null;
                        } catch (Exception ignored) {}
                        return chain.proceed();
                    });
                    break;
                case 33:
                    Class<?> utils33 = classLoader.loadClass("com.android.server.pm.PackageManagerServiceUtils");
                    Method checkDowngrade33 = utils33.getDeclaredMethod("checkDowngrade",
                        classLoader.loadClass("com.android.server.pm.parsing.pkg.AndroidPackage"),
                        classLoader.loadClass("android.content.pm.PackageInfoLite"));
                    module.hook(checkDowngrade33).intercept(chain -> {
                        Object pkgObj = chain.getArgs().get(0);
                        try {
                            Method getPackageName = pkgObj.getClass().getMethod("getPackageName");
                            String pkg = (String) getPackageName.invoke(pkgObj);
                            if (Objects.equals(pkg, FeatureLoader.PACKAGE_WPP) || Objects.equals(pkg, FeatureLoader.PACKAGE_BUSINESS))
                                return null;
                        } catch (Exception ignored) {}
                        return chain.proceed();
                    });
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            // Log error
        }
    }
}
