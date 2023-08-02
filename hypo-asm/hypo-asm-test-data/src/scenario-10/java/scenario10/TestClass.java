package scenario10;

import java.util.Random;

// Compiled with JDK 17
public class TestClass {

    public void test(Random random) {
        final var s1 = Integer.toString(random.nextInt());
        final var s2 = Integer.toString(random.nextInt());

        class LocalClass {
            private final String thing;

            LocalClass(final String thing) {
                this.thing = thing;
            }

            public void localThing() {
                System.out.println(s1 + s2 + this.thing);
            }
        }

        record LocalRecord(String name, int num) {}

        enum LocalEnum { ONE, TWO; }

        new LocalClass(Integer.toString(random.nextInt()));
    }

    public static void staticTest(Random random) {
        final var s1 = Integer.toString(random.nextInt());
        final var s2 = Integer.toString(random.nextInt());

        class LocalClass {
            private final String thing;

            LocalClass(final String thing) {
                this.thing = thing;
            }

            public void localThing() {
                System.out.println(s1 + s2 + this.thing);
            }
        }

        record LocalRecord(String name, int num) {}

        enum LocalEnum { ONE, TWO; }

        new LocalClass(Integer.toString(random.nextInt()));
    }
}
