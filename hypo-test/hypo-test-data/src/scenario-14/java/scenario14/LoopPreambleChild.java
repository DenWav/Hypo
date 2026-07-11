package scenario14;

// Compiled with JDK 25
public class LoopPreambleChild extends ParentClass {

    public LoopPreambleChild(int value) {
        int sum = 0;
        for (int i = 0; i < value; i++) {
            sum += i;
        }
        super(value);
    }
}
