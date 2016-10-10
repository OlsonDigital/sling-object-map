# Sling Object Map #

Sling Object Map is a library that provides fast, lossless read/write/merge of arbitraty Java objects to/from a Sling resource tree (i.e., to a JCR repository, or any other system for which a modifiable resource provider is configured). It works entirely via reflection -- no annotations on target classes are required.

### Type Support ##

Any POJO class can be written, provided it meets the following requirements:

* All properties are accessible via Java-beans getters and setters
* All properties are either basic types (see below) or an object type composed of basic types (fully-recursive)
* Basic types include:
  * String
  * Boolean
  * Integer
  * Long
  * Float
  * Double
  * Date
  * Calendar
  * Enum values
  * Java collection types (Collections, Lists, Sets, Maps)
  * Arrays (of both basic and non-basic values)

Clients can add support for additional types by providing functions to serialize/deserialize their types to a Sling resource.

## Features ##

Any previously-written POJO can subsequently be read without loss, provided it has an accessible default constructor.  The OOTB system can be extended to handle classes that don't meet these requirements.  The library also provides limited support for cyclic object trees, provided the self-referencing types implement equals().

In addition to basic read/write operations, the library can merge a java object to a previously-written resource.  Only updated properties will be written to the resource tree, and a list of the changed properties/sub-resources will be returned.

## Serialization Format ##

Objects are written to the Sling resource tree in a format that tracks the runtime class (and the bundle that loaded the class) to ensure lossless deserialization.  For example, an instance of the following class...

```java
package example;

public class Example {

	private String value = "test";
	
	public String getValue() {
		return value;
	}
	
	public void setValue(final String value) {
		this.value = value;
	}
}
```

...would be written as the following resource tree (when persisted to a JCR repository):

```
+ resource-name (nt:unstructured)
  - map:runtimeClass (string) = "example.Example"
  - map:bundleName (string) = "example-bundle"
  - value (string) = "test"
```

Object trees that can't be written as a single resource will be written as a tree of resources:

```java
package example;

public class ExampleContainer {

	private Example contained = new Example();
	
	public Example getContained() {
		return contained;
	}
	
	public void setContained(final Example contained) {
		this.contained = contained;
	}
}
```

Would be written as:

```
+ resource-name (nt:unstructured)
  - map:runtimeClass (string) = "example.ExampleContainer"
  - map:bundleName (string) = "example-bundle"
  + contained (nt:unstructured)
    - map:runtimeClass (string) = "example.Example"
    - map:bundleName (string) = "example-bundle"
    - value (string) = "test"
```

### Usage ##

The example below shows the basic flow for building an ObjectMapper instance and writing/reading/merging an object tree.

```java
package com.icfolson.sling.slingmap.runtime;

import com.icfolson.sling.slingmap.api.basictype.BasicTypeRegistry;
import com.icfolson.sling.slingmap.api.domain.MergeResult;
import com.icfolson.sling.slingmap.api.exception.MappingException;
import com.icfolson.sling.slingmap.api.generator.MappingGenerator;
import com.icfolson.sling.slingmap.api.mapper.ObjectMapper;
import com.icfolson.sling.slingmap.api.registry.ReaderWriterRegistry;
import com.icfolson.sling.slingmap.runtime.basictype.JcrBasicTypeRegistry;
import com.icfolson.sling.slingmap.runtime.generator.JcrMappingGeneratorDecorator;
import com.icfolson.sling.slingmap.runtime.generator.ReflectionMappingGenerator;
import com.icfolson.sling.slingmap.runtime.mapper.DefaultObjectMapper;
import com.icfolson.sling.slingmap.runtime.registry.DefaultReaderWriterRegistry;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

public class Usage {

    public void exampleUsage(ResourceResolver resolver) throws MappingException, PersistenceException {
        BasicTypeRegistry basicTypeRegistry = new JcrBasicTypeRegistry();
        // Add new basic types, if desired
        MappingGenerator generator = new ReflectionMappingGenerator(basicTypeRegistry); // Automatically generate mappings via reflection
        generator = new JcrMappingGeneratorDecorator(generator); // Add JCR-specific handling (node types)
        ReaderWriterRegistry registry = new DefaultReaderWriterRegistry(generator);
        // Register custom reader/writer/merger implementations, if desired
        ObjectMapper mapper = new DefaultObjectMapper(registry);

        Example a = new Example("A");
        Example b = new Example("B");
        Example c = new Example("C");
        
        a.setNext(b);
        b.setNext(c);
        c.setNext(a);  // Object graphs don't need to be acyclic

        Resource target = resolver.resolve("/test"); // May be non-existing
        mapper.writeObject(a, target);
        resolver.commit(); // Changes are not saved automatically

        /*

        The resulting structure is written at /test

        + test
          - jcr:primaryType = "nt:unstructured"
          - map:runtimeClass = "com.icfolson.sling.slingmap.runtime.Usage$Example"
          - map:bundleName = "sling-object-map-runtime"
          - name = "A"
          + next
            - jcr:primaryType = "nt:unstructured"
            - map:runtimeClass = "com.icfolson.sling.slingmap.runtime.Usage$Example"
            - map:bundleName = "sling-object-map-runtime"
            - name = "B"
            + next
              - jcr:primaryType = "nt:unstructured"
              - map:runtimeClass = "com.icfolson.sling.slingmap.runtime.Usage$Example"
              - map:bundleName = "sling-object-map-runtime"
              - name = "C"
              + next
                - jcr:primaryType = "nt:unstructured"
                - map:referencePath = "/test"
         */

        target = resolver.resolve("/test");
        Example readValue = mapper.readObject(target, Example.class);
        assert readValue.equals(a);
        assert readValue.getNext().equals(b);
        assert readValue.getNext().getNext().equals(c);
        assert readValue.getNext().getNext().getNext().equals(a);

        a.setName(null);
        b.setName("b");
        c.setNext(null);
        target = resolver.resolve("/test");
        MergeResult result = mapper.mergeObject(a, target);
        resolver.commit();

        /*

        Result contains three changes:

        - /@name: DELETE
        - /next/@name: UPDATE
        - /next/next/next: DELETE

        The resulting structure is written at /test

        + test
          - jcr:primaryType = "nt:unstructured"
          - map:runtimeClass = "com.icfolson.sling.slingmap.runtime.Usage$Example"
          - map:bundleName = "sling-object-map-runtime"
          + next
            - jcr:primaryType = "nt:unstructured"
            - map:runtimeClass = "com.icfolson.sling.slingmap.runtime.Usage$Example"
            - map:bundleName = "sling-object-map-runtime"
            - name = "b"
            + next
              - jcr:primaryType = "nt:unstructured"
              - map:runtimeClass = "com.icfolson.sling.slingmap.runtime.Usage$Example"
              - map:bundleName = "sling-object-map-runtime"
              - name = "C"
         */

    }

    public static final class Example {

        private String name;
        private Example next;

        public Example() { }

        public Example(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public Example getNext() {
            return next;
        }

        public void setNext(final Example next) {
            this.next = next;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final Example example = (Example) o;

            return name != null ? name.equals(example.name) : example.name == null;

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

}

```
