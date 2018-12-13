package com.icfolson.sling.slingmap.runtime.mapper;

import com.google.common.base.Preconditions;
import com.icfolson.sling.slingmap.api.constants.JcrProperties;
import com.icfolson.sling.slingmap.api.domain.ChangeType;
import com.icfolson.sling.slingmap.api.domain.MergeContext;
import com.icfolson.sling.slingmap.api.domain.MergeOptions;
import com.icfolson.sling.slingmap.api.domain.MergeResult;
import com.icfolson.sling.slingmap.api.domain.ObjectMerger;
import com.icfolson.sling.slingmap.api.domain.ObjectReader;
import com.icfolson.sling.slingmap.api.domain.ObjectWriter;
import com.icfolson.sling.slingmap.api.domain.ReadContext;
import com.icfolson.sling.slingmap.api.domain.WriteContext;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.mapper.ObjectMapper;
import com.icfolson.sling.slingmap.api.registry.ReaderWriterRegistry;
import com.icfolson.sling.slingmap.runtime.registry.types.reference.Reference;
import com.icfolson.sling.slingmap.runtime.registry.types.reference.ReferenceObjectMerger;
import com.icfolson.sling.slingmap.runtime.registry.types.reference.ReferenceObjectWriter;
import com.icfolson.sling.slingmap.runtime.util.ClassUtil;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 * The default object mapper implementation.
 */
public class DefaultObjectMapper implements ObjectMapper {

    private final ReaderWriterRegistry readerWriterRegistry;

    public DefaultObjectMapper(final ReaderWriterRegistry readerWriterRegistry) {
        this.readerWriterRegistry = readerWriterRegistry;
    }

    @Override
    public Object readObject(final Resource resource) throws MappingException {
        final DefaultReadContext context = new DefaultReadContext(resource);
        return context.readObject();
    }

    @Override
    public <T> T readObject(final Resource resource, final Class<T> type) throws MappingException {
        final DefaultReadContext context = new DefaultReadContext(resource, type);
        return (T) context.readObject();
    }

    @Override
    public void writeObject(final Object object, final Resource target) throws MappingException {
        final DefaultWriteContext context = new DefaultWriteContext();
        context.write(object, target);
    }

    @Override
    public MergeResult mergeObject(final Object object, final Resource target)
        throws MappingException {

        DefaultMergeContext context = new DefaultMergeContext(target.getPath());
        return context.merge(object, target);
    }

    @Override
    public MergeResult mergeObject(final Object object, final Resource target, final MergeOptions options)
        throws MappingException {

        DefaultMergeContext context = new DefaultMergeContext(target.getPath(), options);
        return context.merge(object, target);
    }

    /**
     * Object used for tracking an object being read from a Sling resource tree.
     */
    private static class ReadItem implements ReadContext.Callback {

        private final String path;
        private final ReadContext.Callback callback;
        private final List<ReadItem> children = new ArrayList<>();
        private Object value;

        private ReadItem(final String path, final ReadContext.Callback callback) {
            this.path = path;
            this.callback = callback;
        }

        @Override
        public void objectRead(final Object read) throws MappingException {
            this.value = read;
        }

        /**
         * Recurse through items, executing the callbacks from the leaves to the root
         * @throws MappingException
         */
        private void executeCallbacks() throws MappingException {
            for (final ReadItem child : children) {
                child.executeCallbacks();
            }
            callback.objectRead(value);
        }
    }

    /**
     * Coordinates the read of a tree of Java objects from a Sling resource tree.  In order to handle reference nodes
     * properly, the algorithm for reading in an object graph is as follows:
     *
     * When instantiating a class containing a reference, a request to read a child resource is submitted.  This
     * request will create the child instance immediately (if the corresponding child resource exists), but will not
     * return it to the requestor until the child's entire subgraph (including its outgoing references, and its
     * children's outgoing references, etc.) are fully initialized.  As such, instantiation will proceed outward from
     * the root object in a breadth-first manner, but the assignment of references will happen starting at the leaf
     * nodes.  The final object to have referenced instances assigned will be the root node, which will then be returned
     * to the calling function.
     */
    private class DefaultReadContext implements ReadContext {

        private final Queue<ReadItem> itemQueue = new LinkedList<>();
        private final Map<String, Object> readObjects = new HashMap<>();
        private final ResourceResolver resolver;
        private RootCallback rootCallback;
        private ReadItem rootItem;
        private ReadItem current;
        private Class<?> deserializationType;

        public DefaultReadContext(final Resource resource, final Class<?> deserializationType) throws MappingException {
            this.deserializationType = deserializationType;
            resolver = resource.getResourceResolver();
            rootCallback = new RootCallback();
            rootItem = new ReadItem(resource.getPath(), rootCallback);
            itemQueue.add(rootItem);
        }

        public DefaultReadContext(final Resource resource) throws MappingException {
            this(resource, null);
        }

        public Object readObject() throws MappingException {
            while (!itemQueue.isEmpty()) {
                current = itemQueue.poll();
                doRead(resolver, current);
            }
            rootItem.executeCallbacks();
            return rootCallback.readValue;
        }

        @Override
        public void recursiveRead(final Resource target, final Callback callback) throws MappingException {
            final ReadItem readItem = new ReadItem(target.getPath(), callback);
            itemQueue.add(readItem);
            if (current != null) {
                current.children.add(readItem);
            }
        }

        private void doRead(final ResourceResolver resolver, final ReadItem readItem)
            throws MappingException {

            final String path = readItem.path;
            final Callback cb = readItem.callback;
            final Resource target = resolver.resolve(path);
            // Case 1: Type explicitly specified (root only)
            if  (deserializationType != null) {
                final ObjectReader reader = readerWriterRegistry.getReader(deserializationType);
                Object out = reader.read(target, this, deserializationType);
                if (out == null) {
                    out = target.adaptTo(deserializationType);
                }
                readObjects.put(target.getPath(), out);
                readItem.objectRead(out);
                deserializationType = null; // only root can be specified -- infer types from class definition from now
                return;
            }
            // Case 2: Type must be inferred
            final Class<?> modelType = ClassUtil.tryLoadModelClassForSerializedResource(target);
            if (modelType != null) {
                final Object out = target.adaptTo(modelType);
                readObjects.put(target.getPath(), out);
                readItem.objectRead(out);
            } else {
                final Class<?> classType = ClassUtil.tryLoadClassForSerializedResource(target);
                if (classType == null) {
                    tryReadReference(target, cb);
                } else {
                    final ObjectReader reader = readerWriterRegistry.getReader(classType);
                    final Object out = reader.read(target, this);
                    readObjects.put(target.getPath(), out);
                    readItem.objectRead(out);
                }
            }
        }

        private void tryReadReference(final Resource target, final Callback callback) throws MappingException {
            final String referencePath = target.getValueMap().get(JcrProperties.REFERENCE_PATH, String.class);
            if (referencePath != null) {
                if (readObjects.containsKey(referencePath)) {
                    callback.objectRead(readObjects.get(referencePath));
                } else {
                    final Resource resource = target.getResourceResolver().resolve(referencePath);
                    if (!ResourceUtil.isNonExistingResource(resource)) {
                        recursiveRead(resource, callback);
                    }
                }
            }
        }
    }

    /**
     * Data structure used to track an object being written/merged.
     */
    private static class WriteItem {

        private final Object object;
        private final Resource resource;
        private final boolean merge;

        private WriteItem(final Object object, final Resource resource) {
            this(object, resource, false);
        }

        private WriteItem(final Object object, final Resource resource, final boolean merge) {
            this.object = object;
            this.resource = resource;
            this.merge = merge;
        }
    }

    /**
     * Used to queue up objects being recursively written to a resource tree.  The queue allows writing to proceed in a
     * breadth-first manner, which is necessary to detect reference cycles and prevent infinite loops.
     */
    private class DefaultWriteContext implements WriteContext {

        protected final Queue<WriteItem> itemQueue = new LinkedList<>();
        protected final Map<Object, Resource> writeHistory = new HashMap<>();

        public void write(final Object root, final Resource target) throws MappingException {
            queueWrite(root, target);
            while (!itemQueue.isEmpty()) {
                final WriteItem item = itemQueue.poll();
                writeItem(item);
            }
        }


        /**
         * Perform a write of a referenced object.  If the object is found to have been already written, a cycle in the
         * object graph has been found.  Rather than writing that object again, a reference to the already-written
         * resource is created.  If not, the object is written normally.
         */
        protected void writeItem(final WriteItem item) throws MappingException {
            final Object object = item.object;
            final Resource resource = item.resource;
            if (writeHistory.containsKey(object)) {
                final String existingPath = writeHistory.get(object).getPath();
                final Reference reference = new Reference();
                reference.setReferencePath(existingPath);
                final ReferenceObjectWriter writer = new ReferenceObjectWriter(reference);
                writer.write(object, resource, this);
            } else {
                final Class<?> objectClass = object.getClass();
                final ObjectWriter writer = readerWriterRegistry.getWriter(objectClass);
                writer.write(object, resource, this);
                writeHistory.put(object, resource);
            }
        }

        @Override
        public void queueWrite(final Object object, final Resource resource) {
            Preconditions.checkNotNull(object, "Object cannot be null");
            Preconditions.checkNotNull(resource, "Resource cannot be null");
            itemQueue.add(new WriteItem(object, resource));
        }
    }

    /**
     * Contains similar algorithms to the write context, with additional functionality for tracking changes as the
     * write proceeds
     */
    private class DefaultMergeContext extends DefaultWriteContext implements MergeContext {

        private final String mergeRoot;
        private final Map<String, ChangeType> changes = new TreeMap<>();
        private final MergeOptions options;

        private DefaultMergeContext(final String mergeRoot) {
            this(mergeRoot, DefaultMergeOptions.INSTANCE);
        }

        private DefaultMergeContext(final String mergeRoot, final MergeOptions options) {
            this.mergeRoot = mergeRoot;
            this.options = options;
        }

        public MergeResult merge(final Object root, final Resource target) throws MappingException {
            queueMerge(root, target);
            while (!itemQueue.isEmpty()) {
                final WriteItem item = itemQueue.poll();
                if (item.merge) {
                    mergeItem(item);
                } else {
                    writeItem(item);
                }
            }
            return mergeResultFromMap(changes);
        }

        private void mergeItem(WriteItem item) throws MappingException {
            final Object object = item.object;
            final Resource resource = item.resource;
            if (writeHistory.containsKey(object)) {
                final String existingPath = writeHistory.get(object).getPath();
                final Reference reference = new Reference();
                reference.setReferencePath(existingPath);
                final ReferenceObjectMerger merger = new ReferenceObjectMerger(reference);
                merger.merge(reference, resource, this);
            } else {
                final Class<?> objectClass = object != null ? object.getClass() : null;
                if (ResourceUtil.isNonExistingResource(resource) && object != null) {
                    final ObjectWriter writer = readerWriterRegistry.getWriter(objectClass);
                    writer.write(object, resource, this);
                    changeWritten(resource, null, ChangeType.ADD);
                    writeHistory.put(object, resource);
                } else if (!ResourceUtil.isNonExistingResource(resource) && object == null) {
                    final ResourceResolver resolver = resource.getResourceResolver();
                    try {
                        changeWritten(resource, null, ChangeType.DELETE);
                        resolver.delete(resource);
                    } catch (PersistenceException e) {
                        throw new MappingException(e);
                    }
                } else if (object != null) {
                    final ObjectMerger merger = readerWriterRegistry.getMerger(objectClass);
                    merger.merge(object, resource, this);
                    writeHistory.put(object, resource);
                }
            }
        }

        @Override
        public void queueMerge(final Object object, final Resource resource) {
            Preconditions.checkNotNull(resource, "Resource cannot be null");
            itemQueue.add(new WriteItem(object, resource, true));
        }

        @Override
        public void changeWritten(final Resource resource, final String propertyName, final ChangeType changeType) {
            String mergePath = resource.getPath().substring(mergeRoot.length())
                + (propertyName != null ? "/@" + propertyName : "");
            if (mergePath.isEmpty()) {
                mergePath = "/";
            }
            final ChangeType existing = changes.get(mergePath);
            if (changeType == ChangeType.ADD && existing == ChangeType.DELETE) {
                changes.put(mergePath, ChangeType.UPDATE);
            } else {
                changes.put(mergePath, changeType);
            }
        }

        @Override
        public MergeOptions getOptions() {
            return options;
        }
    }

    private static MergeResult mergeResultFromMap(final Map<String, ChangeType> map) {
        List<MergeResult.MergeChange> changes = new ArrayList<>();
        map.entrySet().forEach(e -> changes.add(new MergeChangeImpl(e.getKey(), e.getValue())));
        return () -> changes;
    }

    private static class MergeChangeImpl implements MergeResult.MergeChange {

        private final String mergePath;
        private final ChangeType changeType;

        private MergeChangeImpl(final String mergePath, final ChangeType changeType) {
            this.mergePath = mergePath;
            this.changeType = changeType;
        }

        @Override
        public String getPath() {
            return mergePath;
        }

        @Override
        public ChangeType getType() {
            return changeType;
        }
    }

    private static class DefaultMergeOptions implements MergeOptions {

        public static final DefaultMergeOptions INSTANCE = new DefaultMergeOptions();

    }

    private static class RootCallback implements ReadContext.Callback {

        private Object readValue;

        @Override
        public void objectRead(final Object read) {
            readValue = read;
        }
    }
}
