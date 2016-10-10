package com.icfolson.sling.slingmap.runtime.util;

import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public final class ClassUtil {

    public static Class<?> tryLoadClassForSerializedResource(final Resource resource) {
        final String className = resource.getValueMap().get(JcrProperties.RUNTIME_CLASS, String.class);
        final String bundleName = resource.getValueMap().get(JcrProperties.BUNDLE_NAME, String.class);
        if (className == null) {
            return null;
        }
        final Bundle thisBundle = FrameworkUtil.getBundle(ClassUtil.class);
        final Bundle containingBundle = getClassLoaderBundle(thisBundle, bundleName);
        return tryLoadClass(containingBundle, className);
    }

    public static void writeClassDataToResource(final Class<?> objectClass, final Resource resource) {
        ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
        properties.put(JcrProperties.RUNTIME_CLASS, objectClass.getName());
        final Bundle bundle = FrameworkUtil.getBundle(objectClass);
        if (bundle != null) {
            properties.put(JcrProperties.BUNDLE_NAME, bundle.getSymbolicName());
        }
    }

    private static Class<?> tryLoadClass(final Bundle bundle, final String className) {
        try {
            if (bundle == null) {
                return Class.forName(className);
            }
            return bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Bundle getClassLoaderBundle(final Bundle thisBundle, final String bundleName) {
        if (bundleName == null) {
            return null;
        }
        if (thisBundle != null && thisBundle.getBundleContext() != null) {
            final Bundle[] bundles = thisBundle.getBundleContext().getBundles();
            if (bundles != null) {
                for (final Bundle bundle : bundles) {
                    if (bundleName.equals(bundle.getSymbolicName())) {
                        return bundle;
                    }
                }
            }
        }
        return thisBundle;
    }

    private ClassUtil() { }

}
