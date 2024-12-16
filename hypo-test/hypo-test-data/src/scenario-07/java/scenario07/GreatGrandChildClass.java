package scenario07;

import java.util.List;
import java.util.Locale;

// Compiled with JDK 21
public class GreatGrandChildClass extends GrandChildClass {

    public GreatGrandChildClass(int left, int right, long up, String down, List<String> forward, Object back) {
        super(back, forward, down.toUpperCase(Locale.ROOT), up, right, left);
    }
}
