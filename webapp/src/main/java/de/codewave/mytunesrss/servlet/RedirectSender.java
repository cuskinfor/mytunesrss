package de.codewave.mytunesrss.servlet;

import de.codewave.utils.servlet.StreamSender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RedirectSender extends StreamSender {

    private final String myRedirectUrl;

    public RedirectSender(String redirectUrl) {
        super(null, null, -1);
        myRedirectUrl = redirectUrl;
    }

    @Override
    public void sendHeadResponse(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Location", myRedirectUrl);
        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    }

    @Override
    public void sendGetResponse(HttpServletRequest request, HttpServletResponse response, boolean throwExceptions) throws IOException {
        try {
            sendHeadResponse(request, response);
        } finally {
            afterSend();
        }
    }
}
