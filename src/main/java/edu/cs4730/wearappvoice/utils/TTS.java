package edu.cs4730.wearappvoice.utils;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import javax.swing.*;
import java.util.Locale;

public class TTS {

	private Voice voice;
	private boolean isSpeaking = false;

	public TTS() {
		initTTS();
	}

	private void initTTS() {
		System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
//		System.setProperty("mbrola.base","C:\\hxf\\Huangxf\\AI\\AIDriverjava\\src\\main\\libs/mbrola");

		voice = VoiceManager.getInstance().getVoice("kevin16");

		if (voice == null) {
			JOptionPane.showMessageDialog(null, "未找到语音库 'kevin16'");
			return;
		}

		voice.allocate();
		voice.setPitch(100);       // 100 是正常值，值越高音调越尖
		voice.setRate(150);        // 每分钟单词数，默认 150
		voice.setVolume(1.0f);     // 音量，范围 0.0 - 1.0
	}

	public void startAnsyTTS(String str) {
		Constants.ttsplaying = true;
//		voice.speak(str);
		Constants.ttsplaying = false;
	}

	public void startTTS(String str) {
		Constants.ttsplaying = true;
		new Thread(() -> {
//			voice.speak(str);
			Constants.ttsplaying = false;
			edu.cs4730.wearappvoice.utils.Message msg1 = new edu.cs4730.wearappvoice.utils.Message();
			msg1.what = Constants.MESSAGE_END_TTS;
			if(Constants.mainHandle != null)
				Constants.mainHandle.sendMessage(msg1);
		}).start();
	}

	public void shutdown() {
		if (voice != null) {
			voice.deallocate();
		}
	}

	public boolean isSpeaking() {
		return isSpeaking;
	}
	public void setPitch(float pitch) {
		voice.setPitch(pitch);
	}

	public void stop() {
		// FreeTTS 无法直接中断正在发音的语音（除非使用低级线程控制）
		// 可通过改用线程 + 中断机制实现
//		JOptionPane.showMessageDialog(null, "FreeTTS 不支持主动停止语音播放");
		if(Constants.ttsplaying) {
			Constants.ttsplaying = false;
			edu.cs4730.wearappvoice.utils.Message msg1 = new edu.cs4730.wearappvoice.utils.Message();
			msg1.what = Constants.MESSAGE_END_TTS;
			if (Constants.mainHandle != null)
				Constants.mainHandle.sendMessage(msg1);
		}

//		if(tts != null)
//			tts.stop();
	}

	public void setLanguage(Locale locale) {
		// FreeTTS 仅支持英文（默认 cmu_us_kal），此处可预留切换语音模型
		JOptionPane.showMessageDialog(null, "FreeTTS 当前仅支持英文语音");
	}
	
	public void setRate(float rate) {
		voice.setRate((int) rate);
	}

	public void setVolume(float volume) {
		voice.setVolume(volume);
	}


}
