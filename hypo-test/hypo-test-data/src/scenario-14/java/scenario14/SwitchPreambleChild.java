package scenario14;

// Compiled with JDK 25
public class SwitchPreambleChild extends ParentClass {

    public SwitchPreambleChild(int value) {
        int category = switch (value) {
            case 0 -> 0;
            case 1 -> 1;
            default -> 2;
        };
        super(value);
    }
}
