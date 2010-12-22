<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<div id="externalSites" style="display:none" title="<fmt:message key="dialog.externalSite.title"/>">
    <c:forEach items="${externalSiteDefinitions}" var="externalSite" varStatus="siteLoopStatus">
        <p><a style="cursor:pointer;text-decoration:underline" onclick="openExternalSite('${externalSite.value}', $jQ(this).closest('div').dialog('option', 'keyword'));$jQ(this).closest('div').dialog('close')"><c:out value="${externalSite.key}"/></a></p>
    </c:forEach>
</div>

<script type="text/javascript">
    $jQ(document).ready(function() {
        $jQ("#externalSites").dialog({
            autoOpen:false,
            modal:true,
            buttons:{
                "<fmt:message key="dialog.button.close"/>":function() {
                    $jQ(this).dialog("close");
                }
            }
        })
    });
    function openExternalSite(urlTemplate, keyword) {
        var newWindow = window.open(urlTemplate.replace(/\\{KEYWORD\\}/, $jQ.trim(keyword)), "_blank");
        newWindow.focus();
    }
    function openExternalSitesDialog(keyword) {
        $jQ("#externalSites").dialog("option", "keyword", keyword);
        $jQ("#externalSites").dialog("open");
    }
</script>
