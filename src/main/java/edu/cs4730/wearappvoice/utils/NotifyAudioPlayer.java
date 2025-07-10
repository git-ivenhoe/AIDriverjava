package edu.cs4730.wearappvoice.utils;
import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;

public class NotifyAudioPlayer {

	public interface OnAudioPlayCompleteListener {
		void onAudioPlayComplete();
	}

	private Player mp3Player;
	private Clip wavClip;

	public void playAudio(URL audioUrl, OnAudioPlayCompleteListener listener) {
		new Thread(() -> {
			try {
				if (audioUrl == null) {
					System.err.println("音频资源 URL 为 null！");
					return;
				}

				String lowerPath = audioUrl.getPath().toLowerCase();

				if (lowerPath.endsWith(".mp3")) {
					// 播放 MP3（使用 JLayer）
					if (mp3Player != null) {
						mp3Player.close();
					}
					BufferedInputStream bis = new BufferedInputStream(audioUrl.openStream());
					mp3Player = new Player(bis);
					mp3Player.play(); // 阻塞播放直到结束
					if (listener != null) {
						listener.onAudioPlayComplete();
					}

				} else if (lowerPath.endsWith(".wav")) {
					// 播放 WAV（使用 Java Clip）
					if (wavClip != null && wavClip.isRunning()) {
						wavClip.stop();
						wavClip.close();
					}
					AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioUrl);
					wavClip = AudioSystem.getClip();
					wavClip.open(audioInputStream);
					wavClip.addLineListener(event -> {
						if (event.getType() == LineEvent.Type.STOP) {
							wavClip.close();
							if (listener != null) {
								listener.onAudioPlayComplete();
							}
						}
					});
					wavClip.start();
				} else {
					System.err.println("不支持的音频格式：" + lowerPath);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void stopAudio() {
		try {
			if (mp3Player != null) {
				mp3Player.close();
				mp3Player = null;
			}
			if (wavClip != null && wavClip.isRunning()) {
				wavClip.stop();
				wavClip.close();
				wavClip = null;
			}
		} catch (Exception ignored) {}
	}

	public void release() {
		stopAudio();
	}
}
