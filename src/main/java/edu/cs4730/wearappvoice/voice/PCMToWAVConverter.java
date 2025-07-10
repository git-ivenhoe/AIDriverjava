package edu.cs4730.wearappvoice.voice;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PCMToWAVConverter {

    public static byte[] convertPcmToWav(byte[] pcmData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeWavHeader(out, pcmData.length);

        out.write(pcmData);
        return out.toByteArray();
    }

    public static byte[] convertPcmToWav(short[] pcmData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] byteArray = toByteArray(pcmData);
        writeWavHeader(out, byteArray.length);

        out.write(byteArray);
        return out.toByteArray();
    }

    private static void writeWavHeader(ByteArrayOutputStream out, int pcmLength) throws IOException {
        int totalDataLen = pcmLength + 36;
        int sampleRate = 16000; // 16kHz
        int channels = 1; // Mono
        int byteRate = sampleRate * channels * 2; // 16-bit audio

        out.write(new byte[]{
                'R', 'I', 'F', 'F', // ChunkID
                (byte) (totalDataLen & 0xff),
                (byte) ((totalDataLen >> 8) & 0xff),
                (byte) ((totalDataLen >> 16) & 0xff),
                (byte) ((totalDataLen >> 24) & 0xff),
                'W', 'A', 'V', 'E', // Format
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                (byte) channels, 0, // NumChannels
                (byte) (sampleRate & 0xff),
                (byte) ((sampleRate >> 8) & 0xff),
                (byte) ((sampleRate >> 16) & 0xff),
                (byte) ((sampleRate >> 24) & 0xff),
                (byte) (byteRate & 0xff),
                (byte) ((byteRate >> 8) & 0xff),
                (byte) ((byteRate >> 16) & 0xff),
                (byte) ((byteRate >> 24) & 0xff),
                (byte) (2 * channels), 0, // BlockAlign
                16, 0, // BitsPerSample
                'd', 'a', 't', 'a', // Subchunk2ID
                (byte) (pcmLength & 0xff),
                (byte) ((pcmLength >> 8) & 0xff),
                (byte) ((pcmLength >> 16) & 0xff),
                (byte) ((pcmLength >> 24) & 0xff)
        });
    }

    public static float[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        float[] dest = new float[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }

    public static byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count * 2];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i] >> 8);
            dest[i * 2 + 1] = (byte) (src[i] & 0xff);
        }
        return dest;
    }
}
