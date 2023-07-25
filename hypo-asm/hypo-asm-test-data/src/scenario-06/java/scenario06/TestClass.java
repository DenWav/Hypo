package scenario06;

import java.util.Random;

// Compiled with JDK 17
public class TestClass {

    public void test() {
        // no params
        new Object() {};

        final String s1 = Integer.toString(new Random().nextInt());
        final String s2 = Integer.toString(new Random().nextInt());

        // 2 string params
        class LocalClass {
            void method() {
                System.out.println(s1 + s2);
            }
        }

        // 2 string params
        new Object() {
            void method() {
                System.out.println(s1 + s2);
            }
        };

        new LocalClass();

        // not used anywhere
        final int i1 = new Random().nextInt();
        final int i2 = new Random().nextInt();

        // Multiple constructor calls
        new LocalClass();
    }
}
