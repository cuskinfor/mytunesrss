/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.jsp.ArrayTag
 */
public class ArrayTag extends TagSupport {
    private String myVar;
    private List myList;

    public String getVar() {
        return myVar;
    }

    public void setVar(String var) {
        myVar = var;
    }

    @Override
    public int doStartTag() throws JspException {
        return Tag.EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        pageContext.setAttribute(getVar(), myList.toArray(new Object[myList.size()]));
        return Tag.EVAL_PAGE;
    }

    @Override
    public void setPageContext(PageContext pageContext) {
        super.setPageContext(pageContext);
        myVar = null;
        myList = new ArrayList();
    }

    void addElement(Object element) {
        myList.add(element);
    }
}