import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class OOMExecutor {

    public static void main(String[] args) {
        OOMExecutor tester = new OOMExecutor();
        tester.detectLimitByteSize(b -> {
        });
        tester.divideMemoryToPerThread();
    }

    void detectLimitByteSize(Consumer<Long> perLoopConsumer) {
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
            System.out.println("byteSum が totalMemory を超えるあたりで[OutOfMemoryError: Java heap space]になることがわかる");
            // freeMemory() > 0 でもOutOfMemoryになる. 理由は不明.
            System.out.println("totalMemory: " + Runtime.getRuntime().totalMemory() + ", byteSum: " + byteSum);
        }
    }

    // 各Threadで確保したByteの合計がtotalMemoryを超えるあたりでOOMになることがわかる.
    // 一つのThreadがOOMで終了すると残りのThreadはtotalMemoryを超えるまで動き続ける.
    void divideMemoryToPerThread() {
        ExecutorService service = Executors.newFixedThreadPool(2);

        Runnable detectLimitByteSize = () -> {
            detectLimitByteSize(sum -> {
                System.out.println("zzz..." + Thread.currentThread().getName() + ", " + sum);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        };
        service.execute(detectLimitByteSize);
        service.execute(detectLimitByteSize);
    }
}
