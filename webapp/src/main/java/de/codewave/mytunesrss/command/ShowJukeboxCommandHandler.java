package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FlashPlayerConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.command.ShowJukeboxCommandHandler
 */
public class ShowJukeboxCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        String id = getWebConfig().getFlashplayer();
        FlashPlayerConfig flashPlayerConfig = MyTunesRss.CONFIG.getFlashPlayer(id);
        if (flashPlayerConfig == null) {
            flashPlayerConfig = FlashPlayerConfig.getDefault(id);
        }
        String playerId = StringUtils.defaultIfEmpty(getRequestParameter("playerId", flashPlayerConfig.getId()), FlashPlayerConfig.ABSOLUTE_DEFAULT.getId());
        redirect(MyTunesRssWebUtils.getApplicationUrl(getRequest()) + "/flashplayer/" + playerId + "/?url=" + MyTunesRssUtils.getUtf8UrlEncoded(getPlaylistUrl()));
    }

    private String getPlaylistUrl() {
        StringBuilder playlistUrl = new StringBuilder(MyTunesRssWebUtils.getServletUrl(getRequest()));
        String auth = (String) getRequest().getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) getSession().getAttribute("auth");
        }
        playlistUrl.append("/").append(MyTunesRssCommand.CreatePlaylist.getName()).append("/").append(auth);
        playlistUrl.append("/fpr=1/");
        playlistUrl.append("/").append(MyTunesRssWebUtils.encryptPathInfo(getRequest(), getRequestParameter("playlistParams", null) + "/type=Xspf"));
        return playlistUrl.toString();
    }
}