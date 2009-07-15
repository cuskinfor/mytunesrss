<%@ page import="javax.servlet.jsp.jstl.core.Config" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocalizationContext" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.ResourceBundle" %>

<script type="text/javascript">

var serviceMessages = {

<%

    LocalizationContext context = (LocalizationContext)request.getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
    ResourceBundle resourceBundle=context.getResourceBundle();for (Enumeration keys = resourceBundle.getKeys(); keys.hasMoreElements(); ) {
        String key = (String)keys.nextElement();
        if (key.startsWith("service.")) {
            out.println("\"" + key + "\" : \"" + resourceBundle.getString(key) + "\",");
        }
    }

%>

    "dummy" : "dummy"
}

</script>
