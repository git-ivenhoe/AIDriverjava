package edu.cs4730.wearappvoice.utils;
import java.util.Arrays;
import java.util.LinkedList;

public class SilenceDetector {

    private final int sampleRate;
    private final int frameSizeBytes;
    private final int windowSize; // 滑动窗长度（帧数）
    private final double rmsThreshold; // 判断静音的 RMS 门限
    private final double silentFrameRatio; // 静音帧比例阈值
    private final int medianWindowSize; // 中值滤波窗口大小

    private final LinkedList<Boolean> slidingWindow = new LinkedList<>();

    /**
     * 构造函数
     * @param sampleRate 音频采样率（如16000）
     * @param frameMs 每帧多少毫秒（如20）
     * @param windowSize 滑动窗帧数（如50）
     * @param rmsThreshold RMS静音门限（如500）
     * @param silentFrameRatio 静音比例阈值（如0.8）
     * @param medianWindowSize 中值滤波窗口大小（推荐3）
     */
    public SilenceDetector(int sampleRate, int frameMs, int windowSize,
                           double rmsThreshold, double silentFrameRatio, int medianWindowSize) {
        this.sampleRate = sampleRate;
        this.frameSizeBytes = sampleRate * frameMs / 1000 * 2; // 每帧字节数（16位单通道）
        this.windowSize = windowSize;
        this.rmsThreshold = rmsThreshold;
        this.silentFrameRatio = silentFrameRatio;
        this.medianWindowSize = medianWindowSize;
    }

    /**
     * 判断一帧音频是否为静音
     * 帧数据为 16-bit PCM，单声道
     */
    private boolean isSilentFrame(byte[] buffer, int offset) {
        int sampleCount = frameSizeBytes / 2;
        short[] samples = new short[sampleCount];

        // 将 byte 转为 short 样本
        for (int i = 0; i < sampleCount; i++) {
            int index = offset + i * 2;
            samples[i] = (short) ((buffer[index + 1] << 8) | (buffer[index] & 0xFF));
        }

        // 对样本应用中值滤波
        short[] filteredSamples = applyMedianFilter(samples, medianWindowSize);

        // 计算 RMS
        double sum = 0;
        for (short s : filteredSamples) {
            sum += s * s;
        }
        double rms = Math.sqrt(sum / filteredSamples.length);
        return rms < rmsThreshold;
    }

    /**
     * 对 short[] 应用中值滤波
     */
    private short[] applyMedianFilter(short[] samples, int windowSize) {
        short[] result = new short[samples.length];
        for (int i = 0; i < samples.length; i++) {
            int start = Math.max(0, i - windowSize / 2);
            int end = Math.min(samples.length, i + windowSize / 2 + 1);
            short[] window = Arrays.copyOfRange(samples, start, end);
            Arrays.sort(window);
            result[i] = window[window.length / 2]; // 中值
        }
        return result;
    }

    /**
     * 提供给调用方的主接口：判断当前帧是否处于“静音段”
     * @param frameBuffer 单帧音频数据（长度必须等于 frameSizeBytes）
     * @return 当前是否进入“静音段”
     */
    public boolean processFrame(byte[] frameBuffer) {
        if (frameBuffer.length != frameSizeBytes) return false;

        boolean silent = isSilentFrame(frameBuffer, 0);

        slidingWindow.add(silent);
        if (slidingWindow.size() > windowSize) {
            slidingWindow.poll(); // 超出滑动窗，移除最早帧
        }

        long silentCount = slidingWindow.stream().filter(b -> b).count();
        return silentCount >= windowSize * silentFrameRatio;
    }
}
