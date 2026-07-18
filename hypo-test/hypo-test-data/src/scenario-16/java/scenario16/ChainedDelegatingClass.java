package scenario16;

public class ChainedDelegatingClass extends DelegatingClass {
    public ChainedDelegatingClass(int value) {
        super(value);
    }

    public ChainedDelegatingClass(String s, int value) {
        this(value);
    }

    public ChainedDelegatingClass(int a, int b) {
        this("", a + b);
    }
}
