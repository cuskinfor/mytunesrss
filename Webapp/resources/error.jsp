<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb"/>

<c:if test="${!empty error}">
  <div class="error">
    <fmt:message key="${error}">
      <fmt:param value="${errorParam0}"/>
      <fmt:param value="${errorParam1}"/>
    </fmt:message>
  </div>
</c:if>
