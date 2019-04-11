import utility.Complex;
import utility.MusicSegment;
import utility.Utility;

import javax.rmi.CORBA.Util;
import javax.sound.sampled.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//import static utility.Utility.fft;
import static utility.Utility.*;


public class Main {
    public static void main(String[] args) throws LineUnavailableException, IOException {
        // Our preferred setting for the data we record
        float sampleRate = 44100; // The needed frequency for sampling
        int sampleSizeBits = 8; // The size of each sample to which we apply the domain conversion and information extraction
        int numOfChannels = 1; // Determines if it is mon or stereo
        boolean isSigned = true; // Determines if we are dealing with singed or unsigned numbers
        boolean isBigEndian = true; // Indicates that the data is stored in big endian or little endian order

        final AudioFormat format = new AudioFormat(sampleRate, sampleSizeBits, numOfChannels, isSigned, isBigEndian);
        System.out.println("In [main]: AudioFormat with the desired setting created");
//        final AudioFormat format = getAudioFormat();


        // Creating the data line for recording audio
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        targetDataLine.open(format);
        targetDataLine.start();
        System.out.println("In [main]: TargetDataLine set");

//        Utility.printHashMap(Utility.loadHashMap());
//        final double numSeconds = 30;
        final double numSeconds = 30;
        MicrophoneDataReader microphoneDataReader = new MicrophoneDataReader(targetDataLine, numSeconds);

        System.out.println("In [main]: start recording audio");
        byte[] audio = microphoneDataReader.recordAudio();
        System.out.println("in [main]: recorded audio with length in bytes: " + audio.length);



        int maxAmplitude = Utility.getHighestAmplitude(audio);
        System.out.println("in [main]: Highest Amplitude:" + maxAmplitude);
        // String musicID = "Thunder and Lightning";

       ArrayList<int[]> allChunks = Utility.extractAllMainFrequencies(performFFT(audio));
       Utility.addNewSongToDB(allChunks, musicID);
       System.out.println("in [main]: new song added to the DB");

       System.out.println("In [main]: recognizing the music");
       MusicRecognizer.recognizeMusic(audio);
    }
}
