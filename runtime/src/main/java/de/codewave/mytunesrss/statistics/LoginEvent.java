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
    private User myUser;

    public LoginEvent() {
        // intentionally left blank
    }

    public LoginEvent(User user) {
        myUser = user;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(myUser.getName());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        myUser = MyTunesRss.CONFIG.getUser(in.readUTF());
    }

    @Override
    public String toString() {
        return "# " + myUser.getName() + " logged in #";
    }
}