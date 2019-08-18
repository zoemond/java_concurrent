import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class OOMExecutor {

    void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        OOMExecutor tester = new OOMExecutor();
//        tester.allocateMemoryStepByStep(b -> {
//        });
//        tester.allocateMemoryPerThread();
        tester.blockThreadWithBigMemory();
    }

    // byteSum が totalMemory を超えるあたりで[OutOfMemoryError: Java heap space]になることがわかる
    void allocateMemoryStepByStep(Consumer<Long> perLoopConsumer) {
        List<byte[]> buffers = new ArrayList<>();
        long byteSum = 0;
        try {
            while (true) {
                int byteMemory = 1024 * 1024 * 100;
                buffers.add(new byte[byteMemory]);
                byteSum += byteMemory;

                perLoopConsumer.accept(byteSum);
            }
        } catch (OutOfMemoryError e) {
            // freeMemory() > 0 でもOutOfMemoryになる. 理由は不明.
            System.out.println("totalMemory: " + Runtime.getRuntime().totalMemory() + ", byteSum: " + byteSum);
        }
    }

    Runnable allocateMemoryWileSleeping(int seconds) {
        return () -> {
            allocateMemoryStepByStep(sum -> {
                System.out.println("zzz..." + Thread.currentThread().getName() + ", " + sum);
                sleep(seconds);
            });
        };
    }

    // 各Threadで確保したByteの合計がtotalMemoryを超えるあたりでOOMになることがわかる.
    // 一つのThreadがOOMで終了すると残りのThreadはtotalMemoryを超えるまで動き続ける.
    void allocateMemoryPerThread() {
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(allocateMemoryWileSleeping(1));
        service.execute(allocateMemoryWileSleeping(1));
    }


    // 1つのTreadPoolServiceだとメモリが圧迫されて他のスレッドがブロックされて進まないこと
    void blockThreadWithBigMemory() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long smallMemory = totalMemory;
        long bigMemory = totalMemory;

        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(() -> {
            while (true) {
                System.out.print(Thread.currentThread().getName() + ", ");
                allocateMemory((int) bigMemory, 10);
            }
        });
        service.execute(() -> {
            while (true) {
                System.out.print(Thread.currentThread().getName() + ", ");
                allocateMemory((int) smallMemory, 1);
            }
        });
    }

    void allocateMemory(int byteSize, int sleepSeconds) {
        List<byte[]> buffers = new ArrayList<>();
        buffers.add(new byte[byteSize]);
        sleep(sleepSeconds);
        System.out.println("byteMemory: " + byteSize + ", buffer size: " + buffers.size());
    }

    // 別々のTreadPoolServiceだとメモリが圧迫されても他の片方のThreadPoolServiceのスレッドは動くこと
    void nonBlockThreadWithBigMemory() {

    }
}
