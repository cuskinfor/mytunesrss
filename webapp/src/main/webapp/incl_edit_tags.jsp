<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="remoteApiSessionId" type="java.lang.String"--%>

<div id="editTagsDialog" class="dialog">
    <h2>
        <fmt:message key="editTagsDialogTitle"/>
    </h2>
    <div>
        <p>
            <fmt:message key="editTagsDialogInfoPre"/>
            <span id="editTagsDialog_targetInfo"></span>
            <fmt:message key="editTagsDialogInfoPost"/>
        </p>
        <select id="editTagsDialog_existingTags" multiple="true" size="10" style="width:100%"></select><br />
        <button id="linkEditTagsRemove" class="ui-state-default ui-corner-all" style="margin-top:3px" onclick="removeTags();"><fmt:message key="removeTags"/></button><br /><br />
        <input id="editTagsDialog_newTag" style="width:100%" /><br />
        <button id="linkEditTagsAdd" class="ui-state-default ui-corner-all" style="margin-top:3px" onclick="addTag();"><fmt:message key="addTags"/></button>
    </div>
</div>

<script type="text/javascript">
    $jQ("#editTagsDialog_newTag").autocomplete("${servletUrl}/getTagsForAutocomplete");
    function initExistingTags(json) {
        $jQ("#editTagsDialog_existingTags").empty();
        for (var i = 0; i < json.results.length; i++) {
            $jQ("#editTagsDialog_existingTags").append("<option value='" + json.results[i] + "'>" + json.results[i] + "</option>");
        }
    }
    function openEditTagsDialog(json, editTagsType, editTagsId, title) {
        initExistingTags(json);
        $jQ("#editTagsDialog").data("editTagsType", editTagsType);
        $jQ("#editTagsDialog").data("editTagsId", editTagsId);
        $jQ("#editTagsDialog_targetInfo").text("\"" + jQuery.trim(title) + "\"");
        openDialog("#editTagsDialog");
    }
    function removeTags() {
        var tagIds = $jQ.map($jQ('#editTagsDialog_existingTags :selected'), function(e) { return $jQ(e).text(); });
        jsonRpc('${servletUrl}', 'TagService.removeTagsFrom' + $jQ("#editTagsDialog").data("editTagsType"), [$jQ("#editTagsDialog").data("editTagsId"), tagIds], function() {
            jsonRpc('${servletUrl}', 'TagService.getTagsFor' + $jQ("#editTagsDialog").data("editTagsType"), [$jQ("#editTagsDialog").data("editTagsId")], function(json) {
                initExistingTags(json);
            } ,'${remoteApiSessionId}');
        } ,'${remoteApiSessionId}');
    }
    function addTag() {
        var tags = jQuery.trim($jQ("#editTagsDialog_newTag").val());
        if (tags != '') {
            jsonRpc('${servletUrl}', 'TagService.setTagsTo' + $jQ("#editTagsDialog").data("editTagsType"), [$jQ("#editTagsDialog").data("editTagsId"), tags.split(" ")], function() {
                $jQ("#editTagsDialog_newTag").val("");
                jsonRpc('${servletUrl}', 'TagService.getTagsFor' + $jQ("#editTagsDialog").data("editTagsType"), [$jQ("#editTagsDialog").data("editTagsId")], function(json) {
                    initExistingTags(json);
                } ,'${remoteApiSessionId}');
            } ,'${remoteApiSessionId}');
        }
    }
    function showEditTagsTooltip(element, type, id) {
        $jQ(element).removeAttr('title');
        $jQ(element).removeAttr('alt');
        jsonRpc("${servletUrl}", "TagService.getTagsFor" + type, [id], function(json) {
            if (json.results.length > 0) {
                $jQ("#tooltip_edittags").empty();
                for (var i = 0; i < json.results.length; i++) {
                    $jQ("#tooltip_edittags").append(json.results[i] + "<br />");
                }
                showTooltipElement(document.getElementById("tooltip_edittags"));
            } else {
                $jQ(element).attr('title', '<fmt:message key="tooltip.editTags"/>');
                $jQ(element).attr('alt', '<fmt:message key="tooltip.editTags"/>');
            }
        }, '${remoteApiSessionId}');
    }
</script>

<div class="tooltip" id="tooltip_edittags"></div>
