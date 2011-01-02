/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.jsp;

import org.codehaus.jackson.io.JsonStringEncoder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

public class EscapeJsonTag extends BodyTagSupport {

    @Override
    public int doStartTag() {
        return BodyTag.EVAL_BODY_BUFFERED;
    }


    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write(JsonStringEncoder.getInstance().quoteAsString(getBodyContent().getString()));
        } catch (IOException e) {
            throw new JspException("Could not escape JSON string.", e);
        }
        return EVAL_PAGE;
    }
}
