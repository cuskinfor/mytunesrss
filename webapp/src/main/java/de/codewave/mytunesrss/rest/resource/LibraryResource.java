/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.LuceneQueryParserException;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.representation.AlbumRepresentation;
import de.codewave.mytunesrss.rest.representation.ArtistRepresentation;
import de.codewave.mytunesrss.rest.representation.LibraryRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@ValidateRequest
@Path("library")
public class LibraryResource extends RestResource {

    @GET
    @Produces({"application/json"})
    @GZIP
    public LibraryRepresentation getLibrary(@Context UriInfo uriInfo) {
        LibraryRepresentation libraryRepresentation = new LibraryRepresentation();
        libraryRepresentation.setVersion(MyTunesRss.VERSION);
        libraryRepresentation.getUri().put("albums", uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getAlbums").build());
        libraryRepresentation.getUri().put("artists", uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getArtists").build());
        libraryRepresentation.getUri().put("genres", uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getGenres").build());
        libraryRepresentation.getUri().put("movies", uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getMovies").build());
        libraryRepresentation.getUri().put("playlists", uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getPlaylists").build());
        libraryRepresentation.getUri().put("tracks", uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "findTracks").build());
        libraryRepresentation.getUri().put("tvShows", uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getTvShows").build());
        return libraryRepresentation;
    }

    @GET
    @Path("albums")
    @Produces({"application/json"})
    @GZIP
    public List<AlbumRepresentation> getAlbums(
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
        return toAlbumRepresentation(queryResult.getResults());
    }

    @GET
    @Path("artists")
    @Produces({"application/json"})
    @GZIP
    public List<ArtistRepresentation> getArtists(
            @QueryParam("filter") String filter,
            @QueryParam("album") String album,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        DataStoreQuery.QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindArtistQuery(getAuthUser(), filter, album, genre, index));
        return toArtistRepresentation(queryResult.getResults());
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
