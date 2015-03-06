/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.ImageImportType;

import java.util.HashSet;
import java.util.Set;

public abstract class PhotoDatasourceConfig extends DatasourceConfig implements CommonPhotoDatasourceConfig {

    private Set<ReplacementRule> myPathReplacements = new HashSet<>();
    private ImageImportType myPhotoThumbnailImportType = ImageImportType.OnDemand;

    public PhotoDatasourceConfig(String id, String name, String definition) {
        super(id, name, definition);
    }

    public PhotoDatasourceConfig(PhotoDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<>(source.getPathReplacements());
        myPhotoThumbnailImportType = source.getPhotoThumbnailImportType();
    }

    public Set<ReplacementRule> getPathReplacements() {
        return new HashSet<>(myPathReplacements);
    }

    public void clearPathReplacements() {
        myPathReplacements.clear();
    }

    public void addPathReplacement(ReplacementRule pathReplacement) {
        myPathReplacements.add(pathReplacement);
    }

    @Override
    public ImageImportType getPhotoThumbnailImportType() {
        return myPhotoThumbnailImportType;
    }

    @Override
    public void setPhotoThumbnailImportType(ImageImportType photoThumbnailImportType) {
        myPhotoThumbnailImportType = photoThumbnailImportType;
    }

    @Override
    public boolean isUseSingleImageInFolder() {
        return false;
    }
}
