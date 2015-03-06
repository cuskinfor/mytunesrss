package de.codewave.mytunesrss.mediarenderercontrol;

import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.TransportState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

public abstract class AvTransportLastChangeSubscriptionCallback extends SubscriptionCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvTransportLastChangeSubscriptionCallback.class);

    private TransportState myTransportState = TransportState.CUSTOM;
    private URI myTransportUri;

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
        Long eventSequence = subscription.getCurrentSequence().getValue();
        LOGGER.debug("Received GENA event #" + eventSequence + ".");
        Map currentValues = subscription.getCurrentValues();
        if (currentValues.containsKey("LastChange")) {
            String xml = currentValues.get("LastChange").toString();
            try {
                LastChange lastChange = new LastChange(new AVTransportLastChangeParser(), xml);
                LOGGER.debug("Received last changed data in GENA event #" + eventSequence + ".");
                AVTransportVariable.TransportState transportStateEventedValue = lastChange.getEventedValue(0, AVTransportVariable.TransportState.class);
                if (transportStateEventedValue != null) {
                    TransportState transportState = transportStateEventedValue.getValue();
                    if (transportState == null) {
                        transportState = TransportState.CUSTOM;
                    }
                    if (myTransportState != transportState) {
                        try {
                            handleTransportStateChange(myTransportState, transportState);
                        } finally {
                            myTransportState = transportState;
                        }
                    }
                }
                AVTransportVariable.AVTransportURI transportUriEventedValue = lastChange.getEventedValue(0, AVTransportVariable.AVTransportURI.class);
                if (transportUriEventedValue != null) {
                    URI transportUri = transportUriEventedValue.getValue();
                    if ((myTransportUri == null && transportUri != null) || (myTransportUri != null && transportUri == null) || (myTransportUri != null && !myTransportUri.equals(transportUri))) {
                        try {
                            handleTransportUriChange(transportUri);
                        } finally {
                            myTransportUri = transportUri;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Could not parse last change data (" + e.getClass().getName() + "): \"" + e.getMessage() + "\".");
            }
        }
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        LOGGER.info("AVTransport events missed: " + numberOfMissedEvents + ".");
    }

    abstract void handleTransportStateChange(TransportState previousTransportState, TransportState currentTransportState);

    protected abstract void handleTransportUriChange(URI currentTransportUri);

}
