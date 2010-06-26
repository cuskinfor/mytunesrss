<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<c:if test="${!empty errors}">
	<div class="messageContainer">
	    <div class="error">
	    	<div class="top">
	    		<div class="inner"></div>
	    	</div>
	    	<div class="messageContent">
	    		<div class="inner">
	    			<div class="icon"></div>
			        <c:forEach items="${errors}" var="error">
			            <c:choose>
			                <c:when test="${error.localized}">
			                    <c:set var="localizedMessage" value="${error.message}" />
			                </c:when>
			                <c:otherwise>
			                    <fmt:message var="localizedMessage" key="${error.key}" />
			                </c:otherwise>
			            </c:choose>
			            <c:out value="${cwfn:message(localizedMessage, error.parameters)}" escapeXml="${error.escapeXml}"/>
			        </c:forEach>
				</div>
			</div>
			<div class="bottom">
				<div class="inner"></div>
			</div>        
	    </div>
	</div>
    <c:remove var="errors" scope="session" />
</c:if>

<c:if test="${!empty messages}">
	<div class="messageContainer">
	    <div class="message">
			<div class="top">
				<div class="inner"></div>
			</div>
			<div class="messageContent">
				<div class="inner">
					<div class="icon"></div>
			        <c:forEach items="${messages}" var="message">
                        <div>
                            <c:choose>
                                <c:when test="${message.localized}">
                                    <c:set var="localizedMessage" value="${message.message}" />
                                </c:when>
                                <c:otherwise>
                                    <fmt:message var="localizedMessage" key="${message.key}" />
                                </c:otherwise>
                            </c:choose>
                            <c:out value="${cwfn:message(localizedMessage, message.parameters)}" escapeXml="${message.escapeXml}"/>
                        </div>
			        </c:forEach>
				</div>
			</div>
			<div class="bottom">
				<div class="inner"></div>
			</div>        		        
	    </div>
	</div>
    <c:remove var="messages" scope="session" />
</c:if>
