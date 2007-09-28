function sort(servletUrl, sortOrder) {
    document.forms["browse"].action = servletUrl + "/browseTrack";
    document.forms["browse"].elements["sortOrder"].value = sortOrder;
    document.forms["browse"].submit();
}

function selectAllByLoop(prefix, first, last, checkbox) {
    for (var i = first; i <= last; i++) {
        var element = document.getElementById(prefix + i);
        if (element) {
            element.checked = checkbox.checked;
        }
    }
}

function selectAll(prefix, ids, checkbox) {
    var idArray = ids.split(",");
    for (var i = 0; i < idArray.length; i++) {
        var element = document.getElementById(prefix + idArray[i]);
        if (element) {
            element.checked = checkbox.checked;
        }
    }
}

function openPlayer(url) {
    window.open(url, 'MyTunesRssFlashPlayer', 'width=600,height=276,resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,hotkeys=no')
}

function getElementParams(elements, separator) {
    var elementNames = elements.split(",");
    var buffer = '';
    for (var i = 0; i < elementNames.length; i++) {
        buffer += elementNames[i] + "=" + getElementValue(self.document.getElementById(elementNames[i]));
        if (i + 1 < elementNames.length) {
            buffer += separator;
        }
    }
    return buffer;
}

function getElementValue(element) {
    if (element != undefined) {
        if (element.type == 'text') {
            return element.value;
        }
        if (element.type == 'select-one') {
            return element.options[element.options.selectedIndex].value;
        }
    }
    return '';
}
