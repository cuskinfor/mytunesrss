/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.LuceneQueryParserException;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.representation.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.Version;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@ValidateRequest
@Path("/")
public class LibraryResource extends RestResource {

    /**
     * Get URIs of all kind of data in the library.
     *
     * @param uriInfo
     *
     * @return Tbe library representation which contains several URIs for further data retrieval.
     */
    @GET
    @Produces({"application/json"})
    @GZIP
    public LibraryRepresentation getLibrary(@Context UriInfo uriInfo) {
        LibraryRepresentation libraryRepresentation = new LibraryRepresentation();
        libraryRepresentation.setVersion(new VersionRepresentation(new Version(MyTunesRss.VERSION)));
        libraryRepresentation.setAlbumsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getAlbums").build());
        libraryRepresentation.setArtistsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getArtists").build());
        libraryRepresentation.setGenresUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getGenres").build());
        libraryRepresentation.setMoviesUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getMovies").build());
        libraryRepresentation.setPlaylistsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getPlaylists").build());
        libraryRepresentation.setTracksUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "findTracks").build());
        libraryRepresentation.setTvShowsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getTvShows").build());
        libraryRepresentation.setMediaPlayerUri(uriInfo.getBaseUriBuilder().path(MediaPlayerResource.class).build());
        libraryRepresentation.setSessionUri(uriInfo.getBaseUriBuilder().path(SessionResource.class).build());
        return libraryRepresentation;
    }

    /**
     * Get a list of albums according to the specified optional filter critria.
     *
     * @param filter Album name filter.
     * @param artist Artist name filter.
     * @param genre Genre filter.
     * @param index The index can be "-1" for all, "0" for albums in the section "0-9", "1" for the section "A-C",
     *              "2" for "D-F", "3" for "G-I", "4" for "J-L", "5" for "M-O", "6" for "P-S", "7" for "T-V" and
     *              "8" for "W-Z".
     * @param minYear Minimum album year filter.
     * @param maxYear Maximum album year filter.
     * @param sortYear "true" to sort albums by year or "false" to sort by name.
     * @param type Filter for album type (One of "COMPILATIONS", "ALBUMS", "ALL").
     *
     * @return A list of albums.
     *
     * @throws SQLException
     */
    @GET
    @Path("albums")
    @Produces({"application/json"})
    @GZIP
    public List<AlbumRepresentation> getAlbums(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("filter") String filter,
            @QueryParam("artist") String artist,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        DataStoreQuery.QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAlbumQuery(MyTunesRssWebUtils.getAuthUser(request), filter, artist, false, genre, index, minYear, maxYear, sortYear, type));
        return toAlbumRepresentations(uriInfo, request, queryResult.getResults());
    }

    /**
     * Get a list of artists according to the specified optional filter criteria.
     *
     * @param filter Artist name filter.
     * @param album Album name filter (i.e. only artist with matching albums are returned).
     * @param genre Genre name filter.
     * @param index The index can be "-1" for all, "0" for albums in the section "0-9", "1" for the section "A-C",
     *              "2" for "D-F", "3" for "G-I", "4" for "J-L", "5" for "M-O", "6" for "P-S", "7" for "T-V" and
     *              "8" for "W-Z".
     *
     * @return A list of artists.
     *
     * @throws SQLException
     */
    @GET
    @Path("artists")
    @Produces({"application/json"})
    @GZIP
    public List<ArtistRepresentation> getArtists(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("filter") String filter,
            @QueryParam("album") String album,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        DataStoreQuery.QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindArtistQuery(MyTunesRssWebUtils.getAuthUser(request), filter, album, genre, index));
        return toArtistRepresentations(uriInfo, request, queryResult.getResults());
    }

    /**
     * Get a list of genres according to the specified optional filter criteria.
     *
     * @param includeHidden "true" to include genres which should be hidden from user interfaces (admin setting).
     * @param index The index can be "-1" for all, "0" for albums in the section "0-9", "1" for the section "A-C",
     *              "2" for "D-F", "3" for "G-I", "4" for "J-L", "5" for "M-O", "6" for "P-S", "7" for "T-V" and
     *              "8" for "W-Z".
     *
     * @return A list of genres.
     *
     * @throws SQLException
     */
    @GET
    @Path("genres")
    @Produces({"application/json"})
    @GZIP
    public List<GenreRepresentation> getGenres(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        DataStoreQuery.QueryResult<Genre> queryResult = TransactionFilter.getTransaction().executeQuery(new FindGenreQuery(MyTunesRssWebUtils.getAuthUser(request), includeHidden, index));
        return toGenreRepresentations(uriInfo, queryResult.getResults());
    }

    /**
     * Get a list of playlists according to the specified optional filter criteria.
     *
     * @param includeHidden "true" to include playlists which should be hidden from user interfaces (admin setting).
     * @param matchingOwner "true" to return only playlists owned by the currently logged in user.
     * @param types List of playlist types to return (Possible values: "ITunes", "MyTunes", "M3uFile", "ITunesFolder", "MyTunesSmart", "Random", "System").
     * @param root "true" to return only root playlists, i.e. playlists which do not have a parent playlist (iTunes folder).
     *
     * @return A list of playlists.
     *
     * @throws SQLException
     */
    @GET
    @Path("playlists")
    @Produces({"application/json"})
    @GZIP
    public List<PlaylistRepresentation> getPlaylists(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("owner") @DefaultValue("false") boolean matchingOwner,
            @QueryParam("type") List<PlaylistType> types,
            @QueryParam("root") @DefaultValue("false") boolean root
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), types, null, root ? "ROOT": null, includeHidden, matchingOwner));
        return toPlaylistRepresentations(uriInfo, request, queryResult.getResults());
    }

    /**
     * Get a list of movies.
     *
     * @return A list of movies.
     *
     * @throws SQLException
     */
    @GET
    @Path("movies")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getMovies(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getMovies(MyTunesRssWebUtils.getAuthUser(request)));
        return toTrackRepresentations(uriInfo, request, queryResult.getResults());
    }

    /**
     * Get a list of TV shows.
     *
     * @return A list of TV shows.
     *
     * @throws SQLException
     */
    @GET
    @Path("tvshows")
    @Produces({"application/json"})
    @GZIP
    public List<TvShowRepresentation> getTvShows(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowEpisodes(MyTunesRssWebUtils.getAuthUser(request)));
        Map<String, Set<Integer>> seasonsPerShow = new HashMap<String, Set<Integer>>();
        Map<String, MutableInt> episodeCountPerShow = new HashMap<String, MutableInt>();
        for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
            if (episodeCountPerShow.containsKey(track.getSeries())) {
                episodeCountPerShow.get(track.getSeries()).increment();
                seasonsPerShow.get(track.getSeries()).add(track.getSeason());
            } else {
                episodeCountPerShow.put(track.getSeries(), new MutableInt(1));
                seasonsPerShow.put(track.getSeries(), new HashSet<Integer>(Collections.singleton(track.getSeason())));
            }
        }
        List<TvShowRepresentation> shows = new ArrayList<TvShowRepresentation>();
        for (String name : episodeCountPerShow.keySet()) {
            TvShowRepresentation representation = new TvShowRepresentation();
            representation.setName(name);
            representation.setSeasonCount(seasonsPerShow.get(name).size());
            representation.setEpisodeCount(episodeCountPerShow.get(name).intValue());
            representation.setSeasonsUri(uriInfo.getBaseUriBuilder().path(TvShowResource.class).path(TvShowResource.class, "getSeasons").build(name));
            shows.add(representation);
        }
        Collections.sort(shows);
        return shows;
    }

    /**
     * Get a list of tracks.
     *
     * @param term Search term.
     * @param expert "true" for expert search, i.e. search term can contain Lucene seatch syntax.
     * @param fuzziness Search fuzziness (see Lucene documentation where 0% fuzziness is exact search and 100% fuzziness is a similarity of 0). If the session representation has a value
     *                  for the search fuzziness (i.e. a value is configured in the user settings), this parameter is ignored and the value from the user settings is used.
     * @param maxItems Maximum number of results to return.
     * @param sortOrder Sort order of the results (One of "Album", "Artist", "KeepOrder").
     *
     * @return A list of tracks.
     *
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     * @throws LuceneQueryParserException
     */
    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> findTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("term") @NotBlank(message = "Missing search term.") String term,
            @QueryParam("expert") @DefaultValue("false") boolean expert,
            @QueryParam("fuzziness") @DefaultValue("35") @Range(min = 0, max = 100, message = "Fuzziness must a be a value from 0 to 100.") int fuzziness,
            @QueryParam("max") @DefaultValue("1000") @Range(min = 1, max = 1000, message = "Max must be a value from 1 to 1000.") int maxItems,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
            ) throws IOException, ParseException, SQLException, LuceneQueryParserException {
        DataStoreQuery.QueryResult<Track> queryResult = null;
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (expert) {
            queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForExpertSearchTerm(user, term, sortOrder, maxItems));
        } else {
            int userSearchFuzziness = user.getSearchFuzziness();
            if (userSearchFuzziness >= 0 && userSearchFuzziness <= 100) {
                fuzziness = userSearchFuzziness; // use configured value if any and ignore parameter value
            }
            queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForSearchTerm(user, term, fuzziness, sortOrder, maxItems));
        }
        return toTrackRepresentations(uriInfo, request, queryResult.getResults());
    }
}
