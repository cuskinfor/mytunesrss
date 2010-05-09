var jsonCallbackFunction;
var json;
var handleError = true;

function mytunesrss(func, parameterArray, callbackFunction) {
    var jsonString = $H({version : "1.1", method : func, id : "" + reqId, params : $A(parameterArray)}).toJSON();
    reqId++;
    jsonCallbackFunction = callbackFunction;
    var srcString = mytunesrssServer + "/jsonrpc" + sid + "?body=" + encodeURIComponent(jsonString) + "&jsonp=top.mytunesrssCallback";
    var dyna = document.getElementById("dynascript");
    var scpt = document.createElement("script");
    scpt.setAttribute("type", "text/javascript");
    scpt.setAttribute("src", srcString);
    truncate(dyna);
    dyna.appendChild(scpt);
}

function mytunesrssCallback(json) {
    if (json.error && handleError) {
        top.error(json.error.code, json.error.msg);
    } else {
        handleError = true;
        jsonCallbackFunction(json);
    }
}

function truncate(elem) {
    while (elem.hasChildNodes()) {
        elem.removeChild(elem.firstChild);
    }
}

function setSessionId(id) {
    if (typeof id != "undefined" && id != "") {
        sid = "/" + id;
    } else {
        sid = "";
    }
    top.document.cookie = top.mytunesrssUsername + "_sid=" + sid;
}

function getSessionId() {
    var sid = getCookieValue(top.mytunesrssUsername + "_sid");
    return sid == "undefined" ? "" : sid;
}

function loadContent(page) {
    document.getElementById("content").setAttribute("src", page);
}

function createIphonePlaylist(appUrl, tracks, startIndex, backCall) {
    var targetElement = top.document.getElementById("content").contentDocument;
    var html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1.0' /></head><body>";
    html += "<table border='0' cellspacing='3px' cellpadding='6px' width='100%'>";
    html += "<tr><td align='center' onclick='" + backCall + "'><input type=submit style='font-size:14px; width:64px' value='back'></td></tr></table>";
    html += "<div style='margin-top:60px;align:center'><embed src='" + appUrl + "/iphone/img/movie_poster.png' autoplay='false' href='" + tracks[startIndex].playbackUrl + "' type='" + tracks[startIndex].contentType + "' target='myself'\n";
    for (var i = startIndex + 1; i < tracks.length; i++) {
        html += "qtnext" + (i - startindex) + "='<" + tracks[i].playbackUrl + "> T<myself>'\n";
    }
    html += "qtnext" + tracks.length + "='GOTO0' /></div>";
    html += "</body></html>";
    var htmlElement = document.createElement("html");
    truncate(targetElement);
    targetElement.appendChild(htmlElement);
    htmlElement.innerHTML = html;
}

function getCookieValue(name) {
    var c = top.document.cookie;
    var b = 0;
    while (b >= 0 && b < c.length) {
    var e = c.indexOf(";", b);
        if (e == -1) {
            e = c.length;
        }
        var kv = c.substring(b, e).strip();
        var i = kv.indexOf("=");
        var k = kv.substring(0, i);
        var v = kv.substring(i + 1);
        if (k == name) {
            return v;
        }
        b = e + 1;
    }
    return "undefined";
}

function getRequestParameter(query, name) {
    var results = new RegExp("[?&]" + name + "=([^&#]*)").exec(query);
    return results == null ? null : results[1];
}
