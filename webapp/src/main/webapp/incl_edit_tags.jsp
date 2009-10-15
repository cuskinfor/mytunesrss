<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="remoteApiSessionId" type="java.lang.String"--%>

<div id="editTagsDialog" style="display:none">
    TODO: existing tags<br />
    <select id="editTagsDialog_existingTags" multiple="true" size="10" style="width:100%"></select><br />
    <input type="button" onclick="removeTags();" value="TODO: remove" /><br />
    <input id="editTagsDialog_newTag" />&nbsp;<input type="button" onclick="addTag();" value="TODO: add" />
</div>

<script type="text/javascript">
    $jQ("#editTagsDialog_newTag").autocomplete("${servletUrl}/getTagsForAutocomplete");
    $jQ("#editTagsDialog").dialog({
        autoOpen:false,
        modal:true
    });
    function initExistingTags(json) {
        $jQ("#editTagsDialog_existingTags").empty();
        for (var i = 0; i < json.results.length; i++) {
            $jQ("#editTagsDialog_existingTags").append("<option value='" + json.results[i] + "'>" + json.results[i] + "</option>");
        }
    }
    function openEditTagsDialog(json, editTagsType, editTagsId) {
        initExistingTags(json);
        $jQ("#editTagsDialog").dialog("option", "editTagsType", editTagsType);
        $jQ("#editTagsDialog").dialog("option", "editTagsId", editTagsId);
        $jQ("#editTagsDialog").dialog("open");
    }
    function removeTags() {
        var tagIds = $jQ.map($jQ('#editTagsDialog_existingTags :selected'), function(e) { return $jQ(e).text(); });
        jsonRpc('${servletUrl}', 'TagService.removeTagsFrom' + $jQ("#editTagsDialog").dialog("option", "editTagsType"), [$jQ("#editTagsDialog").dialog("option", "editTagsId"), tagIds], function() {
            jsonRpc('${servletUrl}', 'TagService.getTagsFor' + $jQ("#editTagsDialog").dialog("option", "editTagsType"), [$jQ("#editTagsDialog").dialog("option", "editTagsId")], function(json) {
                initExistingTags(json);
            } ,'${remoteApiSessionId}');
        } ,'${remoteApiSessionId}');
    }
    function addTag() {
        jsonRpc('${servletUrl}', 'TagService.setTagsTo' + $jQ("#editTagsDialog").dialog("option", "editTagsType"), [$jQ("#editTagsDialog").dialog("option", "editTagsId"), $jQ("#editTagsDialog_newTag").val().split(" ")], function() {
            $jQ("#editTagsDialog_newTag").val("");
            jsonRpc('${servletUrl}', 'TagService.getTagsFor' + $jQ("#editTagsDialog").dialog("option", "editTagsType"), [$jQ("#editTagsDialog").dialog("option", "editTagsId")], function(json) {
                initExistingTags(json);
            } ,'${remoteApiSessionId}');
        } ,'${remoteApiSessionId}');
    }
</script>
