package utility;

import java.io.Serializable;

public class MusicSegment implements Serializable {
    // This class contains the information regarding one segment (chunk) of the music
    private int chunkNumber;
    private String musicID;

    public MusicSegment(int chunkNumber, String musicID) {
        this.chunkNumber = chunkNumber;
        this.musicID = musicID;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public String getMusicID() {
        return musicID;
    }

    public void setMusicID(String musicID) {
        this.musicID = musicID;
    }

    @Override
    public String toString() {
        String str1 = "(chunkNumber=" + chunkNumber;
        String str2 = "musicID=" + musicID;
        return str1 + ", " + str2 + ")";
    }
}
