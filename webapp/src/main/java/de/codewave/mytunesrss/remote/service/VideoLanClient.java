package de.codewave.mytunesrss.remote.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientProcess
 */
public class VideoLanClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoLanClient.class);
    private static final String DUMMY_COMMAND = "thisdummycommanddoesnotexist";

    private TelnetClient myTelnetClient;

    public synchronized void connect(String host, int port) throws IOException, InterruptedException {
        if (myTelnetClient != null) {
            throw new RuntimeException("Already connected.");
        }
        LOGGER.debug("Creating VLC telnet client.");
        myTelnetClient = new TelnetClient();
        try {
            LOGGER.debug("Connecting to VLC telnet server at \"" + host + ":" + port + "\".");
            myTelnetClient.connect(host, port);
        } catch (IOException e) {
            myTelnetClient = null;
            throw e;
        }
    }

    public synchronized void disconnect() throws InterruptedException, IOException {
        if (myTelnetClient == null) {
            throw new RuntimeException("Not connected.");
        }
        LOGGER.debug("Disconnecting from VLC telnet server.");
        send("logout");
        myTelnetClient.disconnect();
        myTelnetClient = null;
    }

    public synchronized String sendCommands(String... commands) throws IOException, InterruptedException {
        if (myTelnetClient == null) {
            throw new RuntimeException("Not connected.");
        }
        StringBuilder builder = new StringBuilder();
        for (String command : commands) {
            builder.append(send(command));
        }
        return builder.toString();
    }

    private synchronized String send(String... commands) throws InterruptedException {
        PrintStream out = new PrintStream(myTelnetClient.getOutputStream());
        for (String command : commands) {
            LOGGER.debug("Sending command \"" + command + "\".");
            out.println(command);
        }
        out.println(DUMMY_COMMAND);
        out.flush();
        LOGGER.debug("Reading response.");
        StringBuilder builder = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(myTelnetClient.getInputStream()));
        try {
            for (String line = in.readLine(); line != null && !StringUtils.contains(line, DUMMY_COMMAND); line = in.readLine()) {
                LOGGER.debug("Received line \"" + line + "\".");
                builder.append(line);
            }
        } catch (IOException e) {
            LOGGER.error(null, e);

        }
        return builder.toString();
    }
}