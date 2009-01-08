package de.codewave.mytunesrss.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.RedirectServlet
 */
public class RedirectServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ensure we don't keep a session from any of the servlet filters mapped to "/*"
        if (req.getSession(false) != null) {
            req.getSession().invalidate();
        }
        resp.sendRedirect(req.getParameter("url"));
    }
}
