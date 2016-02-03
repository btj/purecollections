Java's standard collection classes in the java.util package are mutable. As a result, passing instances of these classes between modules requires special care, such as making copies or wrapping them in unmodifiable wrappers, to avoid inadvertent modifications of a module's internal state representation by other modules, a problem known as **representation exposure**.

For example, consider a Person class that maintains a map of children and a list of telephone numbers. We show two variants of each getter: one which returns an unmodifiable wrapper, and one which creates a copy. Note that the public constructor that accepts initial values for the children and the phone numbers needs to defensively create a copy of the incoming collections.

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Person {
    
    private final Map<String, Person> children;
    private final Map<String, Person> childrenUnmodifiableWrapper;
    private final List<String> phoneNumbers;
    private final List<String> phoneNumbersUnmodifiableWrapper;
    
    private Person(Map<String, Person> children, List<String> phoneNumbers, boolean dummy) {
    	this.children = children;
    	this.childrenUnmodifiableWrapper = Collections.unmodifiableMap(this.children);
    	this.phoneNumbers = phoneNumbers;
    	this.phoneNumbersUnmodifiableWrapper = Collections.unmodifiableList(this.phoneNumbers);
    }
    
    public Person(Map<String, Person> children, List<String> phoneNumbers) {
    	// Create defensive copies
    	this(new HashMap<String, Person>(children), new ArrayList<String>(phoneNumbers), false);
    }
    
    public Person() {
    	this(new HashMap<String, Person>(), new ArrayList<String>(), false);
    }
        
    public Map<String, Person> getChildren1() {
        return childrenUnmodifiableWrapper;
    }
    
    public Map<String, Person> getChildren2() {
        return new HashMap<String, Person>(children);
    }
    
    public Person addChild(String name) {
        Person child = new Person();
        children.put(name, child);
        return child;
    }
    
    public List<String> getPhoneNumbers1() {
        return phoneNumbersUnmodifiableWrapper;
    }
    
    public List<String> getPhoneNumbers2() {
        return new ArrayList<String>(phoneNumbers);
    }
    
    public void addPhoneNumber(String phoneNumber) {
        phoneNumbers.add(phoneNumber);
    }
    
}
```

This project offers a simpler solution to the representation exposure problem. It provides a small library of **purely functional** data structures: a purely functional list data structure called PList, and a purely functional map data structure called PMap. Using purely functional data structures, which are inherently immutable, eliminates the representation exposure problem without wrapping or copying.

We can simplify the example using the purely functional PMap and PList data structures as follows. Note that no copying or wrapping is necessary, either for incoming or for outgoing collections. Since PMap and PList are classes that have no public or protected constructors, there can be no instances of these types that accidentally or maliciously exhibit mutable or otherwise incorrect behavior.

```java
import purecollections.PMap;
import purecollections.PList;

class Person {
    
    private PMap<String, Person> children;
    private PList<String> phoneNumbers;
    
    public Person(PMap<String, Person> children, PList<String> phoneNumbers) {
    	this.children = children;
    	this.phoneNumbers = phoneNumbers;
    }
    
    public Person() {
    	this(PMap.<String, Person>empty(), PList.<String>empty());
    }
    
    public PMap<String, Person> getChildren() {
        return children;
    }
    
    public Person addChild(String name) {
        Person child = new Person();
        children = children.plus(name, child);
        return child;
    }
    
    public PList<String> getPhoneNumbers() {
        return phoneNumbers;
    }
    
    public void addPhoneNumber(String phoneNumber) {
        phoneNumbers = phoneNumbers.plus(phoneNumber);
    }
    
}
```
