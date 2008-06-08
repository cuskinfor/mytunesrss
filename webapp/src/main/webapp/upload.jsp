<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        function initProgress() {
            frames["progressFrame"].location.href = "${servletUrl}/showUploadProgress/${auth}";
            document.getElementById("progressDiv").style.display = "block";
        }
        function updateProgress() {
            var percentage = frames["progressFrame"].document.getElementById("progress").innerHTML;
            document.getElementById("progressBar").style.width = (percentage) + "%";
            document.getElementById("progressBar").innerHTML = percentage + "%";
        }
    </script>

</head>

<body>

<div class="body">

    <h1 class="upload">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active"><fmt:message key="fileUpload"/></th>
        </tr>
        <tr>
            <td><fmt:message key="fileUploadInfo"/></td>
        </tr>
        <tr class="odd">
            <td>
                <form name="upload" enctype="multipart/form-data" method="post" action="${mtfn:makeHttp(servletUrl)}/upload/${auth}" target="resultFrame">
                    <input type="file" name="file" /> <input type="submit" value="<fmt:message key="doUpload"/>" onclick="initProgress()"/>
                </form>
            </td>
        </tr>
    </table>

    <div id="progressDiv">
        <div id="progressBackground">
            <div id="progressBar">0%</div>
        </div>
    </div>

</div>

<iframe name="progressFrame" style="visibility:hidden" src=""></iframe>

<iframe name="resultFrame" style="visibility:hidden" src=""></iframe>

</body>
</html>
