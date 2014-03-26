package de.codewave.mytunesrss.upnp;

import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternetGatewayDeviceRegistryListener extends DefaultRegistryListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternetGatewayDeviceRegistryListener.class);

    private DeviceRegistryCallback myCallback;

    public InternetGatewayDeviceRegistryListener(DeviceRegistryCallback callback) {
        myCallback = callback;
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        if (isInternetGatewayDevice(device)) {
            LOGGER.info("Adding internet gateway device: " + device.getIdentity());
            myCallback.add(device);
        }
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        LOGGER.info("Removing internet gateway device: " + device.getIdentity());
        myCallback.remove(device);
    }

    private boolean isInternetGatewayDevice(RemoteDevice device) {
        return "InternetGatewayDevice".equals(device.getType().getType()) && device.getType().getVersion() == 1 && device.findService(new UDAServiceType("WANIPConnection", 1)) != null;
    }
}
