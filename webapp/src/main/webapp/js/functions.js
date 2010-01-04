var tooltipElement;
var mouseX;
var mouseY;

document.onmousemove = updateTooltipPosition;

function openPlayer(url) {
    var flashPlayer = window.open(url, "MyTunesRssFlashPlayer", "width=600,height=276,resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,hotkeys=no");
    flashPlayer.onload=function() {
        flashPlayer.document.title = self.document.title;
    }
}

function getElementParams(elements, separator) {
    var elementNames = elements.split(",");
    var buffer = '';
    for (var i = 0; i < elementNames.length; i++) {
        var val = $jQ("#" + elementNames[i]).val();
        buffer += elementNames[i] + "=" + (val ? val : "");
        if (i + 1 < elementNames.length) {
            buffer += separator;
        }
    }
    return buffer;
}

function updateTooltipPosition(e) {
    var scrLeft = (document.documentElement && document.documentElement.scrollLeft) ? document.documentElement.scrollLeft : document.body.scrollLeft;
    var scrTop = (document.documentElement && document.documentElement.scrollTop) ? document.documentElement.scrollTop : document.body.scrollTop;
    mouseX = (document.all) ? window.event.x + scrLeft : e.pageX;
    mouseY = (document.all) ? window.event.y + scrTop : e.pageY;

    if (tooltipElement != null) {
        tooltipElement.style.position = "absolute";

        tooltipElement.style.left = (mouseX + 20) + "px";

        var bottomPos = mouseY + 20 + tooltipElement.scrollHeight;
        if (bottomPos > scrTop + (window.innerHeight ? window.innerHeight : document.documentElement.clientHeight)) {
            tooltipElement.style.top = (mouseY + 20 - (bottomPos - scrTop - (window.innerHeight ? window.innerHeight : document.documentElement.clientHeight))) + "px";
        } else {
            tooltipElement.style.top = (mouseY + 20) + "px";
        }
    }
}

function showTooltip(element) {
    showTooltipElement(document.getElementById("tooltip_" + element.id));
}

function showTooltipElement(element) {
    if (element != null) {
        document.body.appendChild(element);
        element.style.position = "absolute";
        element.style.left = (mouseX) + 10 + "px";
        element.style.top = (mouseY + 20) + "px";
        element.style.display = "block";
    }
}

function hideTooltip(element) {
    hideTooltipElement(document.getElementById("tooltip_" + element.id));
}

function hideTooltipElement(element) {
    if (element != null) {
        element.style.display = "none";
    }
}

function jsonRpc(serverUrl, func, parameterArray, resultCallback, sessionId) {
    var headers;
    if (sessionId != undefined) {
        headers = $H({
            "X-MyTunesRSS-ID" : sessionId
        })
    } else {
        headers = $H()
    }
    new Ajax.Request(serverUrl + "/../jsonrpc", {
        requestHeaders : headers,
        postBody : $H({
            "version" : "1.1",
            "method" : func,
            "id" : "0",
            "params" : parameterArray
        }).toJSON(),
        onSuccess : function(result) {
            if (resultCallback != undefined) {
                resultCallback(result.responseJSON.result, result.responseJSON.error);
            }
        }
    });
}
