function mytunesrss(func, parms, callback) {
    var jsonString = $H({version:"1.1",method:func,id:"1", params:parms}).toJSON();
    var sid = "";
    if (typeof session != "undefined") {
        sid = "/" + session;
    }
    var srcString = "http://localhost:8080/jsonrpc" + sid + "?body=" + jsonString + "&jsonp=" + callback;
    document.getElementById("dynascript").setAttribute("src", srcString);
}
