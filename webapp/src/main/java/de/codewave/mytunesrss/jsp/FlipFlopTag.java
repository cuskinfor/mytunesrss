/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import org.apache.commons.logging.*;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.jsp.FlipFlopTag
 */
public class FlipFlopTag extends TagSupport {
    private static final Log LOG = LogFactory.getLog(FlipFlopTag.class);

    @Override
    public int doStartTag() throws JspException {
        return Tag.EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        String value1 = (String)pageContext.getAttribute("flipFlop_value1");
        String value2 = (String)pageContext.getAttribute("flipFlop_value2");
        String current = (String)pageContext.getAttribute("flipFlop_current");
        if (current.equals(value1)) {
            pageContext.setAttribute("flipFlop_current", value2);
        } else {
            pageContext.setAttribute("flipFlop_current", value1);
        }
        try {
            pageContext.getOut().print(current);
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not write to JSP writer.", e);
            }
        }
        return Tag.EVAL_PAGE;
    }
}