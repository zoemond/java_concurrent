import java.util.ArrayList;
import java.util.List;


public class OOMExecutor {

    public static void main(String[] args) {
        new OOMExecutor().detectLimitByteSize();
    }

    void detectLimitByteSize() {
        List<byte[]> buffers = new ArrayList<>();
        long byteSum = 0;
        try {
            while (true) {
                int byteMemory = 1024 * 1024;
                buffers.add(new byte[byteMemory]);
                byteSum += byteMemory;
            }
        } catch (OutOfMemoryError e) {
            System.out.println("byteSum が totalMemory を超えるあたりで[OutOfMemoryError: Java heap space]になることがわかる");
            // freeMemory() > 0 でもOutOfMemoryになる. 理由は不明.
            System.out.println("totalMemory: " + Runtime.getRuntime().totalMemory() + ", byteSum: " + byteSum);
        }
    }

}
