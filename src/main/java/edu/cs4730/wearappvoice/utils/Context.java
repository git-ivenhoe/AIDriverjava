package edu.cs4730.wearappvoice.utils;


import javax.swing.*;

public class Context {
	private final JFrame frame;

	public Context(JFrame frame) {
		this.frame = frame;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void showToast(String msg) {
		Toast.makeText(frame, msg, 2000);
	}

	public void startActivity(Intent intent) {
	}

	public void sendBroadcast(Intent broadcastIntent) {
	}
}
