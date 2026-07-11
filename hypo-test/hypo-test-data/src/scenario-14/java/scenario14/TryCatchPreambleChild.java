package scenario14;

// Compiled with JDK 25
public class TryCatchPreambleChild extends ParentClass {

    public TryCatchPreambleChild(int value) {
        int result;
        try {
            result = 100 / value;
        } catch (ArithmeticException e) {
            result = 0;
        }
        super(value);
    }
}
