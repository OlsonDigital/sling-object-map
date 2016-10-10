package com.icfolson.sling.slingmap.runtime.generator;

import com.icfolson.sling.slingmap.api.basictype.BasicTypeConverter;
import com.icfolson.sling.slingmap.api.basictype.BasicTypeReader;
import com.icfolson.sling.slingmap.api.basictype.BasicTypeRegistry;
import com.icfolson.sling.slingmap.api.basictype.BasicTypeWriter;
import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.domain.ObjectReader;
import com.icfolson.sling.slingmap.api.domain.ObjectWriter;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.generator.MappingGenerator;
import com.icfolson.sling.slingmap.runtime.domain.CompositeObjectMerger;
import com.icfolson.sling.slingmap.runtime.domain.CompositeObjectReader;
import com.icfolson.sling.slingmap.runtime.domain.CompositeObjectWriter;
import com.icfolson.sling.slingmap.runtime.domain.ItemMerger;
import com.icfolson.sling.slingmap.runtime.domain.ItemReader;
import com.icfolson.sling.slingmap.runtime.domain.ItemWriter;
import com.icfolson.sling.slingmap.runtime.registry.types.basic.BasicValueObjectMerger;
import com.icfolson.sling.slingmap.runtime.registry.types.basic.BasicValueObjectReader;
import com.icfolson.sling.slingmap.runtime.registry.types.basic.BasicValueObjectWriter;
import com.icfolson.sling.slingmap.runtime.util.MappedResourceUtil;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A {@link MappingGenerator} that uses reflection to generate object read/write functions.  The functions are built by
 * breaking the class into a series of items (properties and/or sub-nodes), each with a corresponding read/write
 * function. The generated Composite functions can be customized after generation, if needed.
 *
 * Whether a given sub-component is serialized as a property or a child node depends on whether that type is registered
 * as a "primitive".  See {@link BasicTypeRegistry}.
 */
public class ReflectionMappingGenerator implements MappingGenerator {

    private final BasicTypeRegistry primitiveRegistry;

    public ReflectionMappingGenerator(final BasicTypeRegistry primitiveRegistry) {
        this.primitiveRegistry = primitiveRegistry;
    }

    @Override
    public ObjectReader generateObjectReader(final Class<?> inputClass) {
        if (primitiveRegistry.isBasicType(inputClass)) {
            return new BasicValueObjectReader(primitiveRegistry.getBasicConverter(inputClass));
        }
        CompositeObjectReader out = new CompositeObjectReader();
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(inputClass);
            for (final PropertyDescriptor property: beanInfo.getPropertyDescriptors()) {
                final String propertyName = property.getName();
                final Method setter = property.getWriteMethod();
                final Method getter = property.getReadMethod();
                if (setter != null && getter != null) {
                    final Class<?> type = property.getPropertyType();
                    final boolean isPrimitive = primitiveRegistry.isBasicType(type);
                    if (isPrimitive) {
                        final BasicTypeConverter converter = primitiveRegistry.getBasicConverter(type);
                        out.setItemReader(propertyName, new ReflectionPropertyReader(propertyName, setter, converter));
                    } else {
                        out.setItemReader(propertyName, new ReflectionResourceReader(propertyName, setter));
                    }
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public ObjectWriter generateObjectWriter(final Class<?> inputClass) {
        if (primitiveRegistry.isBasicType(inputClass)) {
            return new BasicValueObjectWriter(primitiveRegistry.getBasicConverter(inputClass));
        }
        CompositeObjectWriter out = new CompositeObjectWriter();
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(inputClass);
            for (final PropertyDescriptor property: beanInfo.getPropertyDescriptors()) {
                final String propertyName = property.getName();
                final Method getter = property.getReadMethod();
                final Method setter = property.getWriteMethod();
                if (getter != null && setter != null) {
                    final Class<?> type = property.getPropertyType();
                    final boolean isPrimitive = primitiveRegistry.isBasicType(type);
                    if (isPrimitive) {
                        final BasicTypeConverter converter = primitiveRegistry.getBasicConverter(type);
                        out.setItemWriter(propertyName, new ReflectionPropertyWriter(propertyName, getter, converter));
                    } else {
                        out.setItemWriter(propertyName, new ReflectionResourceWriter(propertyName, getter));
                    }
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public ObjectMerger generateObjectMerger(final Class<?> inputClass) {
        if (primitiveRegistry.isBasicType(inputClass)) {
            return new BasicValueObjectMerger(primitiveRegistry.getBasicConverter(inputClass));
        }
        CompositeObjectMerger out = new CompositeObjectMerger();
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(inputClass);
            for (final PropertyDescriptor property: beanInfo.getPropertyDescriptors()) {
                final String propertyName = property.getName();
                final Method getter = property.getReadMethod();
                final Method setter = property.getWriteMethod();
                if (getter != null && setter != null) {
                    final Class<?> type = property.getPropertyType();
                    final boolean isPrimitive = primitiveRegistry.isBasicType(type);
                    if (isPrimitive) {
                        final BasicTypeConverter converter = primitiveRegistry.getBasicConverter(type);
                        out.setItemMerger(propertyName, new ReflectionPropertyMerger(propertyName, getter, converter));
                    } else {
                        out.setItemMerger(propertyName, new ReflectionResourceMerger(propertyName, getter));
                    }
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return out;
    }

    private static class ReflectionPropertyReader implements ItemReader {

        private final String propertyName;
        private final Method setter;
        private final BasicTypeReader converter;

        private ReflectionPropertyReader(final String propertyName, final Method setter, BasicTypeReader converter) {
            this.propertyName = propertyName;
            this.setter = setter;
            this.converter = converter;
        }

        @Override
        public void read(final Resource resource, final Object object, final ReadContext readContext)
            throws MappingException {

            try {
                Object value = resource.getValueMap().get(propertyName);
                if (converter != null) {
                    value = converter.resourceToObjectPropertyValue(value);
                }
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MappingException(e);
            }
        }
    }

    private static class ReflectionResourceReader implements ItemReader {

        private final String childName;
        private final Method setter;

        private ReflectionResourceReader(final String childName, final Method setter) {
            this.childName = childName;
            this.setter = setter;
        }

        @Override
        public void read(final Resource resource, final Object object, final ReadContext readContext)
            throws MappingException {

            final Resource child = MappedResourceUtil.getPotentiallyNonExistingChild(resource, childName);
            readContext.recursiveRead(child, value -> {
                try {
                    setter.invoke(object, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new MappingException(e);
                }
            });
        }
    }

    private static class ReflectionPropertyWriter implements ItemWriter {

        private final String propertyName;
        private final Method getter;
        private final BasicTypeWriter converter;

        private ReflectionPropertyWriter(final String propertyName, final Method getter, BasicTypeWriter converter) {
            this.propertyName = propertyName;
            this.getter = getter;
            this.converter = converter;
        }

        @Override
        public void write(final Object object, final Resource resource, final WriteContext writeContext)
            throws MappingException {

            try {
                Object value = getter.invoke(object);
                final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
                if (converter != null) {
                    value = converter.objectToResourcePropertyValue(value);
                }
                if (value != null) {
                    valueMap.put(propertyName, value);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MappingException(e);
            }
        }
    }

    private static class ReflectionResourceWriter implements ItemWriter {

        private final String childName;
        private final Method getter;

        private ReflectionResourceWriter(final String childName, final Method getter) {
            this.childName = childName;
            this.getter = getter;
        }

        @Override
        public void write(final Object object, final Resource resource, final WriteContext writeContext)
            throws MappingException {

            try {
                final Object value = getter.invoke(object);
                if (value != null) {
                    final Resource child = MappedResourceUtil.getPotentiallyNonExistingChild(resource, childName);
                    writeContext.queueWrite(value, child);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MappingException(e);
            }
        }
    }

    private static class ReflectionPropertyMerger implements ItemMerger {

        private final String propertyName;
        private final Method getter;
        private final BasicTypeWriter converter;

        private ReflectionPropertyMerger(final String propertyName, final Method getter, BasicTypeWriter converter) {
            this.propertyName = propertyName;
            this.getter = getter;
            this.converter = converter;
        }

        @Override
        public void merge(final Object object, final Resource resource, final MergeContext context)
            throws MappingException {

            try {
                Object updated = getter.invoke(object);
                if (converter != null) {
                    updated = converter.objectToResourcePropertyValue(updated);
                }
                Object old = resource.getValueMap().get(propertyName);
                ChangeType changeType = ChangeType.fromVersions(old, updated);
                if (changeType != null) {
                    final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
                    if (changeType == ChangeType.DELETE) {
                        valueMap.remove(propertyName);
                    } else {
                        valueMap.put(propertyName, updated);
                    }
                    context.changeWritten(resource, propertyName, changeType);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MappingException(e);
            }
        }
    }

    private static class ReflectionResourceMerger implements ItemMerger {

        private final String childName;
        private final Method getter;

        private ReflectionResourceMerger(final String childName, final Method getter) {
            this.childName = childName;
            this.getter = getter;
        }

        @Override
        public void merge(final Object instance, final Resource resource, final MergeContext context)
            throws MappingException {

            try {
                final Object childObject = getter.invoke(instance);
                final Resource childResource = MappedResourceUtil.getPotentiallyNonExistingChild(resource, childName);
                context.queueMerge(childObject, childResource);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MappingException(e);
            }
        }
    }

}
