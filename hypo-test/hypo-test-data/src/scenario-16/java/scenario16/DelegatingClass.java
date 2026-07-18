package scenario16;

public class DelegatingClass extends ParentClass {
    public DelegatingClass(int value) {
        super(value);
    }

    public DelegatingClass(String label, int value) {
        this(value);
    }
}
