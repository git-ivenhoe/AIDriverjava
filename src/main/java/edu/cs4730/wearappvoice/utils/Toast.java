package edu.cs4730.wearappvoice.utils;

import javax.swing.*;
import java.awt.*;



public class Toast {

	public static int LENGTH_SHORT = 1000;
	public static int LENGTH_LONG = 2000;

	public static void makeText(JFrame parent, String msg, int durationMs) {
		JDialog dialog = new JDialog(parent, false);
		dialog.setUndecorated(true);
		dialog.setLayout(new BorderLayout());
		dialog.add(new JLabel(msg, SwingConstants.CENTER), BorderLayout.CENTER);
		dialog.setSize(200, 60);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		new Timer(durationMs, e -> dialog.dispose()).start();
	}
}
