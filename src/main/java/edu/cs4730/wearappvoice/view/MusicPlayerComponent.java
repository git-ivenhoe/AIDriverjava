package edu.cs4730.wearappvoice.view;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MusicPlayerComponent extends JPanel {
    private final JLabel songTitle;
    private final JLabel artistName;
    private final JSlider seekBar;
    private final JButton playPauseButton;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Timer seekBarTimer;

    public MusicPlayerComponent(String title, String artist, String audioFilePath) {
        new JFXPanel(); // 初始化 JavaFX 环境

        setLayout(new BorderLayout(10, 10));
        songTitle = new JLabel("Title: " + title);
        artistName = new JLabel("Artist: " + artist);
        seekBar = new JSlider();
        playPauseButton = new JButton("▶"); // 或使用 ImageIcon

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(songTitle);
        topPanel.add(artistName);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(seekBar, BorderLayout.CENTER);
        bottomPanel.add(playPauseButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        loadMedia(audioFilePath);

        playPauseButton.addActionListener(e -> {
            if (isPlaying) {
                pause();
            } else {
                play();
            }
        });

        seekBar.addChangeListener(e -> {
            if (seekBar.getValueIsAdjusting() && mediaPlayer != null) {
                double seekTime = seekBar.getValue() / 100.0 * mediaPlayer.getTotalDuration().toMillis();
                mediaPlayer.seek(javafx.util.Duration.millis(seekTime));
            }
        });
    }

    private void loadMedia(String filePath) {
        try {
            File file = new File(filePath);
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                seekBar.setMaximum(100);
            });

            mediaPlayer.setOnEndOfMedia(this::reset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void play() {
        if (mediaPlayer == null) return;
        mediaPlayer.play();
        isPlaying = true;
        playPauseButton.setText("⏸");

        seekBarTimer = new Timer(1000, e -> {
            if (mediaPlayer.getTotalDuration() != null) {
                double current = mediaPlayer.getCurrentTime().toMillis();
                double total = mediaPlayer.getTotalDuration().toMillis();
                int progress = (int) ((current / total) * 100);
                seekBar.setValue(progress);
            }
        });
        seekBarTimer.start();
    }

    private void pause() {
        if (mediaPlayer == null) return;
        mediaPlayer.pause();
        isPlaying = false;
        playPauseButton.setText("▶");

        if (seekBarTimer != null) {
            seekBarTimer.stop();
        }
    }

    private void reset() {
        isPlaying = false;
        playPauseButton.setText("▶");
        seekBar.setValue(0);
        if (seekBarTimer != null) {
            seekBarTimer.stop();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void updateData(String title, String artist, String audioFilePath) {
        songTitle.setText("Title: " + title);
        artistName.setText("Artist: " + artist);
        release();
        loadMedia(audioFilePath);
    }
}
