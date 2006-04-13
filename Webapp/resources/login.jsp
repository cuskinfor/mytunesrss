<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

  <head>

    <title><fmt:message key="title"/> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="styles/mytunesrss.css" />
    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="styles/ie.css" />
    <![endif]-->

  </head>

  <body>

    <div class="body">

      <div class="head index">

        <h1 class="index">MyTunesRSS</h1>

      </div>

      <jsp:include page="/error.jsp" />

      <form id="login" action="${urlMap.login}" method="post">

        <table class="search" cellspacing="0">
          <tr>
            <th style="width: 50%;">&nbsp;</th>
            <th colspan="2"><fmt:message key="login.caption"/></th>
            <th style="width: 50%;">&nbsp;</th>
          </tr>
          <tr>
            <td rowspan="2">&nbsp;</td>
              <td><fmt:message key="login.password"/></td>
              <td><input class="text" type="password" name="password" value="<c:out value="${param.password}"/>" /></td>
            <td rowspan="2">&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td style="text-align: right;"><input class="button" type="submit" value="<fmt:message key="login.login"/>" /></td>
          </tr>
        </table>

      </form>

    </div>

  </body>

</html>
