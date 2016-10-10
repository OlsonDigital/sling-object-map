package com.icfolson.sling.slingmap.runtime.generator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.domain.ObjectReader;
import com.icfolson.sling.slingmap.api.domain.ObjectWriter;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.generator.MappingGenerator;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.runtime.domain.CompositeObjectMerger;
import com.icfolson.sling.slingmap.runtime.domain.CompositeObjectWriter;
import com.icfolson.sling.slingmap.runtime.domain.ItemMerger;
import com.icfolson.sling.slingmap.runtime.domain.ItemWriter;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Decorates a wrapped {@link MappingGenerator}, adding additional JCR-specific mapping details.  Generated write
 * functions will also add a "jcr:primaryType" property.  This property will default to "nt:unstructured", but can be
 * controlled by registering a node type to the serialization class.
 */
public class JcrMappingGeneratorDecorator implements MappingGenerator {

    private static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
    private static final String DEFAULT_PRIMARY_TYPE = "nt:unstructured";
    private static final String JCR_MIXIN_TYPES = "jcr:mixinTypes";

    private final MappingGenerator wrapped;

    private final Map<Class<?>, String> primaryTypeMap = new HashMap<>();
    private final SetMultimap<Class<?>, String> mixinTypeMap = HashMultimap.create();

    public JcrMappingGeneratorDecorator(final MappingGenerator wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Sets the primary node type to be used when writing objects of the given class to the JCR.  Only one node type
     * can be set per class.
     * @param inputClass
     * @param nodeType
     */
    public void registerPrimaryNodeTypeForClass(final Class<?> inputClass, final String nodeType) {
        primaryTypeMap.put(inputClass, nodeType);
    }

    /**
     * Unsets the primary type for the given class.  The default value ("nt:unstructured") will be used.
     * @param inputClass
     * @param nodeType
     */
    public void unregisterPrimaryNodeTypeForClass(final Class<?> inputClass, final String nodeType) {
        primaryTypeMap.remove(inputClass);
    }

    /**
     * Adds the node type to the list of types used when writing objects of the given class to the JCR.  Multiple mixin
     * types can be set per class.
     * @param inputClass
     * @param nodeType
     */
    public void registerMixinNodeTypeForClass(final Class<?> inputClass, final String nodeType) {
        mixinTypeMap.put(inputClass, nodeType);
    }

    /**
     * Removes the node type from the list of types used when writing objects of the given class to the JCR.
     * @param inputClass
     * @param nodeType
     */
    public void unregisterMixinNodeTypeForClass(final Class<?> inputClass, final String nodeType) {
        mixinTypeMap.remove(inputClass, nodeType);
    }

    @Override
    public ObjectReader generateObjectReader(final Class<?> inputClass) {
        return wrapped.generateObjectReader(inputClass);
    }

    @Override
    public ObjectWriter generateObjectWriter(final Class<?> inputClass) {
        final ObjectWriter writer = wrapped.generateObjectWriter(inputClass);
        if (writer instanceof CompositeObjectWriter) {
            CompositeObjectWriter composite = (CompositeObjectWriter) writer;
            if (!composite.getItemNames().contains(JCR_PRIMARY_TYPE)) {
                final String type = primaryTypeMap.getOrDefault(inputClass, DEFAULT_PRIMARY_TYPE);
                composite.setItemWriter(JCR_PRIMARY_TYPE, new ConstantWriter(JCR_PRIMARY_TYPE, type));
            }
            if (!composite.getItemNames().contains(JCR_MIXIN_TYPES)) {
                Set<String> mixinTypes = mixinTypeMap.get(inputClass);
                if (mixinTypes != null && !mixinTypes.isEmpty()) {
                    final String[] types = mixinTypes.toArray(new String[mixinTypes.size()]);
                    composite.setItemWriter(JCR_MIXIN_TYPES, new ConstantWriter(JCR_PRIMARY_TYPE, types));
                }
            }
        }
        return writer;
    }

    @Override
    public ObjectMerger generateObjectMerger(final Class<?> inputClass) {
        final ObjectMerger writer = wrapped.generateObjectMerger(inputClass);
        if (writer instanceof CompositeObjectMerger) {
            CompositeObjectMerger composite = (CompositeObjectMerger) writer;
            if (!composite.getItemNames().contains(JCR_PRIMARY_TYPE)) {
                final String type = primaryTypeMap.getOrDefault(inputClass, DEFAULT_PRIMARY_TYPE);
                composite.setItemMerger(JCR_PRIMARY_TYPE, new ConstantWriter(JCR_PRIMARY_TYPE, type));
            }
            if (!composite.getItemNames().contains(JCR_MIXIN_TYPES)) {
                Set<String> mixinTypes = mixinTypeMap.get(inputClass);
                if (mixinTypes != null && !mixinTypes.isEmpty()) {
                    final String[] types = mixinTypes.toArray(new String[mixinTypes.size()]);
                    composite.setItemMerger(JCR_MIXIN_TYPES, new ConstantWriter(JCR_PRIMARY_TYPE, types));
                }
            }
        }
        return writer;
    }

    private static class ConstantWriter implements ItemWriter, ItemMerger {

        private final String propertyName;
        private final Object propertyValue;

        private ConstantWriter(final String propertyName, final Object propertyValue) {
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
        }

        @Override
        public void write(final Object object, final Resource resource, final WriteContext context) {
            final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
            valueMap.put(propertyName, propertyValue);
        }

        @Override
        public void merge(final Object instance, final Resource resource, final MergeContext context)
            throws MappingException {
            final ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
            final Object old = valueMap.get(propertyName);
            final ChangeType changeType = ChangeType.fromVersions(old, propertyValue);
            if (changeType != null) {
                if (changeType == ChangeType.DELETE) {
                    valueMap.remove(propertyName);
                } else {
                    valueMap.put(propertyName, propertyValue);
                }
                context.changeWritten(resource, propertyName, changeType);
            }
        }
    }
}
