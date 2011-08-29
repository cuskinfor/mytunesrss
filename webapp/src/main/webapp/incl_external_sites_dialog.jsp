<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<div id="externalSites" class="dialog">
    <h2>
        <fmt:message key="dialog.externalSite.title"/>
    </h2>
    <div>
        <c:forEach items="${externalSiteDefinitions}" var="externalSite" varStatus="siteLoopStatus">
            <p><a id="linkExternalSite${siteLoopStatus.index}" style="cursor:pointer;text-decoration:underline"
                  onclick="openExternalSite('${externalSite.value}', $jQ('#externalSites').data('keyword'));$jQ.modal.close()"><c:out
                    value="${externalSite.key}"/></a></p>
        </c:forEach>
        <p align="right">
            <button id="linkExternalSitesCloseDialog" onclick="$jQ.modal.close()"><fmt:message key="dialog.button.close"/></button>
        </p>
    </div>
</div>

<script type="text/javascript">
    function openExternalSite(urlTemplate, keyword) {
        var newWindow = window.open(urlTemplate.replace(/\\{KEYWORD\\}/, $jQ.trim(keyword)), "_blank");
        newWindow.focus();
    }
    function openExternalSitesDialog(keyword) {
        $jQ("#externalSites").data("keyword", keyword);
        openDialog("#externalSites");
    }
</script>
