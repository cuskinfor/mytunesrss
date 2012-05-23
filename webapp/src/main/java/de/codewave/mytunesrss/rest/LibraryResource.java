/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.LuceneQueryParserException;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@ValidateRequest
@Path("library")
public class LibraryResource extends RestResource {

    @GET
    @Path("albums")
    @Produces({"application/json"})
    @GZIP
    public List<Album> getAlbums(
            @QueryParam("filter") String filter,
            @QueryParam("artist") String artist,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        DataStoreQuery.QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAlbumQuery(getAuthUser(), filter, artist, genre, index, minYear, maxYear, sortYear, type));
        return queryResult.getResults();
    }

    @GET
    @Path("artists")
    @Produces({"application/json"})
    @GZIP
    public List<Artist> getArtists(
            @QueryParam("filter") String filter,
            @QueryParam("album") String album,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        DataStoreQuery.QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindArtistQuery(getAuthUser(), filter, album, genre, index));
        return queryResult.getResults();
    }

    @GET
    @Path("genres")
    @Produces({"application/json"})
    @GZIP
    public List<Genre> getGenres(
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        DataStoreQuery.QueryResult<Genre> queryResult = TransactionFilter.getTransaction().executeQuery(new FindGenreQuery(getAuthUser(), includeHidden, index));
        return queryResult.getResults();
    }

    @GET
    @Path("playlists")
    @Produces({"application/json"})
    @GZIP
    public List<Playlist> getPlaylists(
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("owner") @DefaultValue("false") boolean matchingOwner,
            @QueryParam("type") List<PlaylistType> types,
            @QueryParam("root") @DefaultValue("false") boolean root
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), types, null, root ? "ROOT": null, includeHidden, matchingOwner));
        return queryResult.getResults();
    }

    @GET
    @Path("movies")
    @Produces({"application/json"})
    @GZIP
    public List<Track> getMovies() throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getMovies(getAuthUser()));
        return queryResult.getResults();
    }

    @GET
    @Path("tvshows")
    @Produces({"application/json"})
    @GZIP
    public Map<String, Map<Integer, List<Track>>> getTvShows() throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowEpisodes(getAuthUser()));
        Map<String, Map<Integer, List<Track>>> result = new LinkedHashMap<String, Map<Integer, List<Track>>>();
        for (Track track : queryResult.getResults()) {
            if (!result.containsKey(track.getSeries())) {
                result.put(track.getSeries(), new LinkedHashMap<Integer, List<Track>>());
            }
            if (!result.get(track.getSeries()).containsKey(track.getSeason())) {
                result.get(track.getSeries()).put(track.getSeason(), new ArrayList<Track>());
            }
            result.get(track.getSeries()).get(track.getSeason()).add(track);
        }
        return result;
    }

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<Track> findTracks(
            @QueryParam("term") String term,
            @QueryParam("expert") @DefaultValue("false") boolean expert,
            @QueryParam("fuzziness") @DefaultValue("35") @Range(min = 0, max = 100, message = "Fuzziness must a be a value from 0 to 100.") int fuzziness,
            @QueryParam("max") @DefaultValue("1000") @Range(min = 1, max = 1000, message = "Max must be a value from 1 to 1000.") int maxItems,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
            ) throws IOException, ParseException, SQLException, LuceneQueryParserException {
        DataStoreQuery.QueryResult<Track> queryResult = null;
        if (expert) {
            queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForExpertSearchTerm(getAuthUser(), term, sortOrder, maxItems));
        } else {
            queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForSearchTerm(getAuthUser(), term, fuzziness, sortOrder, maxItems));
        }
        return queryResult.getResults();
    }
}
