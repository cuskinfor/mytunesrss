/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.Statements
 */
public class Statements {
    // table creation
    private Statement myCreateTableTrack;
    private Statement myCreateTablePlaylist;
    private Statement myCreateTableLinkTrackPlaylist;

    // title finders
    private Statement myFindTitleById;
    private Statement myFindTitlesByAlbum;
    private Statement myFindTitlesByArtist;
    private Statement myFindTitles;

    public Statements(Connection connection) throws SQLException {
        // table creation
        myCreateTableTrack = connection.prepareStatement("CREATE TABLE track ( id, name, artist, album, time, file )");
        myCreateTablePlaylist = connection.prepareStatement("CREATE TABLE playlist ( id, name )");
        myCreateTableLinkTrackPlaylist = connection.prepareStatement("CREATE TABLE link_track_playlist ( track_id, playlist_id )");

        // title finders
        myFindTitleById = connection.prepareStatement("SELECT id, name, artist, album, time, file FROM title WHERE id = ?");
        myFindTitles = connection.prepareStatement("SELECT id, name, artist, album, time, file FROM title WHERE name = ? OR album = ? OR artist = ?");
        myFindTitlesByAlbum = connection.prepareStatement("SELECT id, name, artist, album, time, file FROM title WHERE album = ?");
        myFindTitlesByArtist = connection.prepareStatement("SELECT id, name, artist, album, time, file FROM title WHERE artist = ?");
    }

    
}