/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.utils.WildcardMatcher;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.List;

public class MediaServerClientProfile {

    private String myUserAgentPattern;
    private WildcardMatcher myWildcardMatcher;
    private List<Integer> myPhotoSizes = Arrays.asList(1024, 2048, 4096, 0);

    public String getUserAgentPattern() {
        return myUserAgentPattern;
    }

    public void setUserAgentPattern(String userAgentPattern) {
        myUserAgentPattern = userAgentPattern;
        myWildcardMatcher = new WildcardMatcher(userAgentPattern);
    }

    public List<Integer> getPhotoSizes() {
        return myPhotoSizes;
    }

    public void setPhotoSizes(List<Integer> photoSizes) {
        myPhotoSizes = photoSizes;
    }

    @XmlTransient
    public boolean matches(String userAgent) {
        return myWildcardMatcher.matches(userAgent);
    }
}
