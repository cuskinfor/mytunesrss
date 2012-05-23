/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.sql.SQLException;
import java.util.List;

@ValidateRequest
@Path("track/{track}")
public class TrackResource extends RestResource {

    @GET
    @Path("uri/download")
    @Produces({"text/plain"})
    public String getDownloadUri(
            @PathParam("track") String track,
            @QueryParam("transcoder") List<String> transcoders,
            @Context HttpServletRequest request
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForIds(new String[] {track}));
        return MyTunesFunctions.downloadUrl(request, queryResult.getResult(0), getExtraPathInfo(transcoders));
    }

    private String getExtraPathInfo(List<String> transcoders) {
        if (transcoders != null && !transcoders.isEmpty()) {
            return "tc=" + MiscUtils.getUtf8UrlEncoded(MyTunesRssWebUtils.createTranscodingParamValue(transcoders.toArray(new String[transcoders.size()])));
        } else {
            return null;
        }
    }

    @GET
    @Path("uri/playback")
    @Produces({"text/plain"})
    public String getPlaybackUri(
            @PathParam("track") String track,
            @QueryParam("transcoder") List<String> transcoders,
            @Context HttpServletRequest request
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForIds(new String[] {track}));
        return MyTunesFunctions.playbackUrl(request, queryResult.getResult(0), getExtraPathInfo(transcoders));
    }

    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("track") String track
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForTrackQuery(track));
        return queryResult.getResults();
    }

    @POST
    @Path("tags")
    @Consumes("application/x-www-form-urlencoded")
    public void getTags(
            @PathParam("track") String track,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(new String[] {track}, tag));
        }
    }

    @DELETE
    @Path("tags")
    @Consumes("application/x-www-form-urlencoded")
    public void deleteTags(
            @PathParam("track") String track,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(new String[] {track}, tag));
        }
    }
}
