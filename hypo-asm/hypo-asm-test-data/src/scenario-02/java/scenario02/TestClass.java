package scenario02;

// Compiled with JDK 16
public class TestClass {

    public void test() {
        class LocalClass {
        }
        // anonymous class
        new Object(){};
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class InnerClass {
        public void test() {
            class LocalClass {
            }
            // anonymous class
            new Object(){};
        }

        public class NestedInnerClass {
            public void test() {
                class LocalClass {
                }
                // anonymous class
                new Object(){};
            }
        }
    }

    public static class StaticInnerClass {
        public void test() {
            class LocalClass {
            }
            // anonymous class
            new Object(){};
        }
    }
}
