/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.representation.PhotoRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ValidateRequest
@Path("photoalbum/{album}")
public class PhotoAlbumResource extends RestResource {

    /**
     * Get the photos of an album.
     *
     * @param albumId A photo album ID.
     * @param photoSize Photo size used for the image URIs.

     * @return List of photos.
     */
    @GET
    @Path("photos")
    @Produces({"application/json"})
    @GZIP
    public List<PhotoRepresentation> getPhotos(
            @PathParam("album") String albumId,
            @QueryParam("size") @Range(min = 1, max = Long.MAX_VALUE, message = "Size must be a positive long value.") Long photoSize,
            @QueryParam("first") @DefaultValue("0") @Range(min = 0, max = Integer.MAX_VALUE, message = "The first index must be 0 or a positive integer value.") int first,
            @QueryParam("count") @DefaultValue("2147483647") @Range(min = 1, max = Integer.MAX_VALUE, message = "The count must be a positive integer value") int count,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) throws SQLException {
        DataStoreQuery.QueryResult<Photo> photos = TransactionFilter.getTransaction().executeQuery(FindPhotoQuery.getForAlbum(MyTunesRssWebUtils.getAuthUser(request), albumId));
        List<PhotoRepresentation> results = new ArrayList<PhotoRepresentation>();
        if (first >= photos.getResultSize()) {
            throw new MyTunesRssRestException(HttpServletResponse.SC_BAD_REQUEST, "FIRST_INDEX_OUT_OF_BOUNDS");
        }
        for (Photo photo : photos.getResults(first, count)) {
            PhotoRepresentation photoRepresentation = new PhotoRepresentation(photo);
            photoRepresentation.setThumbnailImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, "hash=" + photo.getImageHash(), "photoId=" + photo.getId()));
            if (photoSize != null) {
                photoRepresentation.setOriginalImageUri(getAppURI(request, MyTunesRssCommand.ShowPhoto, "photo=" + photo.getId(), "size=" + photoSize));
            } else {
                photoRepresentation.setOriginalImageUri(getAppURI(request, MyTunesRssCommand.ShowPhoto, "photo=" + photo.getId()));
            }
            photoRepresentation.setExifDataUri(uriInfo.getBaseUriBuilder().path(PhotoResource.class).path(PhotoResource.class, "getExifData").build(photo.getId()));
            results.add(photoRepresentation);
        }
        return results;
    }
}
