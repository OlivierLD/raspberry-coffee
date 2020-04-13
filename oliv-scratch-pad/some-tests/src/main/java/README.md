### To run the processor (generates new code at **_Compile_** Time)
> From <https://www.baeldung.com/java-annotation-processing-builder>

From the folder where `oliv` is:
```
$ javac oliv/annotations/processors/BuilderProcessor.java 
$ javac oliv/annotations/processors/BuilderProperty.java
$
$ javac -processor oliv.annotations.processors.BuilderProcessor oliv/annotations/processors/Person.java 
```
Then `PersonBuilder.java` was generated:
```java
package oliv.annotations.processors;

public class PersonBuilder {

    private Person object = new Person();

    public Person build() {
        return object;
    }

    public PersonBuilder setName(java.lang.String value) {
        object.setName(value);
        return this;
    }

    public PersonBuilder setAge(int value) {
        object.setAge(value);
        return this;
    }

}
``` 
> Something to look at: Method interceptors: <https://www.ibm.com/support/knowledgecenter/en/SS7K4U_8.5.5/com.ibm.websphere.zseries.doc/ae/twbs_jaxrs_jcdi_decoratorsandmethod.html>
