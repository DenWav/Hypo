package scenario14;

// Compiled with JDK 25
public class IncDecPreambleChild extends ParentClass {

    public IncDecPreambleChild(int value) {
        int counter = value;
        counter++;
        --counter;
        super(value);
    }
}
