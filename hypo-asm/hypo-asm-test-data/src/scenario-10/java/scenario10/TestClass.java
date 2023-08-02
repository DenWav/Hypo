package scenario10;

public class TestClass implements Comparable<TestClass> {

    @Override
    public int compareTo(final TestClass o) {
        throw new UnsupportedOperationException();
    }

    public static final class SubClass extends TestClass {

    }
}
