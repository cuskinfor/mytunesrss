package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.User;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * de.codewave.mytunesrss.statistics.DownloadBytesEvent
 */
public class UploadEvent implements StatisticsEvent {
    private static final long serialVersionUID = 1L;

    private String myUser;
    private long myBytes;

    public UploadEvent() {
        // intentionally left blank
    }

    public UploadEvent(User user, long bytes) {
        myUser = user.getName();
        myBytes = bytes;
    }

    public String getUser() {
        return myUser;
    }

    public long getBytes() {
        return myBytes;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(myUser);
        out.writeLong(myBytes);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        myUser = in.readUTF();
        myBytes = in.readLong();
    }

    @Override
    public String toString() {
        return "# " + myUser + " uploaded " + myBytes + " bytes #";
    }
}