<%@ page contentType="text/xml" %><?xml version="1.0" encoding="UTF-8"?>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/" xmlns:moz="http://www.mozilla.org/2006/browser/search/">
  <ShortName>MyTunesRSS (${appUrl})</ShortName>
  <Description>MyTunesRSS Media Search.</Description>
  <Contact>info@codewave.de</Contact>
  <Image height="16" width="16" type="image/x-icon">${appUrl}/images/favicon.ico</Image>
  <Url type="text/html" template="${servletUrl}/searchTracks/searchTerm={searchTerms}"/>
  <moz:SearchForm>${servletUrl}/showPortal</moz:SearchForm>
</OpenSearchDescription>
