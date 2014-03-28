package de.codewave.mytunesrss;

import de.codewave.mytunesrss.mediarenderercontrol.MediaRendererController;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class OffHeapSessionStoreFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to initialize
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && ((HttpServletRequest)request).getSession(false) != null) {
            String currentListId = request.getParameter(OffHeapSessionStore.CURRENT_LIST_ID);
            if (StringUtils.isBlank(currentListId)) {
                OffHeapSessionStore.get((HttpServletRequest) request).removeCurrentList();
            }

        }
        chain.doFilter(request, response);
    }

    public void destroy() {
        // TODO: wrong class
        MediaRendererController.getInstance().setMediaRenderer(null);
        // nothing to destroy
    }
}
