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

function selectTrack() {
    var checkbox = this.parentNode.getElementsByTagName("input")[0];
    checkbox.checked = !checkbox.checked;
}
