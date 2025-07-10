package edu.cs4730.wearappvoice.utils;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ExpandableByteArray {
    private ByteBuffer buffer;

    public ExpandableByteArray(int initialCapacity) {
        buffer = ByteBuffer.allocate(initialCapacity);
    }

    public void add(byte... bytes) {
        // 如果添加的字节数将使得buffer容量不足，则扩展容量
        if (buffer.remaining() < bytes.length) {
            int newCapacity = buffer.capacity() + Math.max(buffer.capacity() / 2, bytes.length);
            ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
            buffer.flip(); // 切换到读模式
            newBuffer.put(buffer); // 复制旧的数据到新的buffer
            buffer = newBuffer;
        }
        buffer.put(bytes); // 添加新的字节数据
    }

    public byte[] toArray() {
        buffer.flip(); // 切换到读模式
        byte[] array = new byte[buffer.limit()];
        buffer.get(array); // 复制数据到字节数组
        return array;
    }

    public void clear() {
        if(buffer != null)
            buffer.clear();
        buffer = ByteBuffer.allocate(1024);
    }

/*    public static void main(String[] args) {
        ExpandableByteArray eba = new ExpandableByteArray(10);
        eba.add(1, 2, 3); // 添加3个字节
        eba.add(4, 5, 6, 7, 8); // 添加5个字节，需要扩展容量
        byte[] result = eba.toArray(); // 获取包含所有添加的字节的数组
        // 输出结果以验证
        System.out.println(Arrays.toString(result));
    }*/
}