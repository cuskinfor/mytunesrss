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
    function initExistingTags(tags) {
        $jQ("#editTagsDialog_existingTags").empty();
        for (var i = 0; i < tags.length; i++) {
            $jQ("#editTagsDialog_existingTags").append("<option value='" + tags[i] + "'>" + tags[i] + "</option>");
        }
    }
    function openEditTagsDialog(editTagsResource, editTagsParams, title) {
        initExistingTags(editTagsResource.getTags(editTagsParams));
        $jQ("#editTagsDialog").data("editTagsResource", editTagsResource);
        $jQ("#editTagsDialog").data("editTagsParams", editTagsParams);
        $jQ("#editTagsDialog_targetInfo").text("\"" + jQuery.trim(title) + "\"");
        openDialog("#editTagsDialog");
    }
    function getSelectedTags() {
        return $jQ.map($jQ('#editTagsDialog_existingTags :selected'), function(e) {
            return $jQ(e).text();
        });
    }
    function removeTags() {
        var result;
        var tags = getSelectedTags();
        for (var i = 0; i < tags.length; i++) {
            result = $jQ("#editTagsDialog").data("editTagsResource").deleteTag($jQ.extend({tag:tags[i]}, $jQ("#editTagsDialog").data("editTagsParams")));
        }
        initExistingTags(result);
    }
    function addTag() {
        var tag = jQuery.trim($jQ("#editTagsDialog_newTag").val());
        if (tag != "") {
            var tags = $jQ("#editTagsDialog").data("editTagsResource").setTag($jQ.extend({tag:tag}, $jQ("#editTagsDialog").data("editTagsParams")));
            $jQ("#editTagsDialog_newTag").val("");
            initExistingTags(tags);
        }
    }
    function showEditTagsTooltip(element, editTagsResource, editTagsParams) {
        $jQ(element).removeAttr('title');
        $jQ(element).removeAttr('alt');
        var tags = editTagsResource.getTags(editTagsParams);
        if (tags.length > 0) {
            $jQ("#tooltip_edittags").empty();
            for (var i = 0; i < tags.length; i++) {
                $jQ("#tooltip_edittags").append(tags[i] + "<br />");
            }
            showTooltipElement(document.getElementById("tooltip_edittags"));
        } else {
            $jQ(element).attr('title', '<fmt:message key="tooltip.editTags"/>');
            $jQ(element).attr('alt', '<fmt:message key="tooltip.editTags"/>');
        }
    }
</script>

<div class="tooltip" id="tooltip_edittags"></div>
