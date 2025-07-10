package edu.cs4730.wearappvoice.rnnoise;

import java.io.Closeable;
import java.util.Arrays;

public class Denoiser implements Closeable {
  private static final int FRAME_SIZE = 480; // or 960, 根据实际库确认
  private long state;

  static {
    System.loadLibrary("rnnoise");
  }

  public Denoiser() {
    state = rnnoiseCreate();
  }

  @Override
  public void close() {
    rnnoiseDestroy(state);
  }

  public byte[] process(byte[] pcm) {
    int len = pcm.length;
    byte[] denoised = new byte[len];

    for (int i = 0; i + FRAME_SIZE * 2 <= len; i += FRAME_SIZE * 2) {
      byte[] frame = Arrays.copyOfRange(pcm, i, i + FRAME_SIZE * 2);
      byte[] out = rnnoiseProcess(state, frame);
      System.arraycopy(out, 0, denoised, i, out.length);
    }

    return denoised;
  }

  // JNI native methods
  private native long rnnoiseCreate();
  private native void rnnoiseDestroy(long state);
  private native byte[] rnnoiseProcess(long state, byte[] pcmFrame);
}
