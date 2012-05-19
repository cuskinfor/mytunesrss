<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<title><fmt:message key="applicationTitle" /> v${mytunesrssVersion}</title>
<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
<meta http-equiv="Content-Script-Type" content="text/javascript" />
<link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css?ts=${sessionCreationTime}" />
<!--[if IE]>
  <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css?ts=${sessionCreationTime}" />
<![endif]-->
<link rel="stylesheet" type="text/css" href="${appUrl}/styles/jquery.autocomplete.css?ts=${sessionCreationTime}" />
<link href="${appUrl}/fullsize/fullsize.css" media="screen" rel="stylesheet" type="text/css"/>
<link href="${appUrl}/loadmask/jquery.loadmask.css" media="screen" rel="stylesheet" type="text/css"/>
<link href="${appUrl}/ctnotify/jquery.ctNotify.css" media="screen" rel="stylesheet" type="text/css"/>
<link rel="shortcut icon" href="${appUrl}/images/favicon.ico" type="image/x-icon" />
<script src="${appUrl}/js/jquery.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.autocomplete.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.json.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/functions.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/fullsize/jquery.fullsize.minified.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/loadmask/jquery.loadmask.min.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/ctnotify/jquery.ctNotify.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.simplemodal.js?ts=${sessionCreationTime}" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.cookie.js?ts=${sessionCreationTime}" type="text/javascript"></script>
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

    function hideLoading() {
        $jQ('div.body').unmask();
    }

    function openDialog(element) {
        $jQ(element).modal({
            overlayCss : "background-color: #000",
            overlayClose : true,
            autoResize: true
        });
    }

    function centerPopupWindow(url, name, width, height, params) {
        var centerWidth = (window.screen.width - width) / 2;
        var centerHeight = (window.screen.height - height) / 2;
        window.open("", name).close(); // make sure we get a new window in the next line
        return window.open(url, name, "left=" + centerWidth + ",top=" + centerHeight + ",width=" + width + ",height=" + height + (params === undefined ? "" : "," + params));
    }

    function displayError(text) {
        $jQ.ctNotifyOption({
            opacity: 1
        })
        $jQ.ctNotify(text, "error");
    }

</script>