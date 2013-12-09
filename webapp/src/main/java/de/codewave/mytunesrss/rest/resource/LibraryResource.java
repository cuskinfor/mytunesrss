/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.lucene.LuceneQueryParserException;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.Version;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
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
        if (IncludeExcludeInterceptor.isAttr("version")) {
            libraryRepresentation.setVersion(new VersionRepresentation(new Version(MyTunesRss.VERSION)));
        }
        if (IncludeExcludeInterceptor.isAttr("albumsUri")) {
            libraryRepresentation.setAlbumsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getAlbums").build());
        }
        if (IncludeExcludeInterceptor.isAttr("artistsUri")) {
            libraryRepresentation.setArtistsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getArtists").build());
        }
        if (IncludeExcludeInterceptor.isAttr("genresUri")) {
            libraryRepresentation.setGenresUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getGenres").build());
        }
        if (IncludeExcludeInterceptor.isAttr("moviesUri")) {
            libraryRepresentation.setMoviesUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getMovies").build());
        }
        if (IncludeExcludeInterceptor.isAttr("playlistsUri")) {
            libraryRepresentation.setPlaylistsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getPlaylists").build());
        }
        if (IncludeExcludeInterceptor.isAttr("tracksUri")) {
            libraryRepresentation.setTracksUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "findTracks").build());
        }
        if (IncludeExcludeInterceptor.isAttr("tvShowsUri")) {
            libraryRepresentation.setTvShowsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getTvShows").build());
        }
        if (IncludeExcludeInterceptor.isAttr("photoAlbumsUri")) {
            libraryRepresentation.setPhotoAlbumsUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).path(LibraryResource.class, "getPhotoAlbums").build());
        }
        if (IncludeExcludeInterceptor.isAttr("mediaPlayerUri")) {
            libraryRepresentation.setMediaPlayerUri(uriInfo.getBaseUriBuilder().path(MediaPlayerResource.class).build());
        }
        if (IncludeExcludeInterceptor.isAttr("sessionUri")) {
            libraryRepresentation.setSessionUri(uriInfo.getBaseUriBuilder().path(SessionResource.class).build());
        }
        return libraryRepresentation;
    }

    /**
     * Get a list of albums according to the specified optional filter critria.
     *
     * @param filter Album name filter.
     * @param artist Artist name filter.
     * @param genres Genre filter, can be specified multiple times.
     * @param index The index can be "-1" for all, "0" for albums in the section "0-9", "1" for the section "A-C",
     *              "2" for "D-F", "3" for "G-I", "4" for "J-L", "5" for "M-O", "6" for "P-S", "7" for "T-V" and
     *              "8" for "W-Z".
     * @param minYear Minimum album year filter.
     * @param maxYear Maximum album year filter.
     * @param sortYear "true" to sort albums by year or "false" to sort by name.
     * @param groupByType "true" to return normal albums before compilations or "false" to mix both types.
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
    @RequiredUserPermissions({UserPermission.Audio})
    public Iterable<AlbumRepresentation> getAlbums(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("filter") String filter,
            @QueryParam("artist") String artist,
            @QueryParam("genre") String[] genres,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("groupByType") @DefaultValue("false") boolean groupByType,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        FindAlbumQuery findAlbumQuery = new FindAlbumQuery(MyTunesRssWebUtils.getAuthUser(request), filter, artist, false, genres, index, minYear, maxYear, sortYear, groupByType, type);
        findAlbumQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        DataStoreQuery.QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(findAlbumQuery);
        return toAlbumRepresentations(uriInfo, request, queryResult);
    }

    /**
     * Get a list of artists according to the specified optional filter criteria.
     *
     * @param filter Artist name filter.
     * @param album Album name filter (i.e. only artist with matching albums are returned).
     * @param genres Genre name filter, can be specified multiple times.
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
    @RequiredUserPermissions({UserPermission.Audio})
    public Iterable<ArtistRepresentation> getArtists(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("filter") String filter,
            @QueryParam("album") String album,
            @QueryParam("genre") String[] genres,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        FindArtistQuery findArtistQuery = new FindArtistQuery(MyTunesRssWebUtils.getAuthUser(request), filter, album, genres, index);
        DataStoreQuery.QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(findArtistQuery);
        findArtistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        return toArtistRepresentations(uriInfo, request, queryResult);
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
    @RequiredUserPermissions({UserPermission.Audio})
    public Iterable<GenreRepresentation> getGenres(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        FindGenreQuery findGenreQuery = new FindGenreQuery(MyTunesRssWebUtils.getAuthUser(request), includeHidden, index);
        findGenreQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        DataStoreQuery.QueryResult<Genre> queryResult = TransactionFilter.getTransaction().executeQuery(findGenreQuery);
        return toGenreRepresentations(uriInfo, queryResult);
    }

    /**
     * Get a list of playlists according to the specified optional filter criteria.
     *
     * @param includeHidden "true" to include playlists which should be hidden from user interfaces (admin setting).
     * @param matchingOwner "true" to return only playlists owned by the currently logged in user.
     * @param types List of playlist types to return (Possible values: "ITunes", "MyTunes", "M3uFile", "ITunesFolder", "MyTunesSmart", "System").
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
    @RequiredUserPermissions({UserPermission.Audio, UserPermission.Playlist})
    public Iterable<PlaylistRepresentation> getPlaylists(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("owner") @DefaultValue("false") boolean matchingOwner,
            @QueryParam("type") List<PlaylistType> types,
            @QueryParam("root") @DefaultValue("false") boolean root
    ) throws SQLException {
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), types, null, root ? "ROOT" : null, includeHidden, matchingOwner);
        findPlaylistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(findPlaylistQuery);
        return toPlaylistRepresentations(uriInfo, request, queryResult);
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
    @RequiredUserPermissions({UserPermission.Video})
    public Iterable<TrackRepresentation> getMovies(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) throws SQLException {
        FindTrackQuery findTrackQuery = FindTrackQuery.getMovies(MyTunesRssWebUtils.getAuthUser(request));
        findTrackQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(findTrackQuery);
        return toTrackRepresentations(uriInfo, request, queryResult);
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
    @RequiredUserPermissions({UserPermission.Video})
    public List<TvShowRepresentation> getTvShows(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowEpisodes(MyTunesRssWebUtils.getAuthUser(request)));
        Map<String, Set<Integer>> seasonsPerShow = new HashMap<String, Set<Integer>>();
        Map<String, MutableInt> episodeCountPerShow = new HashMap<String, MutableInt>();
        Map<String, String> imageHashPerShow = new HashMap<String, String>();
        for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
            if (!imageHashPerShow.containsKey(track.getSeries())) {
                imageHashPerShow.put(track.getSeries(), track.getImageHash());
            }
            if (episodeCountPerShow.containsKey(track.getSeries())) {
                episodeCountPerShow.get(track.getSeries()).increment();
                seasonsPerShow.get(track.getSeries()).add(track.getSeason());
            } else {
                episodeCountPerShow.put(track.getSeries(), new MutableInt(1));
                seasonsPerShow.put(track.getSeries(), new HashSet<Integer>(Collections.singleton(track.getSeason())));
            }
        }
        List<TvShowRepresentation> shows = new ArrayList<TvShowRepresentation>();
        for (Map.Entry<String, MutableInt> entry : episodeCountPerShow.entrySet()) {
            TvShowRepresentation representation = new TvShowRepresentation();
            String name = entry.getKey();
            if (IncludeExcludeInterceptor.isAttr("name")) {
                representation.setName(name);
            }
            if (IncludeExcludeInterceptor.isAttr("seasonCount")) {
                representation.setSeasonCount(seasonsPerShow.get(name).size());
            }
            if (IncludeExcludeInterceptor.isAttr("episodeCount")) {
                representation.setEpisodeCount(entry.getValue().intValue());
            }
            if (IncludeExcludeInterceptor.isAttr("seasonsUri")) {
                representation.setSeasonsUri(uriInfo.getBaseUriBuilder().path(TvShowResource.class).path(TvShowResource.class, "getSeasons").build(name));
            }
            if (imageHashPerShow.containsKey(name)) {
                if (IncludeExcludeInterceptor.isAttr("imageHash")) {
                    representation.setImageHash(StringUtils.trimToNull(imageHashPerShow.get(name)));
                }
                if (IncludeExcludeInterceptor.isAttr("imageUri")) {
                    representation.setImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc(request, "hash=" + imageHashPerShow.get(name))));
                }
            }
            shows.add(representation);
        }
        Collections.sort(shows);
        return shows;
    }

    /**
     * Get a list of photo albums.

     * @return A list of photo albums.
     */
    @GET
    @Path("photoalbums")
    @Produces({"application/json"})
    @GZIP
    @RequiredUserPermissions({UserPermission.Photos})
    public Iterable<PhotoAlbumRepresentation> getPhotoAlbums(
            @Context final UriInfo uriInfo,
            @Context HttpServletRequest request
    ) throws SQLException {
        GetPhotoAlbumsQuery photoAlbumsQuery = new GetPhotoAlbumsQuery(MyTunesRssWebUtils.getAuthUser(request));
        photoAlbumsQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        DataStoreQuery.QueryResult<PhotoAlbum> queryResult = TransactionFilter.getTransaction().executeQuery(photoAlbumsQuery);
        return new QueryResultIterable<PhotoAlbum, PhotoAlbumRepresentation>(queryResult, new QueryResultIterable.ResultTransformer<PhotoAlbum, PhotoAlbumRepresentation>() {
            public PhotoAlbumRepresentation transform(PhotoAlbum photoAlbum) {
                PhotoAlbumRepresentation photoAlbumRepresentation = new PhotoAlbumRepresentation(photoAlbum);
                if (IncludeExcludeInterceptor.isAttr("photosUri")) {
                    photoAlbumRepresentation.setPhotosUri(uriInfo.getBaseUriBuilder().path(PhotoAlbumResource.class).path(PhotoAlbumResource.class, "getPhotos").build(photoAlbum.getId()));
                }
                return photoAlbumRepresentation;
            }
        });
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
    @RequiredUserPermissions({UserPermission.Audio})
    public Iterable<TrackRepresentation> findTracks(
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
            FindTrackQuery findTrackQuery = FindTrackQuery.getForExpertSearchTerm(user, term, sortOrder, maxItems);
            findTrackQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
            queryResult = TransactionFilter.getTransaction().executeQuery(findTrackQuery);
        } else {
            int userSearchFuzziness = user.getSearchFuzziness();
            if (userSearchFuzziness >= 0 && userSearchFuzziness <= 100) {
                fuzziness = userSearchFuzziness; // use configured value if any and ignore parameter value
            }
            FindTrackQuery findTrackQuery = FindTrackQuery.getForSearchTerm(user, term, fuzziness, sortOrder, maxItems);
            findTrackQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
            queryResult = TransactionFilter.getTransaction().executeQuery(findTrackQuery);
        }
        return toTrackRepresentations(uriInfo, request, queryResult);
    }
}
