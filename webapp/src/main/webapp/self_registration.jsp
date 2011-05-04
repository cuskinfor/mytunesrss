<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.MyTunesRssConfig"--%>

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
    
                <form id="registration" action="${servletUrl}/register" method="post">
    
                    <h2><fmt:message key="registrationCaption"/></h2>
                    
                    <jsp:include page="/incl_error.jsp"/>
                    
                    <div class="login">
    
	                    <table cellspacing="0">
	                        <tr>
	                            <td class="label">
	                                <label for="username">
	                                    <fmt:message key="userName"/>
	                                </label>
	                            </td>
	                            <td>
	                                <input class="text" type="text" name="username" id="username" value="<c:out value="${param.username}"/>"/>
	                            </td>
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
	                        </tr>
	                        <tr>
	                            <td class="label">
	                                <label for="retypepassword">
	                                    <fmt:message key="retypePassword"/>
	                                </label>
	                            </td>
	                            <td>
	                                <input class="text" type="password" name="retypepassword" id="retypepassword" value="<c:out value="${param.retypepassword}"/>"/>
	                            </td>
	                        </tr>
                            <tr>
                                <td class="label">
                                    <label for="email">
                                        <fmt:message key="registrationEmail"/>
                                    </label>
                                </td>
                                <td>
                                    <input class="text" type="text" name="email" id="email"
                                           value="<c:out value="${param.email}"/>"/>
                                </td>
                            </tr>
                            <tr>
	                            <td>&nbsp;</td>
	                            <td>
	                                <div class="submitBtn">
		                                <input class="button" type="submit" value="<fmt:message key="doRegister"/>"/>
		                            </div>
	                            </td>
	                        </tr>
	                    </table>
	                    
	            	</div>
            
                </form>
        
            </div>
        
        </div>
      
        <div class="footer">
            <div class="inner"></div>
        </div>
    
    </div>

</body>

</html>