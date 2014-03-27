package de.codewave.mytunesrss.remotecontrol;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.TransportState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AvTransportLastChangeSubscriptionCallback extends SubscriptionCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvTransportLastChangeSubscriptionCallback.class);

    private LastChange myPreviousLastChange = new LastChange(new AVTransportLastChangeParser());

    public AvTransportLastChangeSubscriptionCallback(Service service) {
        super(service);
    }

    @Override
    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
        LOGGER.warn("AVTransport event subscription failed.", exception);
    }

    @Override
    protected void established(GENASubscription subscription) {
        LOGGER.info("AVTransport event subscription established.");
    }

    @Override
    protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
        LOGGER.info("AVTransport event subscription ended: \"" + reason + "\".");
    }

    @Override
    protected void eventReceived(GENASubscription subscription) {
        Map currentValues = subscription.getCurrentValues();
        if (currentValues.containsKey("LastChange")) {
            String xml = currentValues.get("LastChange").toString();
            try {
                LastChange lastChange = new LastChange(new AVTransportLastChangeParser(), xml);
                // find changes and call callbacks
            } catch (Exception e) {
                LOGGER.info("Could not parse LastChange event data (" + e.getClass().getSimpleName() + "): \"" + e.getMessage() + "\".");
            }
        }
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        LOGGER.info("AVTransport events missed: " + numberOfMissedEvents + ".");
    }
    
    abstract void handleTransportStateChange(TransportState oldState, TransportState newState);
    
    // TOOD more of them
}
