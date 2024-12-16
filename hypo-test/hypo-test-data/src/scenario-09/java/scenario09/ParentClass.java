package scenario09;

// Compiled with JDK 21
public class ParentClass {

    public ParentClass(int i, int j) {}

    public ParentClass(long i, long j) {
        this((int) i, (int) j);
    }
}
