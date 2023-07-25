package scenario05;

import java.util.Random;
import java.util.function.Function;

// Compiled with JDK 17
public class TestClass {

    public void test() {
        final String s3 = Integer.toString(new Random().nextInt());
        String s33 = Integer.toString(new Random().nextInt());
        StringBuilder sb = new StringBuilder();
        String str = sb.toString();
        final Runnable r = () -> {
            final String s4 = s3 + s3;
            final String s44 = s33 + s33;
            str.length();
            final Runnable r1 = () -> {
                final String s5 = s4 + s4;
            };
            final Function<String, Object> f = TestClass::thing;
        };
    }

    public static Object thing(String in) {
        return null;
    }
}
