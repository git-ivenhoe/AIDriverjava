package edu.cs4730.wearappvoice.utils;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ColorfulWaveformView extends JPanel {
    private final Color[] colors;
    private float[] amplitudes;
    private float maxAmplitude = Float.MIN_VALUE;
    private float minAmplitude = Float.MAX_VALUE;
    private final int numOfLines = 10;
    private final Random random = new Random();

    public ColorfulWaveformView() {
        colors = new Color[numOfLines];
        for (int i = 0; i < numOfLines; i++) {
            colors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        setPreferredSize(new Dimension(400, 200));
        setBackground(Color.BLACK);
    }

    public void setAmplitudes(float[] amplitudes) {
        this.amplitudes = amplitudes;
        updateAmplitudeRange();
        repaint();
    }

    private void updateAmplitudeRange() {
        if (amplitudes == null || amplitudes.length == 0) return;
        maxAmplitude = Float.MIN_VALUE;
        minAmplitude = Float.MAX_VALUE;
        for (float amplitude : amplitudes) {
            if (amplitude > maxAmplitude) maxAmplitude = amplitude;
            if (amplitude < minAmplitude) minAmplitude = amplitude;
        }
        if (maxAmplitude == minAmplitude) {
            maxAmplitude += 1; // 防止除以0
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (amplitudes == null || amplitudes.length < 2) return;

        Graphics2D g2d = (Graphics2D) g.create();
        int width = getWidth();
        int height = getHeight();
        float centerY = height / 2f;
        int step = amplitudes.length / numOfLines;

        for (int j = 0; j < numOfLines - 1; j++) {
            g2d.setColor(colors[j % colors.length]);
            for (int i = j * step; i < (j + 1) * step - 1 && i + 1 < amplitudes.length; i++) {
                float x1 = (float)(width * (i - j * step)) / step;
                float y1 = centerY - (amplitudes[i] - minAmplitude) * centerY / (maxAmplitude - minAmplitude);
                float x2 = (float)(width * (i - j * step + 1)) / step;
                float y2 = centerY - (amplitudes[i + 1] - minAmplitude) * centerY / (maxAmplitude - minAmplitude);
                g2d.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
            }
        }

        g2d.dispose();
    }
}
