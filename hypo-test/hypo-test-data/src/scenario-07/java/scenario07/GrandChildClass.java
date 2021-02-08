package scenario07;

import java.util.List;

public class GrandChildClass extends ChildClass {

    public GrandChildClass(Object back, List<String> forward, String down, long up, int right, int left) {
        super(right, left, down, up, back, forward);
    }
}
