package scenario11;

import java.util.function.Predicate;

// Compiled with JDK 17
public enum TestEnum implements Predicate<String> {
    ONE {},
    TWO {},
    ;

    @Override
    public boolean test(final String s) {
        return false;
    }
}
