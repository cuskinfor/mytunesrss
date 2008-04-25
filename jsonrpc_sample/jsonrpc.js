function mytunesrss(func, parms, successFunction, failureFunction) {
    var sid = document.location.search;
    if (sid.length > 1) {
        sid = "/" + sid.substring(1);
    } else {
        sid = "";
    }
    new Ajax.Request("/mytunesrss/jsonrpc" + sid, {
        contentType:"application/json",
        postBody:$H({version:"1.1",method:func,id:"1", params:parms}).toJSON(),
        onSuccess: function(response) {
            var json = response.responseText.evalJSON();
            successFunction(json);
        },
        onFailure: function() {
            failureFunction()
        }
    });

}