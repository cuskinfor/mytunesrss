package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * de.codewave.mytunesrss.statistics.DownloadBytesEvent
 */
public class DownloadEvent implements StatisticsEvent {
    private User myUser;
    private long myBytes;

    public DownloadEvent() {
        // intentionally left blank
    }

    public DownloadEvent(User user, long bytes) {
        myUser = user;
        myBytes = bytes;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(myUser.getName());
        out.writeLong(myBytes);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        myUser = MyTunesRss.CONFIG.getUser(in.readUTF());
        myBytes = in.readLong();
    }

    @Override
     public String toString() {
        return "# " + myUser.getName() + " downloaded " + myBytes + " bytes #";
    }
}