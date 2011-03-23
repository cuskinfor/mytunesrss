<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<title><fmt:message key="applicationTitle" /> v${mytunesrssVersion}</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css?ts=${sessionCreationTime}" />
<!--[if IE]>
  <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css?ts=${sessionCreationTime}" />
<![endif]-->
<link rel="stylesheet" type="text/css" href="${appUrl}/styles/jquery-ui/jquery-ui.css?ts=${sessionCreationTime}" />
<link rel="stylesheet" type="text/css" href="${appUrl}/styles/jquery.autocomplete.css?ts=${sessionCreationTime}" />
<link href="${appUrl}/fullsize/fullsize.css" media="screen" rel="stylesheet" type="text/css"/>
<link href="${appUrl}/loadmask/jquery.loadmask.css" media="screen" rel="stylesheet" type="text/css"/>
<link rel="shortcut icon" href="${appUrl}/images/favicon.ico" type="image/x-icon" />
<script src="${appUrl}/js/jquery.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/jquery-ui.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.autocomplete.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.json.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/functions.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/fullsize/jquery.fullsize.minified.js" type="text/javascript"></script>
<script src="${appUrl}/loadmask/jquery.loadmask.min.js" type="text/javascript"></script>
<script type="text/javascript">
    var $jQ=jQuery.noConflict();

    <c:if test="${config.keepAlive && !empty authUser}">
        function sendKeepAlive() {
            $jQ.get("${servletUrl}/keepSessionAlive");
            window.setTimeout(sendKeepAlive, ${(authUser.sessionTimeout * 60 * 1000) - 20000});
        }

        window.setTimeout(sendKeepAlive, ${(authUser.sessionTimeout * 60 * 1000) - 20000});
    </c:if>

    function showLoading(text) {
        $jQ('div.body').mask(text);
    }
</script>
<meta name="viewport" content="width=480" />