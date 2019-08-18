import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class OOMExecutor {

    public static void main(String[] args) {
        OOMExecutor tester = new OOMExecutor();
        tester.detectLimitByteSize(b -> {
        });
        tester.threadOOM();
    }

    void detectLimitByteSize(Consumer<Long> perLoopConsumer) {
        List<byte[]> buffers = new ArrayList<>();
        long byteSum = 0;
        try {
            while (true) {
                int byteMemory = 1024 * 1024;
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

    // 1つのTreadPoolServiceだとメモリが圧迫されて他のスレッドがブロックされて進まないこと
    void threadOOM() {
        ExecutorService service = Executors.newFixedThreadPool(2);

        Runnable detectLimitByteSize = () -> {
            detectLimitByteSize(b -> {
                System.out.println("zzz..." + Thread.currentThread().getName());
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
