/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.jsp.FlipFlopTag
 */
public class EncodeBase64Tag extends BodyTagSupport {

    @Override
    public int doStartTag() {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write(MyTunesRssBase64Utils.encode(getBodyContent().getString()));
        } catch (IOException e) {
            throw new JspException(e);
        }
        return Tag.EVAL_PAGE;
    }
}