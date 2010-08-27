package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FlashPlayerConfig;
import de.codewave.mytunesrss.MyTunesRss;
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
        getRequest().setAttribute("playerName", flashPlayerConfig.getName());
        getRequest().setAttribute("playerHtml", flashPlayerConfig.getHtml().replace("{PLAYLIST_URL}", getPlaylistUrl()).replace("{SWF_BASE_URL}", getSwfBasePath(id)));
        forward(MyTunesRssResource.Jukebox);
    }

    private String getSwfBasePath(String id) {
        return getRequest().getAttribute("appUrl") + "/flashplayer/" + id;
    }

    private String getPlaylistUrl() {
        StringBuilder playlistUrl = new StringBuilder((String) getRequest().getAttribute("servletUrl"));
        String auth = (String) getRequest().getAttribute("auth");
        if (StringUtils.isBlank(auth)) {
            auth = (String) getSession().getAttribute("auth");
        }
        playlistUrl.append("/").append(MyTunesRssCommand.CreatePlaylist.getName()).append("/").append(auth);
        playlistUrl.append("/").append(MyTunesRssWebUtils.encryptPathInfo(getRequest(), getRequestParameter("playlistParams", null) + "/playerRequest=true/type=Xspf"));
        return playlistUrl.toString();
    }
}