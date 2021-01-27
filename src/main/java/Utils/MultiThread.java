package Utils;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThread {
    private static MultiThread multiThread = null;
    private static ExecutorService executorService = null;

    protected MultiThread() {
        executorService = Executors.newFixedThreadPool(8);
    }

    /*
        线程池执行单个县线程
     */
    public static MultiThread getInstance() {
        if (multiThread != null) {
            synchronized (MultiThread.class) {
                if (multiThread == null) {
                    multiThread = new MultiThread();
                }
            }
        }
        return multiThread;
    }

    public void executeMultiThread(Thread thread) {
        try {
            executorService.execute(
                    thread
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        线程池中放入多个线程
     */
    public void executeMultiThreads(Thread[] threads) {
        try {
            for (int i = 0; i < threads.length; i++) {
                executeMultiThread(threads[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdownMultiThread() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
