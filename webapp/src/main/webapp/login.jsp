<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body onload="document.forms[0].elements['username'].focus()">

<div class="body">

  <h1 class="search"><span><fmt:message key="myTunesRss"/></span></h1>

  <jsp:include page="/incl_error.jsp"/>

  <form id="login" action="${servletUrl}/login" method="post">

    <h2>
      <fmt:message key="loginCaption"/>
    </h2>

    <div class="login">

      <table class="login" cellspacing="0">
      <tr>
        <td>
          <fmt:message key="userName"/>
        </td>
        <td>
          <input class="text" type="text" name="username" value="<c:out value="${param.username}"/>"/>
        </td>
        <td>&nbsp;</td>
      </tr>
        <tr>
          <td>
            <fmt:message key="password"/>
          </td>
          <td>
            <input class="text" type="password" name="password" value="<c:out value="${param.password}"/>"/>
          </td>
          <td>
            <input class="button" type="submit" value="<fmt:message key="doLogin"/>"/>
          </td>
        </tr>
        <c:if test="${globalConfig.validMailConfig}">
            <tr>
              <td>
                &nbsp;
              </td>
              <td class="forgottenpassword">
                <a href="${servletUrl}/sendForgottenPassword"><fmt:message key="forgottenPasswordLink"/></a>
              </td>
              <td>
                &nbsp;
              </td>
            </tr>
        </c:if>
        <tr>
          <td>&nbsp;</td>
          <td>
            <input type="checkbox" name="rememberLogin" value="true"/>
            <fmt:message key="rememberLogin"/>
          </td>
          <td>&nbsp;</td>
        </tr>
      </table>

    </div>

  </form>

</div>

</body>

</html>