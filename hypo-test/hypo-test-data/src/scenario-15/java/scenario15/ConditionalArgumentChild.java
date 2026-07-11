package scenario15;

// Compiled with JDK 21
public class ConditionalArgumentChild extends ParentClass {

    public ConditionalArgumentChild(int value, int flag) {
        super(flag > 0 ? value : 0);
    }
}
