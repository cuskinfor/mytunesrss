/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.PhotoRepresentation;
import de.codewave.mytunesrss.rest.representation.QueryResultIterable;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;

/**
 * Photo album operations.
 */
@ValidateRequest
@Path("photoalbum/{album}")
@RequiredUserPermissions({UserPermission.Photos})
public class PhotoAlbumResource extends RestResource {

    /**
     * Get the photos of an album.
     *
     * @param albumId   A photo album ID.
     * @param photoSize Photo size used for the image URIs.
     *
     * @return List of photos.
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.PhotoRepresentation>
     */
    @GET
    @Path("photos")
    @Produces({"application/json"})
    @GZIP
    public Iterable<PhotoRepresentation> getPhotos(
            @PathParam("album") String albumId,
            @QueryParam("size") @Range(min = 1, max = Long.MAX_VALUE, message = "Size must be a positive long value.") final Long photoSize,
            @QueryParam("first") @DefaultValue("0") @Range(min = 0, max = Integer.MAX_VALUE, message = "The first index must be 0 or a positive integer value.") int first,
            @QueryParam("count") @DefaultValue("2147483647") @Range(min = 1, max = Integer.MAX_VALUE, message = "The count must be a positive integer value") final int count,
            @Context final UriInfo uriInfo,
            @Context final HttpServletRequest request
    ) throws SQLException {
        FindPhotoQuery findPhotoQuery = FindPhotoQuery.getForAlbum(MyTunesRssWebUtils.getAuthUser(request), albumId);
        findPhotoQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Photo> photos = TransactionFilter.getTransaction().executeQuery(findPhotoQuery);
        for (int skip = 0; skip < first; skip++) {
            if (photos.nextResult() == null) {
                throw new MyTunesRssRestException(HttpServletResponse.SC_BAD_REQUEST, "FIRST_INDEX_OUT_OF_BOUNDS");
            }
        }
        return new QueryResultIterable<>(photos, new QueryResultIterable.ResultTransformer<Photo, PhotoRepresentation>() {
            @Override
            public PhotoRepresentation transform(Photo photo) {
                PhotoRepresentation photoRepresentation = new PhotoRepresentation(photo);
                if (IncludeExcludeInterceptor.isAttr("thumbnailImageUri")) {
                    if (StringUtils.isNotBlank(photo.getImageHash())) {
                        photoRepresentation.setThumbnailImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc("hash=" + photo.getImageHash())).toString());
                    } else {
                        photoRepresentation.setThumbnailImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc("photoId=" + MiscUtils.getUtf8UrlEncoded(photo.getId()))).toString());
                    }
                }
                if (IncludeExcludeInterceptor.isAttr("originalImageUri")) {
                    if (photoSize != null) {
                        photoRepresentation.setOriginalImageUri(getAppURI(request, MyTunesRssCommand.ShowPhoto, enc("photo=" + MiscUtils.getUtf8UrlEncoded(photo.getId())), enc("size=" + photoSize)).toString());
                    } else {
                        photoRepresentation.setOriginalImageUri(getAppURI(request, MyTunesRssCommand.ShowPhoto, enc("photo=" + MiscUtils.getUtf8UrlEncoded(photo.getId()))).toString());
                    }
                }
                if (IncludeExcludeInterceptor.isAttr("exifDataUri")) {
                    photoRepresentation.setExifDataUri(uriInfo.getBaseUriBuilder().path(PhotoResource.class).path(PhotoResource.class, "getExifData").build(photo.getId()).toString());
                }
                return photoRepresentation;
            }
        }, count);
    }
}
