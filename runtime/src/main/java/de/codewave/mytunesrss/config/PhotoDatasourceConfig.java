/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.ImageImportType;

import java.util.*;

public abstract class PhotoDatasourceConfig extends DatasourceConfig implements CommonPhotoDatasourceConfig {

    private Set<ReplacementRule> myPathReplacements = new HashSet<ReplacementRule>();
    private ImageImportType myPhotoThumbnailImportType = ImageImportType.OnDemand;

    public PhotoDatasourceConfig(String id, String name, String definition) {
        super(id, name, definition);
    }

    public PhotoDatasourceConfig(PhotoDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<ReplacementRule>(source.getPathReplacements());
        myPhotoThumbnailImportType = source.getPhotoThumbnailImportType();
    }

    public Set<ReplacementRule> getPathReplacements() {
        return new HashSet<ReplacementRule>(myPathReplacements);
    }

    public void clearPathReplacements() {
        myPathReplacements.clear();
    }

    public void addPathReplacement(ReplacementRule pathReplacement) {
        myPathReplacements.add(pathReplacement);
    }

    public ImageImportType getPhotoThumbnailImportType() {
        return myPhotoThumbnailImportType;
    }

    public void setPhotoThumbnailImportType(ImageImportType photoThumbnailImportType) {
        myPhotoThumbnailImportType = photoThumbnailImportType;
    }

    public List<FileType> getDefaultFileTypes() {
        List<FileType> types = new ArrayList<FileType>();
        types.add(new FileType(true, "mp4", "video/x-mp4", MediaType.Video, false));
        types.add(new FileType(true, "avi", "video/x-msvideo", MediaType.Video, false));
        types.add(new FileType(true, "mov", "video/quicktime", MediaType.Video, false));
        types.add(new FileType(true, "wmv", "video/x-ms-wmv", MediaType.Video, false));
        types.add(new FileType(true, "wma", "audio/x-ms-wma", MediaType.Audio, false));
        types.add(new FileType(true, "mpg", "audio/mpeg", MediaType.Audio, false));
        types.add(new FileType(true, "mpeg", "audio/mpeg", MediaType.Audio, false));
        types.add(new FileType(true, "m4v", "video/x-m4v", MediaType.Video, false));
        types.add(new FileType(true, "jpg", "image/jpeg", MediaType.Image, false));
        types.add(new FileType(true, "jpeg", "image/jpeg", MediaType.Image, false));
        types.add(new FileType(true, "gif", "image/gif", MediaType.Image, false));
        types.add(new FileType(true, "tif", "image/tiff", MediaType.Image, false));
        types.add(new FileType(true, "tiff", "image/tiff", MediaType.Image, false));
        types.add(new FileType(true, "png", "image/png", MediaType.Image, false));
        Collections.sort(types, new Comparator<FileType>() {
            public int compare(FileType o1, FileType o2) {
                return o1.getSuffix().compareTo(o2.getSuffix());
            }
        });
        return types;
    }

    @Override
    public boolean isUseSingleImageInFolder() {
        return false;
    }
}
