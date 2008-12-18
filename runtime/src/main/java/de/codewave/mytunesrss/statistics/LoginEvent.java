package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * de.codewave.mytunesrss.statistics.DownloadBytesEvent
 */
public class LoginEvent implements StatisticsEvent {
    private static final long serialVersionUID = 1L;

    private String myUser;

    public LoginEvent() {
        // intentionally left blank
    }

    public LoginEvent(User user) {
        myUser = user.getName();
    }

    public String getUser() {
        return myUser;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(myUser);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        myUser = in.readUTF();
    }

    @Override
    public String toString() {
        return "# " + myUser + " logged in #";
    }
}