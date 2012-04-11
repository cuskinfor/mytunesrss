<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp" />

</head>

<body class="smartPlaylist">

	<div class="body">

		<div class="head">
		    <h1 class="manager">
		        <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal" /></span></a>
		        <span><fmt:message key="myTunesRss" /></span>
		    </h1>
		</div>

		<div class="content">

			<div class="content-inner">

			    <jsp:include page="/incl_error.jsp" />

			    <form id="playlist" action="${servletUrl}/saveSmartPlaylist/${auth}" method="post">

			        <input type="hidden" name="smartPlaylist.playlist.id" value="${smartPlaylist.playlist.id}" />
			        <input id="remove" type="hidden" name="remove" value="" />

			        <table class="settings" cellspacing="0">

			            <tr>
			                <th class="active" colspan="4"><fmt:message key="editSmartPlaylistTitle" /></th>
			            </tr>

			            <mt:initFlipFlop value1="" value2="class=\"odd\""/>

			            <tr <mt:flipFlop/>>
			                <td class="label"><label for="smartPlaylistName"><fmt:message key="playlistName" /></label></td>
			                <td colspan="3"><input id="smartPlaylistName" type="text" name="smartPlaylist.playlist.name" value="<c:out value="${smartPlaylist.playlist.name}"/>" /></td>
			            </tr>

                        <tr <mt:flipFlop/>>
                            <td class="label"><label for="smartPlaylistUserPrivate"><fmt:message key="playlistUserPrivate" /></label></td>
                            <td colspan="3">
                                <input id="smartPlaylistUserPrivate" type="checkbox"
                                       <c:if test="${!authUser.createPublicPlaylists}">disabled="disabled"</c:if>
                                       name="smartPlaylist.playlist.userPrivate"
                                       value="true"
                                       <c:if test="${smartPlaylist.playlist.userPrivate}">checked="checked"</c:if> />
                            </td>
                        </tr>

			            <c:forEach items="${smartPlaylist.smartInfos}" var="smartInfo" varStatus="loopStatus">
                            <input type="hidden" name="type_${loopStatus.index}" value="${smartInfo.fieldType}" />
			                <tr id="criteriaRow${loopStatus.index}" <mt:flipFlop/>>
			                    <td class="label"><fmt:message key="smartPlaylist.smartInfo.${smartInfo.fieldType}" /></td>
                                <c:choose>
                                    <c:when test="${smartInfo.fieldType == 'mediatype'}">
                                        <td>
                                            <select name="pattern_${loopStatus.index}">
                                                <option value="Audio" <c:if test="${smartInfo.pattern == 'Audio'}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.mediatype.audio"/></option>
                                                <option value="Video" <c:if test="${smartInfo.pattern == 'Video'}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.mediatype.video"/></option>
                                                <option value="Image" <c:if test="${smartInfo.pattern == 'Image'}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.mediatype.image"/></option>
                                            </select>
                                        </td>
                                        <td>
                                            <input type="checkbox" name="invert_${loopStatus.index}" value="true" <c:if test="${smartInfo.invert}">checked="checked"</c:if>/><fmt:message key="smartPlaylist.smartInfo.invert" />
                                        </td>
                                        <td>
                                            <img class="smartPlaylistDeleteAction" src="${appUrl}/images/action-delete.png" onclick="$jQ('#playlist').attr('action', '${servletUrl}/delSmartPlaylistCriteria/${auth}');$jQ('#remove').attr('value', '${loopStatus.index}');$jQ('#playlist').submit()"/>
                                        </td>
                                    </c:when>
                                    <c:when test="${smartInfo.fieldType == 'videotype'}">
                                        <td colspan="2">
                                            <select name="pattern_${loopStatus.index}">
                                                <option value="Movie" <c:if test="${smartInfo.pattern == 'Movie'}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.videotype.movie"/></option>
                                                <option value="TvShow" <c:if test="${smartInfo.pattern == 'TvShow'}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.videotype.tvshow"/></option>
                                            </select>
                                        </td>
                                        <td>
                                            <img class="smartPlaylistDeleteAction" src="${appUrl}/images/action-delete.png" onclick="$jQ('#playlist').attr('action', '${servletUrl}/delSmartPlaylistCriteria/${auth}');$jQ('#remove').attr('value', '${loopStatus.index}');$jQ('#playlist').submit()"/>
                                        </td>
                                    </c:when>
                                    <c:when test="${smartInfo.fieldType == 'protection'}">
                                        <td colspan="2">
                                            <select name="pattern_${loopStatus.index}">
                                                <option value="true" <c:if test="${smartInfo.pattern}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.protection.true"/></option>
                                                <option value="false" <c:if test="${!smartInfo.pattern}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.protection.false"/></option>
                                            </select>
                                        </td>
                                        <td>
                                            <img class="smartPlaylistDeleteAction" src="${appUrl}/images/action-delete.png" onclick="$jQ('#playlist').attr('action', '${servletUrl}/delSmartPlaylistCriteria/${auth}');$jQ('#remove').attr('value', '${loopStatus.index}');$jQ('#playlist').submit()"/>
                                        </td>
                                    </c:when>
                                    <c:otherwise>
                                        <td><input type="text" name="pattern_${loopStatus.index}" value="<c:out value="${smartInfo.pattern}"/>" /></td>
                                        <td>
                                            <input type="checkbox" name="invert_${loopStatus.index}" value="true" <c:if test="${smartInfo.invert}">checked="checked"</c:if>/><fmt:message key="smartPlaylist.smartInfo.invert" />
                                        </td>
                                        <td>
                                            <img class="smartPlaylistDeleteAction" src="${appUrl}/images/action-delete.png" onclick="$jQ('#playlist').attr('action', '${servletUrl}/delSmartPlaylistCriteria/${auth}');$jQ('#remove').attr('value', '${loopStatus.index}');$jQ('#playlist').submit()"/>
                                        </td>
                                    </c:otherwise>
                                </c:choose>
			                </tr>
			            </c:forEach>

                        <tr><td class="criteriaSpacer" colspan="4"></td></tr>

                        <tr id="addCriteria">
                            <td class="label">
                                <fmt:message key="smartPlaylist.smartInfo.new" />
                            </td>
                            <td colspan="3">
                                <select name="newFieldType">
                                    <option value="album"><fmt:message key="smartPlaylist.smartInfo.album"/></option>
                                    <option value="artist"><fmt:message key="smartPlaylist.smartInfo.artist"/></option>
                                    <option value="genre"><fmt:message key="smartPlaylist.smartInfo.genre"/></option>
                                    <option value="tvshow"><fmt:message key="smartPlaylist.smartInfo.tvshow"/></option>
                                    <option value="title"><fmt:message key="smartPlaylist.smartInfo.title"/></option>
                                    <option value="file"><fmt:message key="smartPlaylist.smartInfo.file"/></option>
                                    <option value="tag"><fmt:message key="smartPlaylist.smartInfo.tag"/></option>
                                    <option value="comment"><fmt:message key="smartPlaylist.smartInfo.comment"/></option>
                                    <option value="mintime"><fmt:message key="smartPlaylist.smartInfo.mintime"/></option>
                                    <option value="maxtime"><fmt:message key="smartPlaylist.smartInfo.maxtime"/></option>
                                    <option value="mediatype"><fmt:message key="smartPlaylist.smartInfo.mediatype"/></option>
                                    <option value="videotype"><fmt:message key="smartPlaylist.smartInfo.videotype"/></option>
                                    <option value="protection"><fmt:message key="smartPlaylist.smartInfo.protection"/></option>
                                    <option value="composer"><fmt:message key="smartPlaylist.smartInfo.composer"/></option>
                                </select>
                                <input id="linkAddCriteria" type="submit" value="<fmt:message key="smartPlaylist.smartInfo.add"/>" onclick="$jQ('#playlist').attr('action', '${servletUrl}/addSmartPlaylistCriteria/${auth}');return true;"/>
                            </td>
                        </tr>

			        </table>

			        <div class="buttons">
			            <input id="linkSubmit" type="submit" value="<fmt:message key="doSave"/>" />
			            <input id="linkCancel" type="button" value="<fmt:message key="doCancel"/>" onclick="document.location.href='${servletUrl}/showPlaylistManager/${auth}'" />
			        </div>

			    </form>

			</div>

		</div>

		<div class="footer">
			<div class="inner"></div>
		</div>

	</div>

</body>

</html>