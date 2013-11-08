/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.meta.MyTunesRssExifUtils;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.ExifFieldRepresentation;
import de.codewave.mytunesrss.rest.representation.ExifRepresentation;
import de.codewave.mytunesrss.rest.representation.PhotoRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.formats.tiff.TiffField;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ValidateRequest
@Path("photo/{photo}")
@RequiredUserPermissions({UserPermission.Photos})
public class PhotoResource extends RestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoResource.class);

    /**
     * Get the EXIF data of a photo.
     *
     * @param photoId A photo ID.

     * @return The EXIF data.
     */
    @GET
    @Path("exif")
    @Produces({"application/json"})
    @GZIP
    public ExifRepresentation getExifData(
            @PathParam("photo") final String photoId
    ) throws SQLException {
        List<ExifFieldRepresentation> exifFieldList = new ArrayList<ExifFieldRepresentation>();
        String filename = TransactionFilter.getTransaction().executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<String>>() {
            @Override
            public QueryResult<String> execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhoto");
                statement.setString("id", photoId);
                return execute(statement, new ResultBuilder<String>() {
                    public String create(ResultSet resultSet) throws SQLException {
                        return resultSet.getString("file");
                    }
                });
            }
        }).getResult(0);
        File photoFile = new File(filename);
        if (photoFile.isFile()) {
            for (TiffField tiffField : MyTunesRssExifUtils.getExifData(photoFile)) {
                try {
                    if (!"Undefined".equals(tiffField.getFieldTypeName())) {
                        exifFieldList.add(new ExifFieldRepresentation(tiffField.getTagName().trim(), tiffField.getValue().toString().trim()));
                    }
                } catch (ImageReadException e) {
                    LOGGER.warn("Could not get EXIF field: " + e.getMessage());
                }
            }
        } else {
            throw new MyTunesRssRestException(HttpServletResponse.SC_NOT_FOUND, "FILE_NOT_FOUND");
        }
        return new ExifRepresentation(exifFieldList);
    }
}
