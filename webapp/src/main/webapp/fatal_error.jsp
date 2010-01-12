<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="errorPage">

	<div class="body">
	
		<div class="head">
		    <h1><span><fmt:message key="myTunesRss"/></span></h1>
		</div>
		
		<div class="content">
		
			<div class="content-inner">
	
				<form id="login" action="${servletUrl}/login/${auth}" method="post">
				
					<h2 class="fatalError"><fmt:message key="fatalError"/></h2>
					
					<div class="fatalError">
						
						<fmt:message key="error.fatal"/>
						
						<p><a href="${servletUrl}/showPortal/${auth}"><fmt:message key="gotoPortal"/></a></p>
					
					</div>
				
				</form>
	
			</div>
	
		</div>
		
		<div class="footer">
			<div class="footer-inner"></div>
		</div>
	
	</div>

</body>

</html>