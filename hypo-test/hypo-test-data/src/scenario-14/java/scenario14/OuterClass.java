package scenario14;

// Compiled with JDK 25
public class OuterClass {

    public class InnerChild extends BaseClass {

        public InnerChild(int value) {
            int doubled = value * 2;
            super(value);
        }
    }
}
