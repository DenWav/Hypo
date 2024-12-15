package scenario07;

import java.util.Random;

// Compiled with JDK 21
public class TestClass {

    public void test() {
        final var r = new Random();
        final int i1 = r.nextInt();
        final int i2 = r.nextInt();
        final int i3 = r.nextInt();

        // multiply nested lambdas
        final Runnable run = () -> {
            final Runnable run1 = () -> {
                final Runnable run2 = () -> {
                    new Object() {
                        public void run3() {
                            System.out.println(i1 + i2 + i3);
                        }
                    };
                };
            };
        };
    }
}
