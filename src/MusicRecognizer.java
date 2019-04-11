import javafx.util.Pair;
import utility.MusicSegment;
import utility.Utility;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicRecognizer {
    /* This is the main music recognizer class which has the needed functions for recording the song being played,
        applying the fingerprinting algorithm and other things. */
    public static Pair<String, Integer> recognizeMusic(byte[] audio) {
        ArrayList<int[]> allChunks = Utility.extractAllMainFrequencies(Utility.performFFT(audio));
        // All the chunks of the current music being played

        HashMap<String, ArrayList<MusicSegment>> map = Utility.loadHashMap(); // load the DB
        HashMap<Pair<String, Integer>, Integer> chunkDiffPairs = new HashMap<>();
        /* This keeps the chunk matches along with their difference (offset) in the played music and the main music in the DB
            Each pair, as a key, contains the musicID and the chunkDiff, and the value is the number of repetitions of such a pair.
            Finally, the pair with the most repetitions will be chosen as the best match. */


        for (int chunkNumber = 0; chunkNumber < allChunks.size(); chunkNumber++) {
            String fingerprint = Utility.getChunkFingerprint(allChunks.get(chunkNumber));
            if (map.containsKey(fingerprint)) { // If the chunk exists in the DB
                ArrayList<MusicSegment> matchedSegments = map.get(fingerprint); // Get all the matched segments

                for (MusicSegment matchedSegment : matchedSegments) { // For each matched segment in DB
                    int chunkDiff = Math.abs(matchedSegment.getChunkNumber() - chunkNumber); // Compute the offset (chunk difference)
                    Pair<String, Integer> chunkDiffPair = new Pair<>(matchedSegment.getMusicID(), chunkDiff); // Pair of (musicId, chunkDiff)

                    boolean pairFound = false;
                    for (Pair<String, Integer> p : chunkDiffPairs.keySet()) { // For each key in the chunkDiffPairs
                        if (p.equals(chunkDiffPair)) { // If the key equals the one we are dealing with
                            chunkDiffPairs.put(p, chunkDiffPairs.get(p) + 1); // Incrementing the number of repetitions
                            pairFound = true;
                        }
                    }
                    if (!pairFound) // The pair is seen for the first time, should be added to the hash map
                         chunkDiffPairs.put(chunkDiffPair, 1);
                }
            }
        }
        Pair<String, Integer> bestMatch = findTheBestMatch(chunkDiffPairs);
        System.out.println("In [recognizeMusic]: best match musicID = " + bestMatch.getKey());
        return bestMatch;
    }

    private static Pair<String, Integer> findTheBestMatch(HashMap<Pair<String, Integer>, Integer> chunkDiffPairs) {
        int highestHit = 0;
        Pair<String, Integer> bestMatch = null;

        for (Pair<String, Integer> p : chunkDiffPairs.keySet()) {
            int repetition = chunkDiffPairs.get(p);
            if (repetition > highestHit) {
                highestHit = repetition;
                bestMatch = p;
            }
        }
        System.out.println("In [findTheBestMatch]: highestHit = '" + highestHit + "'");
        return bestMatch;
    }
}
