package dev;

public class Timer {
    String name;
    long start;

    public static Timer start(String name) {
        Timer t = new Timer();
        t.name = name;
        t.start = System.nanoTime();
        return t;
    }

    public void stop() {
        long end = System.nanoTime();
        System.out.println(name + " : "+ (end - start) / 1E6 +"ms");
    }
}
