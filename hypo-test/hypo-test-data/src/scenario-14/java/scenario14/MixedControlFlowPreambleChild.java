package scenario14;

// Compiled with JDK 25
public class MixedControlFlowPreambleChild extends ParentClass {

    public MixedControlFlowPreambleChild(int value) {
        int total = 0;
        for (int i = 0; i < value; i++) {
            switch (i % 3) {
                case 0 -> total += i;
                default -> {
                    try {
                        total += 100 / (i - value);
                    } catch (ArithmeticException e) {
                        total -= 1;
                    }
                }
            }
        }
        total++;
        super(value);
    }
}
