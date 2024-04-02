import java.lang.*;

class Starter {

    public static void startRunnables(Runnable[] runnables) {
        // implement the method
        for (Runnable runnable : runnables) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}