package scenario03;

import java.util.function.IntSupplier;

// Compiled with JDK 16
public class TestClass {

    public void doSwitch(SomeEnum someEnum) {
        switch (someEnum) {
            case A, B, C, D, E -> {}
        }
    }

    public IntSupplier intSupplier(Inner inner) {
        return () -> inner.notSynthetic;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class Inner {
        private int notSynthetic;
    }

    private enum SomeEnum {
        A, B, C, D, E
    }
}
