        function sort(servletUrl, sortOrder) {
            document.forms["browse"].action = servletUrl + "/browseTrack";
            document.forms["browse"].elements["sortOrder"].value = sortOrder;
            document.forms["browse"].submit();
        }

        function selectAll(ids, checkbox) {
            var idArray = ids.split(",");
            for (var i = 0; i < idArray.length; i++) {
                var element = document.getElementById("item" + idArray[i]);
                if (element) {
                    element.checked = checkbox.checked == true ? true : false;
                }
            }
        }

        function registerTR() {
            var trs = document.getElementsByTagName("TR");
            for (var i = 0; i < trs.length; i++) {
                if (trs[i].getElementsByTagName("TH").length > 0) {
                    trs[i].getElementsByTagName("TH")[1].onclick = function() {
                        this.parentNode.getElementsByTagName("INPUT")[0].click()
                    };
                }
                if (trs[i].getElementsByTagName("TH").length == 0) {
                    trs[i].getElementsByTagName("TD")[1].onclick = selectTrack;
                }
            }
        }

        function selectTrack() {
            var checkbox = this.parentNode.getElementsByTagName("input")[0];
            checkbox.checked = ( checkbox.checked == true ) ? false : true;
        }
