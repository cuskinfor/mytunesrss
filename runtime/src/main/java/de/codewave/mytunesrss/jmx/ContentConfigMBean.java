package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.ContentConfigMBean
 */
public interface ContentConfigMBean {
    public String showPlaylist(String playlistId);

    public String hidePlaylist(String playlistId);

    public String[] getPlaylists();
}