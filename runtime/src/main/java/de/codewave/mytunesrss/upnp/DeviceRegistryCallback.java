package de.codewave.mytunesrss.upnp;

import org.fourthline.cling.model.meta.RemoteDevice;

public interface DeviceRegistryCallback {

    void add(RemoteDevice device);

    void remove(RemoteDevice device);

}
