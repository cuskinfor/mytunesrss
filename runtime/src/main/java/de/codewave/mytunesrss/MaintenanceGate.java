package de.codewave.mytunesrss;

import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventListener;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;

public class MaintenanceGate implements MyTunesRssEventListener {

    private volatile boolean myMaintenanceMode;

    public MaintenanceGate() {
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    @Override
    public synchronized void handleEvent(MyTunesRssEvent event) {
        switch (event.getType()) {
            case MAINTENANCE_START:
                myMaintenanceMode = true;
                break;
            case MAINTENANCE_STOP:
                myMaintenanceMode = false;
                notifyAll();
                break;
            default:
                // ignore other events
        }
    }

    public synchronized void blockUntilMaintenanceFinished() throws InterruptedException {
        while (myMaintenanceMode) {
            wait();
        }
    }
}
