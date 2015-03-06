/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.GetPhotoQuery;
import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.meta.MyTunesRssExifUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.formats.tiff.TiffField;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShowExifCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowExifCommandHandler.class);

    @XmlRootElement
    public static class ExifField {
        private String myName;
        private String myValue;

        public ExifField() {
        }

        public ExifField(String name, String value) {
            myName = name;
            myValue = value;
        }

        public String getName() {
            return myName;
        }

        public String getValue() {
            return myValue;
        }
    }

    @Override
    public void executeAuthorized() throws Exception {
        if (getAuthUser().isPhotos()) {
            final String id = getRequestParameter("photo", null);
            if (StringUtils.isNotBlank(id)) {
                Photo photo = getTransaction().executeQuery(new GetPhotoQuery(id));
                File photoFile = new File(photo.getFile());
                if (photoFile.isFile()) {
                    List<ExifField> exifFieldList = new ArrayList<>();
                    for (TiffField tiffField : MyTunesRssExifUtils.getExifData(photoFile)) {
                        try {
                            if (!"Undefined".equals(tiffField.getFieldTypeName())) {
                                exifFieldList.add(new ExifField(tiffField.getTagName().trim(), tiffField.getValue().toString().trim()));
                            }
                        } catch (ImageReadException e) {
                            LOGGER.warn("Could not get EXIF field: " + e.getMessage());
                        }
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.getSerializationConfig().withAnnotationIntrospector(new JaxbAnnotationIntrospector());
                    mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
                    getResponse().setContentType("application/json");
                    mapper.writeValue(getResponse().getWriter(), exifFieldList);
                } else {
                    getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "photo file not found");
                }
            } else {
                getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "missing photo id");
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN, "unauthorized");
        }
    }
}
