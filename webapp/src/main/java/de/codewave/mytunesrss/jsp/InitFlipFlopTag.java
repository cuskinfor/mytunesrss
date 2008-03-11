/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * de.codewave.mytunesrss.jsp.InitFlipFlopTag
 */
public class InitFlipFlopTag extends TagSupport {
    private String myValue1;
    private String myValue2;

    public String getValue1() {
        return myValue1;
    }

    public void setValue1(String value1) {
        myValue1 = value1;
    }

    public String getValue2() {
        return myValue2;
    }

    public void setValue2(String value2) {
        myValue2 = value2;
    }

    @Override
    public int doStartTag() throws JspException {
        return Tag.EVAL_BODY_INCLUDE;
    }


    @Override
    public int doEndTag() throws JspException {
        pageContext.setAttribute("flipFlop_value1", myValue1);
        pageContext.setAttribute("flipFlop_value2", myValue2);
        pageContext.setAttribute("flipFlop_current", myValue1);
        return Tag.EVAL_PAGE;
    }

    @Override
    public void setPageContext(PageContext pageContext) {
        super.setPageContext(pageContext);
        myValue1 = null;
        myValue2 = null;
    }
}