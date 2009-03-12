package de.codewave.mytunesrss.remote.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientProcess
 */
public class VideoLanClientProcess {
    private Process myProcess;

    public synchronized void start() throws IOException, InterruptedException {
        if (myProcess == null) {
            myProcess = Runtime.getRuntime().exec("/Applications/VLC.app/Contents/MacOS/VLC -I rc");
        } else {
            throw new RuntimeException("VLC player process already running.");
        }
    }

    public synchronized void stop() throws InterruptedException {
        if (myProcess != null) {
            new PrintWriter(myProcess.getOutputStream()).println("quit");
            myProcess.waitFor();
            myProcess = null;
        } else {
            throw new RuntimeException("VLC player process not running.");
        }
    }

    public synchronized String sendCommand(String command) throws IOException, InterruptedException {
        if (myProcess != null) {
            new PrintWriter(myProcess.getOutputStream()).println(command);
            Thread.sleep(200);
            BufferedReader reader = new BufferedReader(new InputStreamReader(myProcess.getInputStream()));
            StringBuilder resultBuilder = new StringBuilder();
            while (reader.ready()) {
                resultBuilder.append(reader.readLine());
            }
            return resultBuilder.toString();
        } else {
            throw new RuntimeException("VLC player process not running.");
        }
    }
}