package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class RecreateHelpTablesStatement implements DataStoreStatement {

    private final boolean myRecreateAlbums;
    private final boolean myRecreateArtists;
    private final boolean myRecreateGenres;

    public RecreateHelpTablesStatement(boolean recreateAlbums, boolean recreateArtists, boolean recreateGenres) {
        myRecreateAlbums = recreateAlbums;
        myRecreateArtists = recreateArtists;
        myRecreateGenres = recreateGenres;
    }

    public void execute(Connection connection) throws SQLException {

        if (myRecreateAlbums) {
            StopWatch.start("Recreating albums help table");
            try {
                SmartStatement statementAlbum = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesAlbum");
                statementAlbum.execute();
                connection.commit();
            } finally {
                StopWatch.stop();
            }
        }

        if (myRecreateArtists) {
            StopWatch.start("Recreating artists help table");
            try {
                SmartStatement statementArtist = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesArtist");
                statementArtist.execute();
                connection.commit();
            } finally {
                StopWatch.stop();
            }
        }

        if (myRecreateGenres) {
            StopWatch.start("Recreating genres help table");
            try {
                List<Genre> genres = new FindGenresQuery(null, true, -1).execute(connection).getResults();
                List<String> hiddenGenres = new ArrayList<>();
                List<String> genreNames = new ArrayList<>();
                for (Genre genre : genres) {
                    genreNames.add(genre.getName());
                    if (genre.isHidden()) {
                        hiddenGenres.add(genre.getName());
                    }
                }
                SmartStatement statementGenre = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesGenre");
                statementGenre.setObject("hidden_genres", hiddenGenres);
                statementGenre.setObject("genres", genreNames);
                statementGenre.execute();
                connection.commit();
            } finally {
                StopWatch.stop();
            }
        }

    }
}
