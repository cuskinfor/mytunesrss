/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * de.codewave.mytunesrss.jsp.ArrayElementTag
 */
public class ArrayElementTag extends TagSupport {
    private Object myValue;

    public Object getValue() {
        return myValue;
    }

    public void setValue(Object value) {
        myValue = value;
    }

    @Override
    public int doStartTag() throws JspException {
        return Tag.SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        ArrayTag arrayTag = (ArrayTag)TagSupport.findAncestorWithClass(this, ArrayTag.class);
        if (arrayTag != null) {
            arrayTag.addElement(myValue);
        } else {
            throw new IllegalStateException("ArrayElement tag needs parent Array tag!");
        }
        return Tag.EVAL_PAGE;
    }

    @Override
    public void setPageContext(PageContext pageContext) {
        super.setPageContext(pageContext);
        myValue = null;
    }
}