package scenario14;

// Compiled with JDK 25
public class NewInPreambleChild extends ParentClass {

    public NewInPreambleChild(int value) {
        Helper helper = new Helper(value);
        super(value);
    }
}
