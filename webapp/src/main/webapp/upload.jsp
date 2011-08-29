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

<body class="upload">

    <div class="body">

        <div class="head">
            <h1 class="upload">
                <a id="linkPortal" class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>

        <div class="content">

            <div class="content-inner">

                <jsp:include page="/incl_error.jsp" />

                <table cellspacing="0" class="settings">
                    <tr>
                        <th class="active"><fmt:message key="fileUpload"/></th>
                    </tr>
                    <tr>
                        <td class="label"><fmt:message key="fileUploadInfo"/></td>
                    </tr>
                    <tr>
                        <td>
                            <form name="upload" enctype="multipart/form-data" method="post" action="${servletUrl}/upload/${auth}" target="resultFrame">
                                <input type="file" name="file" />
                                <input id="linkDoUpload" type="submit" value="<fmt:message key="doUpload"/>" onclick="initProgress()"/>
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

        </div>

        <div class="footer">
            <div class="inner"></div>
        </div>

    </div>

    <iframe name="progressFrame" style="visibility:hidden" src=""></iframe>

    <iframe name="resultFrame" style="visibility:hidden" src=""></iframe>

</body>
</html>
