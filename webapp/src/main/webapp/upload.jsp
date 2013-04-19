<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        function initProgress() {
            $jQ("#linkDoUpload").attr("disabled", "true");
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
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>

        <div class="content">

            <div class="content-inner">

                <jsp:include page="/incl_error.jsp" />

                <form name="upload" enctype="multipart/form-data" method="post" action="${servletUrl}/upload/${auth}" target="resultFrame">
                    <table cellspacing="0" class="settings">
                        <tr>
                            <th colspan="2" class="active"><fmt:message key="fileUpload"/></th>
                        </tr>
                        <tr>
                            <td colspan="2" class="info"><fmt:message key="fileUploadDatasourceInfo"/></td>
                        </tr>
                        <tr>
                            <td class="formlabel">
                                <fmt:message key="fileUploadDatasourceLabel"/>
                            </td>
                            <td class="formelement">
                                <select id="selectUploadDatasource" name="datasource">
                                    <c:forEach var="ds" items="${datasources}">
                                        <option id="dsOpt${ds.id}" value="${ds.id}"><c:out value="${ds.name}" /></option>
                                    </c:forEach>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td class="formlabel">
                                <fmt:message key="fileUploadFileLabel"/>
                            </td>
                            <td class="formelement">
                                <input id="fileUploadSelector" type="file" name="file" />
                            </td>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td class="formbutton">
                                <input id="linkDoUpload" type="submit" value="<fmt:message key="doUpload"/>" onclick="initProgress()"/>
                            </td>
                        </tr>
                    </table>
                </form>

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
