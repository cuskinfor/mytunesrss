package de.codewave.camel;

import de.codewave.camel.mp3.Id3Tag;
import de.codewave.camel.mp3.Id3v1Tag;
import de.codewave.camel.mp3.Id3v2Tag;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.exception.IllegalHeaderException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class Analyzer {

    public static void main(String[] args) throws IOException, IllegalHeaderException {

        File file = new File(args[0]);

        if (FilenameUtils.isExtension(file.getName().toLowerCase(), "mp3")) {
            analyzeMp3(file);
        } else {
            System.err.println("File \"" + file.getName() + "\" not supported.");
        }

    }

    private static void analyzeMp3(File file) throws IOException, IllegalHeaderException {
        Id3Tag id3Tag = Mp3Utils.readId3Tag(file);
        if (id3Tag.isId3v1()) {
            Id3v1Tag id3v1Tag = (Id3v1Tag) id3Tag;
            System.out.println("Long version = \"" + id3v1Tag.getLongVersionIdentifier() + "\"");
            System.out.println("Short version = \"" + id3v1Tag.getShortVersionIdentifier() + "\"");
            System.out.println("Title = \"" + id3v1Tag.getTitle() + "\"");
            System.out.println("Album = \"" + id3v1Tag.getAlbum() + "\"");
            System.out.println("Artist = \"" + id3v1Tag.getArtist() + "\"");
            System.out.println("Genre = \"" + id3v1Tag.getGenreAsString() + "\"");
            System.out.println("Track number = \"" + id3v1Tag.getTrackNumber() + "\"");
            System.out.println("Year = \"" + id3v1Tag.getYear() + "\"");
            System.out.println("Comment = \"" + id3v1Tag.getComment() + "\"");
        } else if (id3Tag.isId3v2()) {
            Id3v2Tag id3v2Tag = (Id3v2Tag) id3Tag;
            System.out.println("Long version = \"" + id3v2Tag.getLongVersionIdentifier() + "\"");
            System.out.println("Short version = \"" + id3v2Tag.getShortVersionIdentifier() + "\"");
            System.out.println("Title = \"" + id3v2Tag.getTitle() + "\"");
            System.out.println("Album = \"" + id3v2Tag.getAlbum() + "\"");
            System.out.println("Artist = \"" + id3v2Tag.getArtist() + "\"");
            System.out.println("Album artist = \"" + id3v2Tag.getAlbumArtist() + "\"");
            System.out.println("Genre = \"" + id3v2Tag.getGenreAsString() + "\"");
            System.out.println("Sort album = \"" + id3v2Tag.getSortAlbum() + "\"");
            System.out.println("Sort artist = \"" + id3v2Tag.getSortArtist() + "\"");
            System.out.println("Sort album artist = \"" + id3v2Tag.getSortAlbumArtist() + "\"");
            System.out.println("Track number = \"" + id3v2Tag.getTrackNumber() + "\"");
            System.out.println("Year = \"" + id3v2Tag.getYear() + "\"");
            System.out.println("Composer = \"" + id3v2Tag.getComposer() + "\"");
            System.out.println("Pos = \"" + id3v2Tag.getPos() + "\"");
            System.out.println("Time seconds = \"" + id3v2Tag.getTimeSeconds() + "\"");
        }
    }

}
