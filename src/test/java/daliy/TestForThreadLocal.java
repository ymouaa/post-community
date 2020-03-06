package daliy;

import java.util.concurrent.atomic.AtomicInteger;

public class TestForThreadLocal {

    /***
     *   {@code ThreadLocal} instances are typically private
     * static fields in classes that wish to associate state with a thread (e.g.,
     * a user ID or Transaction ID).
     * @param args
     * ThreadLocal实例通常是类中的私有的静态属性，用来和一个线程关联状态
     */
    private static ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    public static T get() {
        return (T) threadLocal.get();
    }

    public static void set(T value) {
        threadLocal.set(value);
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static void main(String[] args) {
        final T t = new T();
        set(t);
        get().i++;
        new Thread(() -> {
            set(t);
            get().i++;
            System.out.println(get() + " : " + get().i);
        }).start();

        new Thread(() -> {
            set(t);
            get().i++;
            System.out.println(get() + " : " + get().i);
        }).start();

        new Thread(() -> {
            set(t);
            get().i++;
            System.out.println(get() + " : " + get().i);
        }).start();

        new Thread(() -> {
            set(t);
            get().i++;
            System.out.println(get() + " : " + get().i);
        }).start();

        new Thread(() -> {
            set(t);
            get().i++;
            System.out.println(get() + " : " + get().i);
        }).start();

        new Thread(() -> {
            set(t);
            get().i++;
            System.out.println(get() + " : " + get().i);
        }).start();
    }

}

class T {
    public volatile int i = 1;
}
