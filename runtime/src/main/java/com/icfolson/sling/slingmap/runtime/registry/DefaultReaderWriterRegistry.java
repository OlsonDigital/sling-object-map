package com.icfolson.sling.slingmap.runtime.registry;

import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.domain.ObjectReader;
import com.icfolson.sling.slingmap.api.domain.ObjectWriter;
import com.icfolson.sling.slingmap.api.generator.MappingGenerator;
import com.icfolson.sling.slingmap.api.registry.ReaderWriterRegistry;
import com.icfolson.sling.slingmap.runtime.registry.types.array.ArrayObjectMerger;
import com.icfolson.sling.slingmap.runtime.registry.types.array.ArrayObjectReader;
import com.icfolson.sling.slingmap.runtime.registry.types.array.ArrayObjectWriter;
import com.icfolson.sling.slingmap.runtime.registry.types.collection.CollectionMerger;
import com.icfolson.sling.slingmap.runtime.registry.types.collection.CollectionReader;
import com.icfolson.sling.slingmap.runtime.registry.types.collection.CollectionWriter;
import com.icfolson.sling.slingmap.runtime.registry.types.enums.EnumMerger;
import com.icfolson.sling.slingmap.runtime.registry.types.enums.EnumReader;
import com.icfolson.sling.slingmap.runtime.registry.types.enums.EnumWriter;
import com.icfolson.sling.slingmap.runtime.registry.types.map.DefaultKeyMapperRegistry;
import com.icfolson.sling.slingmap.runtime.registry.types.map.KeyMapperRegistry;
import com.icfolson.sling.slingmap.runtime.registry.types.map.MapObjectMerger;
import com.icfolson.sling.slingmap.runtime.registry.types.map.MapObjectReader;
import com.icfolson.sling.slingmap.runtime.registry.types.map.MapObjectWriter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default registry implementation.  This implementation provides builtin special handling for collections, arrays, enum
 * values, and maps.  This implementation supports fallback for undefined readers/writers to functions provided by a
 * {@link MappingGenerator} implementation
 */
public class DefaultReaderWriterRegistry implements ReaderWriterRegistry {

    private final MappingGenerator generator;
    private final KeyMapperRegistry keyMapperRegistry;

    private final Map<Class<?>, ObjectReader> readers = new HashMap<>();
    private final Map<Class<?>, ObjectWriter> writers = new HashMap<>();
    private final Map<Class<?>, ObjectMerger> mergers = new HashMap<>();

    private final MapObjectReader mapObjectReader;
    private final MapObjectWriter mapObjectWriter;
    private final MapObjectMerger mapObjectMerger;

    public DefaultReaderWriterRegistry() {
        this(null, new DefaultKeyMapperRegistry());
    }

    public DefaultReaderWriterRegistry(final MappingGenerator generator) {
        this(generator, new DefaultKeyMapperRegistry());
    }

    public DefaultReaderWriterRegistry(final MappingGenerator generator, final KeyMapperRegistry keyMapperRegistry) {
        this.generator = generator;
        this.keyMapperRegistry = keyMapperRegistry;
        this.mapObjectReader = new MapObjectReader();
        this.mapObjectWriter = new MapObjectWriter(keyMapperRegistry);
        this.mapObjectMerger = new MapObjectMerger(keyMapperRegistry);
    }

    @Override
    public void registerReader(final Class<?> outputClass, final ObjectReader reader) {
        readers.put(outputClass, reader);
    }

    @Override
    public void registerWriter(final Class<?> inputClass, final ObjectWriter writer) {
        writers.put(inputClass, writer);
    }

    @Override
    public void registerMerger(final Class<?> inputClass, final ObjectMerger writer) {
        mergers.put(inputClass, writer);
    }

    @Override
    public ObjectReader getReader(final Class<?> outputClass) {
        ObjectReader reader = readers.get(outputClass);
        if (reader == null && outputClass.isEnum()) {
            reader = EnumReader.INSTANCE;
        } else if (Collection.class.isAssignableFrom(outputClass)) {
            reader = CollectionReader.INSTANCE;
        } else if (outputClass.isArray()) {
            reader = ArrayObjectReader.INSTANCE;
        } else if (Map.class.isAssignableFrom(outputClass)) {
            reader = mapObjectReader;
        } else {
            if (generator != null) {
                reader = generator.generateObjectReader(outputClass);
                readers.put(outputClass, reader);
            }
        }
        return reader;

    }

    @Override
    public ObjectWriter getWriter(final Class<?> inputClass) {
        ObjectWriter writer = writers.get(inputClass);
        if (writer == null && inputClass.isEnum()) {
            writer = EnumWriter.INSTANCE;
        } else if (Collection.class.isAssignableFrom(inputClass)) {
            writer = CollectionWriter.INSTANCE;
        } else if (inputClass.isArray()) {
            writer = ArrayObjectWriter.INSTANCE;
        } else if (Map.class.isAssignableFrom(inputClass)) {
            writer = mapObjectWriter;
        } else {
            if (generator != null) {
                writer = generator.generateObjectWriter(inputClass);
                writers.put(inputClass, writer);
            }
        }
        return writer;
    }

    @Override
    public ObjectMerger getMerger(final Class<?> inputClass) {
        ObjectMerger merger = mergers.get(inputClass);
        if (merger == null && inputClass.isEnum()) {
            merger = EnumMerger.INSTANCE;
        } else if (Collection.class.isAssignableFrom(inputClass)) {
            merger = CollectionMerger.INSTANCE;
        } else if (inputClass.isArray()) {
            merger = ArrayObjectMerger.INSTANCE;
        } else if (Map.class.isAssignableFrom(inputClass)) {
            merger = mapObjectMerger;
        } else {
            if (generator != null) {
                merger = generator.generateObjectMerger(inputClass);
                mergers.put(inputClass, merger);
            }
        }
        return merger;
    }

}
