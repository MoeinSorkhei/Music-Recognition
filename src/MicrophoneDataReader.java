import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MicrophoneDataReader {
    private boolean running;
    private TargetDataLine targetDataLine;
    private double numberOfSeconds;
    private byte[] recordedAudio;

    public  MicrophoneDataReader(TargetDataLine targetDataLine, double numberOfSeconds) {
        this.running = true;
        this.targetDataLine = targetDataLine;
        this.numberOfSeconds = numberOfSeconds;
        this.recordedAudio = null;
    }

    public byte[] getRecordedAudio() {
        return this.recordedAudio;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private byte[] readBytes() {
        OutputStream out = new ByteArrayOutputStream();
        byte[] bytesToRead = new byte[1000]; // The byte into which the data is read
        running = true;

        try {
            int count = targetDataLine.read(bytesToRead, 0, bytesToRead.length);
            if (count > 0) {
                out.write(bytesToRead, 0, count);
            }
            out.close();
        } catch (IOException e) {
            System.err.println("In [readData]: I/O problems: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
        return bytesToRead;
    }


    private ByteArrayOutputStream readData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long start = System.currentTimeMillis();
        while (true) {
            byte[] bytesRead = readBytes();
            out.write(bytesRead, 0, bytesRead.length);
            long end = System.currentTimeMillis();
            double timeDifference = (end - start) / 1000.;

            if (timeDifference > numberOfSeconds) { // Simply recording the sound for this number of seconds
                running = false;
                break;
            }
        }
        return out;
    }

    public byte[] recordAudio() {
        this.running = true;
        ByteArrayOutputStream out = readData();
        byte[] audio = out.toByteArray();
        this.recordedAudio = audio;
        this.running = false;
        return audio;
    }
}
