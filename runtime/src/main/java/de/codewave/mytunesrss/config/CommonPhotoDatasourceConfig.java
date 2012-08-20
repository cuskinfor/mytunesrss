/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.ImageImportType;

public interface CommonPhotoDatasourceConfig {
    ImageImportType getPhotoThumbnailImportType();

    void setPhotoThumbnailImportType(ImageImportType photoThumbnailImportType);
}
