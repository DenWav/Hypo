package scenario08;

public class TestClass {

    public TestClass(int i, int j) {}

    public TestClass(long i, long j) {
        this((int) i, (int) j);
    }

    public TestClass(String someText, int i, long j) {
        this(i, (int) j);
    }

    public TestClass(String someText, String whatText) {
        this(someText, 0, 0);
    }
}
