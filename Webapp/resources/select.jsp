<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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

    <script type="text/javascript">

      function sort(sortOrder) {
          document.forms["select"].action = "${urlMap.sort}";
          document.forms["select"].elements["sortOrder"].value = sortOrder;
          document.forms["select"].submit();
      }

      function selectAll(ids, checkbox) {
          var idArray = ids.split(";");
          for (var i = 0; i < idArray.length; i++) {
              document.getElementById("item" + idArray[i]).checked = checkbox.checked == true ? true : false;
          }
      }

      function registerTR() {
        var trs = document.getElementsByTagName("TR");
        for ( var i=0; i<trs.length; i++) {
          if ( trs[i].getElementsByTagName("TH").length > 0 ) {
            trs[i].getElementsByTagName("TH")[1].onclick = function(){ this.parentNode.getElementsByTagName("INPUT")[0].click() };
          }
          if ( trs[i].getElementsByTagName("TH").length == 0 ) {
            trs[i].onmouseover = function(){ this.className = this.className + " over" };
            trs[i].onmouseout = function(){ this.className = this.className.replace(/over/, "") };
            trs[i].getElementsByTagName("TD")[1].onclick = selectTrack;
          }
        }
      }

      function selectTrack() {
        var checkbox = this.parentNode.getElementsByTagName("input")[0];
        checkbox.checked =  ( checkbox.checked == true ) ? false : true;
      }

      function keepAndSearchMore() {
          document.forms["select"].elements["final"].value = 'false';
          document.forms["select"].submit();
      }

    </script>

  </head>

  <body onLoad="document.forms['select'].elements['channel'].focus(); registerTR();">

    <div class="body">

      <form id="select" action="${urlMap.select}" method="post">

        <input type="hidden" name="sortOrder" value="${sortOrder}" />
        <input type="hidden" name="final" value="true" />
        <input type="hidden" name="feedType" value="rss" />
          
        <div class="head">

          <h1><fmt:message key="select.channel"/></h1>

          <div class="input"><input type="text" name="channel" value="<c:out value="${param.channel}"/>" /></div>

          <jsp:include page="/error.jsp" />

          <div class="link">
            <c:if test="${sortOrder != 'Album'}"><a href="#" onClick="sort('Album')"><fmt:message key="select.group.album"/></a></c:if>
            <c:if test="${sortOrder != 'Artist'}"><a href="#" onClick="sort('Artist')"><fmt:message key="select.group.artist"/></a></c:if>
          </div>

        </div>

        <table class="select" cellspacing="0">
          <c:forEach items="${sections}" var="section">
            <c:set var="commonArtist" value="${section.commonArtist}" />
            <c:set var="commonAlbum" value="${section.commonAlbum}" />
            <tr>
              <th class="check"><input type="checkbox" name="none" value="none" onClick="selectAll('${section.sectionIds}',this)"/></th>
              <th colspan="2">
                <c:choose>
                  <c:when test="${commonArtist && commonAlbum}"><c:out value="${section.firstArtist} - ${section.firstAlbum}" /></c:when>
                  <c:when test="${commonArtist}"><c:out value="${section.firstArtist}" /></c:when>
                  <c:when test="${commonAlbum}"><c:out value="${section.firstAlbum}" /></c:when>
                  <c:otherwise>&nbsp;</c:otherwise>
                </c:choose>
              </th>
            </tr>
            <c:forEach items="${section.items}" var="item" varStatus="status">
              <tr <c:if test="${status.index%2==0}">class="odd"</c:if>>
                <td class="check"><input type="checkbox" id="item${item.file.id}" name="id" value="${item.file.id}" <c:if test="${item.selected}"> checked="checked"</c:if> /></td>
                <td>
                  <c:choose>
                    <c:when test="${!commonArtist && !commonAlbum}">
                      <c:out value="${item.file.album} -" />
                      <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                      <c:out value="${item.file.artist}" /> : <c:out value="${item.file.name}" />
                    </c:when>
                    <c:when test="${!commonArtist}">
                      <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                      <c:out value="${item.file.artist}" /> : <c:out value="${item.file.name}" />
                    </c:when>
                    <c:when test="${!commonAlbum}">
                      <c:out value="${item.file.album} -" />
                      <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                      <c:out value="${item.file.name}" />
                    </c:when>
                    <c:otherwise>
                      <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                      <c:out value="${item.file.name}" />
                    </c:otherwise>
                  </c:choose>
                </td>
                <td class="play"><a href="${urlMap.mp3}/id=${item.file.id}${authInfo}/${cwfn:urlEncode(item.file.virtualFileName, 'UTF-8')}"><img src="images/play.png" alt="<fmt:message key="select.play"/>"/></a></td>
              </tr>
            </c:forEach>
          </c:forEach>
        </table>

        <div class="buttons">
          <input type="button" onClick="document.location.href='${urlMap.newSearch}'" value="<fmt:message key="select.new_search"/>"/>
            <input type="button" onClick="keepAndSearchMore()" value="<fmt:message key="select.keep_and_search_more"/>"/>
          <input type="submit" value="<fmt:message key="select.create.rss"/>" />
          <input type="submit" value="<fmt:message key="select.create.m3u"/>" onclick="document.forms['select'].elements['feedType'].value = 'm3u'" />
        </div>

      </form>

    </div>

  </body>

</html>