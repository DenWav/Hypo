package scenario13;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestClass {

    private String thisValue = Integer.toString(ThreadLocalRandom.current().nextInt());

    public void test(Random random) {
        // test some normal situations
        this.<String, String>call((s1, s2) -> {
        });

        this.<Integer, Integer>call((i1, i2) -> {
        });

        final String s1 = Integer.toString(random.nextInt());
        final String s2 = Integer.toString(random.nextInt());
        // with captured LVT
        this.<String, Double>call((string1, double1) -> {
            System.out.println(Double.parseDouble(string1 + s1 + s2) + double1);
        });

        // with captured `this`
        this.<String, Class<?>>call((string1, clazz1) -> {
            System.out.println(string1 + clazz1.getName() + this.thisValue);
        });

        // with captured `this` and captured LVT
        this.<String, Class<?>>call((string1, clazz1) -> {
            System.out.println(string1 + s1 + clazz1.getName() + this.thisValue);
        });

        // with captured `this` and more captured LVT
        this.<String, Class<?>>call((string1, clazz1) -> {
            System.out.println(string1 + s1 + s2 + clazz1.getName() + this.thisValue);
        });
    }

    public <T1, T2> void call(TestInterface<T1, T2> i) {}
}
