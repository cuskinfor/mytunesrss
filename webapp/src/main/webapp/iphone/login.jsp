<html>
<head>

<script src="${appUrl}/iphone/js/sha1.js" type="text/javascript"></script>
<script type="text/javascript">
    function login(json) {
        if (json.result && !json.error) {
            var expireDate = new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * 30);
            top.document.cookie = "mtr_iphone_username=" + document.getElementById("username").value + "; expires=" + expireDate.toGMTString();
            top.document.cookie = "mtr_iphone_password=" + SHA1(document.getElementById("password").value) + "; expires=" + expireDate.toGMTString();
            top.setSessionId(json.result);
            top.loadContent("${appUrl}/iphone/portal.jsp");
        } else {
            alert("Login failed");
        }
    }

    function cookieLogin(json) {
        if (json.result && !json.error) {
            top.setSessionId(json.result);
            top.loadContent("${appUrl}/iphone/portal.jsp");
        } else {
            setVisible();
        }
    }

    function clickLogin() {
        top.handleError = false;
        top.mytunesrss("LoginService.login", [document.getElementById("username").value, SHA1(document.getElementById("password").value), 180], login);
    }

    function tryCookieLogin() {
        var cookieUser = top.getCookieValue("mtr_iphone_username");
        var cookiePass = top.getCookieValue("mtr_iphone_password");
        if (cookieUser != "undefined" && cookiePass != "undefined") {
            top.handleError = false;
            top.mytunesrss("LoginService.login", [cookieUser, cookiePass, 180], cookieLogin);
        } else {
            setVisible();
        }
    }

    var defaultDisplay;

    function setHidden() {
        defaultDisplay = document.getElementById("loginpage").style.display;
        document.getElementById("loginpage").style.display = "none";
    }

    function setVisible() {
        document.getElementById("loginpage").style.display = defaultDisplay;
    }
</script>
</head>
<body onload="setHidden();top.initScroll();tryCookieLogin()" style="font-size:24px">

<TABLE border="0" width="100%" id="loginpage">
<TR>
<td colspan="2" align="center" background="${appUrl}/iphone/img/toolbar.png" style="font-weight:bold;color:white;font-size:24px" height="38px">MyTunesRSS - iPhone</td>
</TR>
<TR><TD align="center">&nbsp;<P>
<FONT SIZE="5" COLOR="">Username:</FONT><br /><input style="height:25px; width:150px; font-size:16px" id="username" type="text" autocorrect="off" autocapitalize="off"/><br />
<FONT SIZE="5" COLOR="">Password:</FONT><br /><input style="height:25px; width:150px; font-size:16px" id="password" type="password" /><P>
<a href="#" onclick="clickLogin()"><input type=submit style="font-size:16px" value="Login"></a>
</TD></TR>
</TABLE>

</body>
</html>
