package edu.cs4730.wearappvoice.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * AudioDataSaver 类用于高可靠性地保存音频数据流到文件。
 * 支持线程安全写入，并确保每次调用都立即将数据写入文件。
 */
public class AudioDataSaver {

	private FileOutputStream outputStream;
	private final Object lock = new Object(); // 用于同步写入操作
	private boolean isInitialized = false;
	private File outputFile;

	/**
	 * 初始化接口
	 * @param fileName 保存的文件名（不包含路径，如 "audio.raw"）
	 * @return true: 初始化成功；false: 失败
	 */
	public boolean init(String fileName) {
		try {
			// 构造完整的路径：/usr/src/ + fileName
			outputFile = new File("/usr/src/" + fileName);

			// 创建文件及其父目录（若不存在）
			File parent = outputFile.getParentFile();
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					System.err.println("无法创建目录: " + parent.getAbsolutePath());
					return false;
				}
			}

			// 打开文件输出流，覆盖旧文件
			outputStream = new FileOutputStream(outputFile, true);
			isInitialized = true;
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 保存音频数据到文件（线程安全）
	 * 每次写入后强制 flush，确保数据真实写入磁盘，避免数据丢失。
	 * @param data 音频数据字节数组
	 * @param len 数据长度（从 data 数组开头开始）
	 */
	public void saveAudioData(byte[] data, int len) {
		if (!isInitialized || outputStream == null || data == null || len <= 0) return;

		synchronized (lock) {
			try {
				outputStream.write(data, 0, len);
				outputStream.flush(); // 强制写入，防止缓存丢失
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭文件接口，释放资源
	 */
	public void close() {
		synchronized (lock) {
			try {
				if (outputStream != null) {
					outputStream.flush();  // 最后再 flush 一次
					outputStream.close();
					outputStream = null;
					isInitialized = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取当前输出文件的路径（用于调试或日志）
	 * @return 完整文件路径
	 */
	public String getOutputFilePath() {
		return outputFile != null ? outputFile.getAbsolutePath() : null;
	}
}

