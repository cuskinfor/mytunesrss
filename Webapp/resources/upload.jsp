<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title>
        <fmt:message key="applicationTitle" />
        v${mytunesrssVersion}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css?ts=${sessionCreationTime}" />
    <!--[if IE]>
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css?ts=${sessionCreationTime}" />
  <![endif]-->

</head>

<body>

<div class="body">
    <form name="upload" enctype="multipart/form-data" method="post" action="${servletUrl}/upload">
        <input type="text" name="firstName" /> <input type="text" name="lastName" /> <input type="file" name="file" /> <input type="submit" value="go" />
    </form>
</div>

</body>
</html>
