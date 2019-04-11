package utility;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
import utility.Complex;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Utility {
    public static void writeToFile (String filename, int[]x) throws IOException{
//        code from: https://stackoverflow.com/questions/13707223/how-to-write-an-array-to-a-file-java
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < x.length; i++) {
            // Maybe:
            outputWriter.write(x[i]+"");
            // Or:
//            outputWriter.write(Integer.toString(x[i]));
            outputWriter.newLine();

        }
        outputWriter.flush();
        outputWriter.close();
    }

    public static int getHighestAmplitude(byte[] audio) throws IOException{
        int[] amplitudes = new int[audio.length];
        int maxSoFar = 0;
        for (int i = 0; i < audio.length; i++) {
            amplitudes[i] = audio[i];
            if (Math.abs(audio[i]) > maxSoFar)
                maxSoFar = audio[i];
        }
        writeToFile("amplitudes.txt", amplitudes);
        return maxSoFar;
    }

    public static Complex[][] performFFT(byte[] audio) {
        final int audioTotalSize = audio.length;
        final int chunkSize = 4096; // the chunk size in bytes
        int numOfChunks = audioTotalSize / chunkSize;
        Complex[][] fftResult = new Complex[numOfChunks][]; // The results of performing the FFT

        // For all the chunks of the data
        for (int chunkNumber = 0; chunkNumber < numOfChunks; chunkNumber++) {
            Complex[] chunkToComplexArray = new Complex[chunkSize];

            // Each byte in that chunk is converted to a complex number, and then the FFT is performed on that chunk
            for (int i = 0; i < chunkSize; i++) {
                chunkToComplexArray[i] = new Complex(audio[(chunkSize * chunkNumber) + i], 0);
            }
            fftResult[chunkNumber] = FFT.fft(chunkToComplexArray); // Performing the FFT and adding the results
        }
        System.out.println("In [performFFT]: FFT analysis done");
        return fftResult;
    }

    public static ArrayList<int[]> extractAllMainFrequencies(Complex[][] allChunks) {
        ArrayList<int[]> allMajorFreqs = new ArrayList<>();
        for (Complex[] chunkFreqs: allChunks) {
            int[] chunkMajorFreqs = extractChunkMajorFrequencies(chunkFreqs);
            allMajorFreqs.add(chunkMajorFreqs);
        }
        System.out.println("In [extractAllMainFrequencies]: all major frequencies extracted");
        return allMajorFreqs;
    }

    private static int[] extractChunkMajorFrequencies(Complex[] chunkAllFrequencies) {
         /* This extracts the frequencies with the highest amplitude in the given chunk. To keep the implementation simple,
            we have chosen to simply consider frequencies in specified intervals as they are the most common in the case of
            music. For each range, we keep the frequency with the highest amplitude as the representative of that interval,
            which will be later used as unique features for that song.
        */

        final int HIGHEST_FREQ = 300;
        final int[] FREQ_RANGE = new int[] {40, 80, 120, 180, HIGHEST_FREQ + 1};

        double[] highestMagnitudes = new double[FREQ_RANGE.length]; // Containing the highest magnitude values
        int[] highestMagnitudeFreqs = new int[FREQ_RANGE.length]; // Containing the corresponding frequencies


         for (int freq = 1; freq < HIGHEST_FREQ - 1; freq++) {
             double freqMagnitude = Math.log(chunkAllFrequencies[freq].abs() + 1);
//             Take the logarithm to make it easier to work with. Note: that 1 added is to prevent facing log(0)
             int index = getFreqRange(freq, FREQ_RANGE);

             if (freqMagnitude > highestMagnitudes[index]) { // If the magnitude is the highest than what is seen so far
                 highestMagnitudes[index] = freqMagnitude;
                 highestMagnitudeFreqs[index] = freq;
             }
         }
        return highestMagnitudeFreqs;
    }

    private static int getFreqRange(int freq, int[] freqRange) { // Returns the index of the interval to which the frequency belongs
        int idx = 0;
        while (freqRange[idx] < freq) idx++;
        return idx;
    }

    public static String getChunkFingerprint(int[] chunk) {
        // Returns the fingerprint of a given chunk by concatinating the major frequencies in that chunk
        StringBuilder stringBuilder = new StringBuilder();

        for (int freq : chunk)
//            fingerprint += freq;
            stringBuilder.append(freq);
        return stringBuilder.toString();
    }

    public static void printHashMap(HashMap<String, ArrayList<MusicSegment>> map) {
        System.out.println("\n============================ HashMap ============================");
        for (String fingerprint: map.keySet()) {
            ArrayList<MusicSegment> musicSegments = map.get(fingerprint);

            StringBuilder stringBuilder = new StringBuilder("[fingerprint=");
            stringBuilder.append(fingerprint);
            stringBuilder.append(": ");
            for (MusicSegment musicSegment : musicSegments) {
                String tempStr = "(chunkNumber=" + musicSegment.getChunkNumber()
                                                    + ",  musicID='" + musicSegment.getMusicID() + "') ";
                stringBuilder.append(tempStr);
            }
            System.out.println(stringBuilder.toString());
        }
        System.out.println("=================================================================\n");
    }

    public static void saveHashMap(HashMap<String, ArrayList<MusicSegment>> map) { // Serializing a hash map into a file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("map.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(map);

            objectOutputStream.close();
            fileOutputStream.close();
            System.out.println("In [saveHashMap]: Serialized HashMap data saved to map.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, ArrayList<MusicSegment>> loadHashMap() {
        HashMap<String, ArrayList<MusicSegment>> map = null;
        try {
            FileInputStream fileInputStream = new FileInputStream("map.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            map = (HashMap) objectInputStream.readObject();

            objectInputStream.close();
            fileInputStream.close();
            System.out.println("In [loadHashMap]: Serialized HashMap data loaded from map.ser");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void addNewSongToDB(ArrayList<int[]> allChunks, String musicID) { // allChunks: all the chunks of the given song
        HashMap<String, ArrayList<MusicSegment>> map = loadHashMap(); // Load the DB
        for (int chunkNumber = 0; chunkNumber < allChunks.size(); chunkNumber++) { // For each chunk in the music
            String fingerprint = getChunkFingerprint(allChunks.get(chunkNumber)); // Get the fingerprint of that chunk
            MusicSegment musicSegment = new MusicSegment(chunkNumber, musicID); // Create the corresponding data point

            if (!map.containsKey(fingerprint)) { // If the fingerprint does not already exist
                ArrayList<MusicSegment> musicSegments = new ArrayList<>();
                musicSegments.add(musicSegment);
                map.put(fingerprint, musicSegments); // Add it along with the music to which it corresponds
            }
            else { // If the fingerprint already exists
                ArrayList<MusicSegment> musicSegments = map.get(fingerprint); // Get the current list
                musicSegments.add(musicSegment); // Update the list
                map.put(fingerprint, musicSegments);
            }
        }
        saveHashMap(map); // Save the DB
        System.out.println("In [addNewSongToDB]: new song added");
    }
}
