package de.codewave.mytunesrss.remotecontrol;

import de.codewave.utils.MiscUtils;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.EventedValueEnum;
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
                checkTransportStateChange(myPreviousLastChange.getEventedValue(0, AVTransportVariable.TransportState.class), lastChange.getEventedValue(0, AVTransportVariable.TransportState.class));
                myPreviousLastChange = lastChange;
            } catch (Exception e) {
                LOGGER.info("Could not parse LastChange event data (" + e.getClass().getSimpleName() + "): \"" + e.getMessage() + "\".");
            }
        }
    }

    private void checkTransportStateChange(AVTransportVariable.TransportState oldValue, AVTransportVariable.TransportState newValue) {
        TransportState oldState = oldValue != null ? oldValue.getValue() : null;
        TransportState newState = newValue != null ? newValue.getValue() : null;
        if (isChanged(oldState, newState)) {
            handleTransportStateChange(oldState, newState);
        }
    }

    private boolean isChanged(Object o1, Object o2) {
        return (o1 == null && o2 != null) || (o1 != null && o2 == null) || (o1 != null && o2 != null && !o1.equals(o2));
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        LOGGER.info("AVTransport events missed: " + numberOfMissedEvents + ".");
    }

    abstract void handleTransportStateChange(TransportState oldState, TransportState newState);

}
