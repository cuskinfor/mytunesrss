package de.codewave.mytunesrss.upnp;

import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaRendererRegistryListener extends DefaultRegistryListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaRendererRegistryListener.class);

    private DeviceRegistryCallback myCallback;

    public MediaRendererRegistryListener(DeviceRegistryCallback callback) {
        myCallback = callback;
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        if (isMediaRendererWithAvTransport(device)) {
            LOGGER.info("Adding media renderer: " + device.getIdentity());
            myCallback.add(device);
        }
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        LOGGER.info("Removing media renderer: " + device.getIdentity());
        myCallback.remove(device);
    }

    private boolean isMediaRendererWithAvTransport(RemoteDevice device) {
        return "MediaRenderer".equals(device.getType().getType()) && device.getType().getVersion() == 1 && device.findService(new UDAServiceType("AVTransport", 1)) != null && device.findService(new UDAServiceType("RenderingControl", 1)) != null;
    }
}
