/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

public class WatchfolderDatasourceConfig extends DatasourceConfig {

    private long myMinFileSize;
    private long myMaxFileSize;
    private Pattern myIncludePattern;
    private Pattern myExcludePattern;

    public WatchfolderDatasourceConfig(String definition) {
        super(definition);
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Watchfolder;
    }

    public long getMinFileSize() {
        return myMinFileSize;
    }

    public void setMinFileSize(long minFileSize) {
        myMinFileSize = minFileSize;
    }

    public long getMaxFileSize() {
        return myMaxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        myMaxFileSize = maxFileSize;
    }

    public String getIncludePattern() {
        return myIncludePattern != null ? myIncludePattern.pattern() : null;
    }

    public void setIncludePattern(String includePattern) {
        myIncludePattern = StringUtils.isNotBlank(includePattern) ? Pattern.compile(includePattern) : null;
    }

    public String getExcludePattern() {
        return myExcludePattern != null ? myExcludePattern.pattern() : null;
    }

    public void setExcludePattern(String excludePattern) {
        myExcludePattern = StringUtils.isNotBlank(excludePattern) ? Pattern.compile(excludePattern) : null;
    }

    public boolean isIncluded(File file) {
        if (file.length() < myMinFileSize) {
            return false;
        }
        if (myMaxFileSize > 0 && file.length() > myMaxFileSize) {
            return false;
        }
        String name = file.getName();
        if (myExcludePattern != null) {
            if (myExcludePattern.matcher(name).matches()) {
                return false;
            }
        }
        return myIncludePattern == null || myIncludePattern.matcher(name).matches();
    }
}
