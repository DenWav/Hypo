package scenario09;

public class ChildClass extends ParentClass {

    public ChildClass(long i, long j) {
        super(i, j);
    }

    public ChildClass(double i, double j) {
        this((long) i, (long) j);
    }
}
