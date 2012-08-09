<%@ page contentType="text/xml;charset=UTF-8" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><?xml version="1.0" encoding="UTF-8"?>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>

<c:set var="backUrl">${servletUrl}/showPortal</c:set>

<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/" xmlns:moz="http://www.mozilla.org/2006/browser/search/">
    <ShortName>MyTunesRSS (${mtfn:decode64(param.username)} @ ${mtfn:hostFromUrl(appUrl)})</ShortName>
    <Description>MyTunesRSS Media Search.</Description>
    <Contact>info@codewave.de</Contact>
    <Image height="16" width="16" type="image/x-icon">${themeUrl}/images/favicon.ico</Image>
    <Url type="text/html" template="${servletUrl}/searchTracks/backUrl=${mtfn:encode64(backUrl)}/searchFuzziness=30/searchTerm={searchTerms}"/>
    <moz:SearchForm>${backUrl}</moz:SearchForm>
</OpenSearchDescription>
