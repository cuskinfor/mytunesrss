<html>
<head>

<script src="${appUrl}/iphone/js/sha1.js" type="text/javascript"></script>
<script type="text/javascript">
    function login(json) {
        if (json.result && !json.error) {
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
        top.mytunesrss("LoginService.login", [document.getElementById("username").value, document.getElementById("password").value, 180], login);
        //top.mytunesrss("LoginService.login", [document.getElementById("username").value, SHA1(document.getElementById("password").value), 180], login);
    }

</script>
</head>
<body onload="top.initScroll();" style="font-size:24px">

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
