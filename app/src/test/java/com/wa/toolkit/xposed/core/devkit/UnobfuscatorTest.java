package com.wa.toolkit.xposed.core.devkit;

import org.junit.Before;
import org.junit.Test;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.result.MethodDataList;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnobfuscatorTest {

    private DexKitBridge mockDexKit;

    @Before
    public void setUp() {
        mockDexKit = mock(DexKitBridge.class);
        Unobfuscator.setDexKitBridge(mockDexKit);
    }

    @Test
    public void testFindFirstMethodUsingStrings_EmptyResult() throws Exception {
        // Return an empty list when findMethod is called
        when(mockDexKit.findMethod(any(FindMethod.class))).thenReturn(new MethodDataList());

        Method result = Unobfuscator.findFirstMethodUsingStrings(
                getClass().getClassLoader(),
                StringMatchType.Contains,
                "someString"
        );

        assertNull(result);
        verify(mockDexKit).findMethod(any(FindMethod.class));
    }

    @Test
    public void testGetMethodDescriptor() throws Exception {
        Method method = String.class.getMethod("substring", int.class, int.class);
        String descriptor = Unobfuscator.getMethodDescriptor(method);
        assertEquals("java.lang.String->substring(int,int)", descriptor);
    }

    @Test
    public void testGetFieldDescriptor() throws Exception {
        java.lang.reflect.Field field = String.class.getDeclaredField("value");
        String descriptor = Unobfuscator.getFieldDescriptor(field);
        // Note: in Java 21 it might be byte[]
        assertTrue(descriptor.startsWith("java.lang.String->value:"));
    }
}
