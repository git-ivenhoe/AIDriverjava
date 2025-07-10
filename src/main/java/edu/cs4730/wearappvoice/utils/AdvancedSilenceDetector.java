package edu.cs4730.wearappvoice.utils;

import java.util.Arrays;
import java.util.LinkedList;

public class AdvancedSilenceDetector {

    private final int frameSizeBytes;      // 每帧字节数（200ms = 6400字节）
    private final int sampleCount;         // 每帧样本数（3200）
    private final int spikeThreshold = 500; // 小波突变判断门限
    private final int maxSpikeCount = 1000; // 超过该值则认为非静音
    private final int windowSize;           // 滑动窗帧数
    private final double silenceRatio;      // 窗内静音比例判定阈值
    private double avgSilentRms = 400.0;    // 初始背景噪声 RMS 值

    private final LinkedList<Boolean> window = new LinkedList<>();

    public AdvancedSilenceDetector() {
        this.frameSizeBytes = 6400;
        this.sampleCount = 3200;
        this.windowSize = 10;       // 最近10帧（2秒）
        this.silenceRatio = 0.8;    // 80%为静音帧则整体认为静音
    }

    /**
     * 主入口：处理一帧 PCM 数据（16bit，单声道，6400字节）
     */
    public boolean processFrame(byte[] frameBuffer) {
        if (frameBuffer.length != frameSizeBytes) return false;

        short[] samples = new short[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            int low = frameBuffer[i * 2] & 0xFF;
            int high = frameBuffer[i * 2 + 1];
            samples[i] = (short) ((high << 8) | low);
        }

        double rms = calculateRMS(samples);
        int spikeCount = calculateSpikeCount(samples);

        // 动态更新背景 RMS（仅在当前帧判为静音时更新）
        boolean isSilentFrame = rms < (avgSilentRms * 1.5) && spikeCount < maxSpikeCount;
        if (isSilentFrame) {
            avgSilentRms = (avgSilentRms * 9 + rms) / 10; // 滑动平均
        }

        // 滑动窗处理
        window.add(isSilentFrame);
        if (window.size() > windowSize) {
            window.poll();
        }

        long silentCount = window.stream().filter(b -> b).count();
        return silentCount >= windowSize * silenceRatio;
    }

    private double calculateRMS(short[] samples) {
        double sum = 0;
        for (short s : samples) {
            sum += s * s;
        }
        return Math.sqrt(sum / samples.length);
    }

    private int calculateSpikeCount(short[] samples) {
        int spikes = 0;
        for (int i = 1; i < samples.length; i++) {
            int diff = Math.abs(samples[i] - samples[i - 1]);
            if (diff > spikeThreshold) {
                spikes++;
            }
        }
        return spikes;
    }
}
