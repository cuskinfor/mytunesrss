<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body onload="document.forms[0].elements['username'].focus()" class="loginpage">

    <div class="body">
    
        <div class="head">
            <h1><span><fmt:message key="myTunesRss" /></span></h1>
        </div>
    
        <div class="content">
    
            <div class="content-inner">
    
                <form id="login" action="${servletUrl}/login" method="post">
    
                    <h2><fmt:message key="loginCaption"/></h2>
                    
                    <jsp:include page="/incl_error.jsp"/>
    
                    <table class="login" cellspacing="0">
                        <tr>
                            <td class="label">
                                <label for="lc">
                                    <fmt:message key="languageSelection"/>
                                </label>
                            </td>
                            <td>
                                <select name="lc" id="lc">
                                    <option value=""><fmt:message key="languageSelectionDefault"/></option>
                                    <c:forEach items="${mtfn:availableLanguages(mtfn:preferredLocale(pageContext, true))}" var="lang">
                                    <option value="${lang[0]}" <c:if test="${lang[0] == mtfn:preferredLocale(pageContext, false).language}">selected="selected"</c:if>><c:out value="${lang[1]}"/></option>
                                    </c:forEach>
                                </select>
                            </td>
                            <td class="label">&nbsp;</td>
                        </tr>
                        <tr>
                            <td class="label">
                                <label for="username">
                                    <fmt:message key="userName"/>
                                </label>
                            </td>
                            <td>
                                <input class="text" type="text" name="username" id="username" value="<c:out value="${param.username}"/>"/>
                            </td>
                            <td>&nbsp;</td>
                        </tr>
                        <tr>
                            <td class="label">
                                <label for="password">
                                    <fmt:message key="password"/>
                                </label>
                            </td>
                            <td>
                                <input class="text" type="password" name="password" id="password" value="<c:out value="${param.password}"/>"/>
                            </td>
                            <td>
                                <input class="button" type="submit" value="<fmt:message key="doLogin"/>"/>
                            </td>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td>
                                <input type="checkbox" name="rememberLogin" value="true" id="rememberCheck"/>
                                <label for="rememberCheck"><fmt:message key="rememberLogin"/></label>
                            </td>
                            <td>&nbsp;</td>
                        </tr>
                        <c:if test="${globalConfig.validMailConfig}">
                            <tr>
                                <td>&nbsp;</td>
                                <td class="forgottenpassword">
                                    <a style="cursor:pointer" onclick="self.document.forms[0].action='${servletUrl}/sendForgottenPassword';self.document.forms[0].submit()"><fmt:message key="forgottenPasswordLink"/></a>
                                </td>
                                <td>&nbsp;</td>
                            </tr>
                        </c:if>
                    </table>
            
                </form>
        
            </div>
        
        </div>
      
        <div class="footer">
            <div class="footer-inner"></div>
        </div>
    
    </div>

</body>

</html>