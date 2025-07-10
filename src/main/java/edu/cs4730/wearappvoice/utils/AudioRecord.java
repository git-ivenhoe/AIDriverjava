package edu.cs4730.wearappvoice.utils;

//3588
import java.io.IOException;
import java.io.InputStream;

public class AudioRecord {

    public static final int STATE_UNINITIALIZED = 0;
    public static final int STATE_INITIALIZED   = 1;
    private int mState = STATE_UNINITIALIZED;

    public static final int RECORDSTATE_STOPPED = 1;
    public static final int RECORDSTATE_RECORDING = 3;
    private int mRecordingState = RECORDSTATE_STOPPED;

    private Process arecordProcess;
    private InputStream micInputStream;
    private volatile boolean reading;

    private static final String ARECORD_DEVICE = "plughw:1,0"; //"hw:rockchipnau8822,0";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int BYTES_PER_SAMPLE = 2;  // 16bit

    public AudioRecord() {
        mState = STATE_INITIALIZED;
    }

    // 启动 arecord 子进程采集数据
    public void startRecording() {
//        if (mRecordingState == RECORDSTATE_RECORDING) return;
        //arecord -D plughw:1,0 -f S16_LE -r 16000 -c 1 -d 5 /usr/sc/test01.wav
        try {
            String cmd = String.format(
                    "pasuspender -- arecord -D %s -f S16_LE -r %d -c %d",
                    ARECORD_DEVICE, SAMPLE_RATE, CHANNELS);

            arecordProcess = Runtime.getRuntime().exec(cmd);
            micInputStream = arecordProcess.getInputStream();

            mRecordingState = RECORDSTATE_RECORDING;
            reading = true;

            // 如果你用回调模式，可以在这里启动线程实时读数据，调用回调（可选）
            // 这里不启用，read()会阻塞读

        } catch (IOException e) {
            e.printStackTrace();
            mRecordingState = RECORDSTATE_STOPPED;
        }
    }

    // 关闭录音，结束进程
    public void stopRecording() {
        reading = false;
        mRecordingState = RECORDSTATE_STOPPED;
        if (arecordProcess != null) {
            arecordProcess.destroy();
            arecordProcess = null;
        }
        if (micInputStream != null) {
            try {
                micInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            micInputStream = null;
        }
    }

    public void release() {
        stopRecording();
        mState = STATE_UNINITIALIZED;
    }

    public void stop() {
        stopRecording();
    }

    /**
     * 从 MIC 读取 short 类型 PCM 数据
     * @param buffer 输出缓冲区
     * @param offset 起始写入位置
     * @param length 需要读取的 short 数量
     * @return 实际读取 short 数量，-1 表示错误或未初始化
     */
    public int read(short[] buffer, int offset, int length) {
        if (micInputStream == null) return -1;

        int bytesToRead = length * BYTES_PER_SAMPLE;
        byte[] byteBuffer = new byte[bytesToRead];

        int totalBytesRead = 0;
        try {
            while (totalBytesRead < bytesToRead) {
                int read = micInputStream.read(byteBuffer, totalBytesRead, bytesToRead - totalBytesRead);
                if (read == -1) {
                    // 流结束
                    break;
                }
                totalBytesRead += read;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        int shortsRead = totalBytesRead / BYTES_PER_SAMPLE;

        // 小端转 short
        for (int i = 0; i < shortsRead; i++) {
            int low = byteBuffer[i * 2] & 0xff;
            int high = byteBuffer[i * 2 + 1];
            buffer[offset + i] = (short) ((high << 8) | low);
        }

        return shortsRead;
    }

    public int getState() {
        return mState;
    }

    public int getRecordingState() {
        return mRecordingState;
    }
}
