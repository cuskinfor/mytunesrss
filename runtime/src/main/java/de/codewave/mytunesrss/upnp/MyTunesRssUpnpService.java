package de.codewave.mytunesrss.upnp;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.RegistrationFeedback;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventListener;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.mediaserver.MyTunesRssContentDirectoryService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.*;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.igd.callback.PortMappingAdd;
import org.fourthline.cling.support.igd.callback.PortMappingDelete;
import org.fourthline.cling.support.model.PortMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MyTunesRssUpnpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUpnpService.class);
    private static final UDN UDN_MEDIASERVER = UDN.uniqueSystemIdentifier("MyTunesRSS-MediaServer");
    public static final String NAME_USER_MAPPING_HTTP = "MyTunesRSS User Interface";
    public static final String NAME_USER_MAPPING_HTTPS = "MyTunesRSS User Interface (secure)";
    public static final String NAME_ADMIN_MAPPING = "MyTunesRSS Admin Interface";

    private UpnpService myClingService;
    private MyTunesRssEventListener myUpnpDatabaseUpdateListener;
    private Set<RemoteDevice> myMediaRenderers = new HashSet<>();
    private Set<RemoteDevice> myInternetGatewayDevices = new HashSet<>();

    public void start() throws ValidationException, IOException {
        RegistrationFeedback feedback = MyTunesRssUtils.getRegistrationFeedback(Locale.getDefault());
        if (feedback == null || feedback.isValid()) {
            myClingService = new UpnpServiceImpl();
            addMediaRendererListener();
            addInternetGatewayDeviceListener();
            LOGGER.debug("Initial media renderer search.");
            myClingService.getControlPoint().search();
        } else {
            LOGGER.warn("Invalid/expired license, not starting UPnP Media Server.");
        }
    }

    public void shutdown() {
        try {
            LOGGER.info("Shutting down UPnP service.");
            myClingService.shutdown();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not complete shutdown hook for UPnP service.", e);
        }
    }

    private void addMediaRendererListener() {
        myClingService.getRegistry().addListener(new MediaRendererRegistryListener(new DeviceRegistryCallback() {
            @Override
            public void add(RemoteDevice device) {
                synchronized (myMediaRenderers) {
                    myMediaRenderers.add(device);
                }
            }

            @Override
            public void remove(RemoteDevice device) {
                synchronized (myMediaRenderers) {
                    myMediaRenderers.remove(device);
                }
            }
        }));
    }

    public Set<RemoteDevice> getMediaRenders() {
        synchronized (myMediaRenderers) {
            return new HashSet<>(myMediaRenderers);
        }
    }

    private void addInternetGatewayDeviceListener() {
        myClingService.getRegistry().addListener(new InternetGatewayDeviceRegistryListener(new DeviceRegistryCallback() {
            @Override
            public void add(RemoteDevice device) {
                synchronized (myInternetGatewayDevices) {
                    myInternetGatewayDevices.add(device);
                }
            }

            @Override
            public void remove(RemoteDevice device) {
                synchronized (myInternetGatewayDevices) {
                    myInternetGatewayDevices.remove(device);
                }
            }
        }));
    }

    public Set<RemoteDevice> getInternetGatewayDevices() {
        synchronized (myInternetGatewayDevices) {
            return new HashSet<>(myInternetGatewayDevices);
        }
    }

    public void addInternetGatewayDevicePortMapping(final int port, final String name) {
        for (final RemoteDevice device : getInternetGatewayDevices()) {
            RemoteService service = device.findService(new UDAServiceId("WANIPConnection"));
            String hostAddress = device.getIdentity().getDiscoveredOnLocalAddress().getHostAddress();
            PortMapping desiredMapping = new PortMapping(port, hostAddress, PortMapping.Protocol.TCP, name);
            myClingService.getControlPoint().execute(new PortMappingAdd(service, desiredMapping) {
                @Override
                public void success(ActionInvocation invocation) {
                    LOGGER.info("Added port mapping \"" + name + "\" (" + port + " -> " + hashCode() + ":" + port + ") to device \"" + device.getDisplayString() + "\".");
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not add port mapping \"" + name + "\" (" + port + " -> " + hashCode() + ":" + port + ") to device \"" + device.getDisplayString() + "\": \"" + defaultMsg + "\".");
                }
            });
        }
    }

    public void removeInternetGatewayDevicePortMapping(final int port) {
        for (final RemoteDevice device : getInternetGatewayDevices()) {
            RemoteService service = device.findService(new UDAServiceId("WANIPConnection"));
            String hostAddress = device.getIdentity().getDiscoveredOnLocalAddress().getHostAddress();
            PortMapping desiredMapping = new PortMapping(port, hostAddress, PortMapping.Protocol.TCP);
            myClingService.getControlPoint().execute(new PortMappingDelete(service, desiredMapping) {
                @Override
                public void success(ActionInvocation invocation) {
                    LOGGER.info("Removed port mapping (" + port + " -> " + hashCode() + ":" + port + ") from device \"" + device.getDisplayString() + "\".");
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not remove port mapping (" + port + " -> " + hashCode() + ":" + port + ") from device \"" + device.getDisplayString() + "\": \"" + defaultMsg + "\".");
                }
            });
        }
    }

    public void startMediaServer() {
        RegistrationFeedback feedback = MyTunesRssUtils.getRegistrationFeedback(Locale.getDefault());
        if ((feedback == null || feedback.isValid()) && MyTunesRss.CONFIG.isUpnpMediaServerActive()) {
            final LocalService<MyTunesRssContentDirectoryService> directoryService = new AnnotationLocalServiceBinder().read(MyTunesRssContentDirectoryService.class);
            myUpnpDatabaseUpdateListener = new MyTunesRssEventListener() {
                @Override
                public void handleEvent(MyTunesRssEvent event) {
                    if (event.getType() == MyTunesRssEvent.EventType.MEDIA_SERVER_UPDATE) {
                        MyTunesRssContentDirectoryService contentDirectoryService = directoryService.getManager().getImplementation();
                        String oldId = contentDirectoryService.getSystemUpdateID().toString();
                        contentDirectoryService.changeSystemUpdateID();
                        String newId = contentDirectoryService.getSystemUpdateID().toString();
                        LOGGER.info("Changing media server system update ID from {} to {}.", oldId, newId);
                    }
                }
            };
            DeviceIdentity identity = new DeviceIdentity(UDN_MEDIASERVER);
            DeviceType type = new UDADeviceType("MediaServer", 1);
            String mediaServerName = MyTunesRss.CONFIG.getUpnpMediaServerName();
            if (StringUtils.isBlank(mediaServerName)) {
                try {
                    mediaServerName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    LOGGER.debug("Could not get hostname.", e);
                }
            }
            DeviceDetails details = new DeviceDetails(StringUtils.isNotBlank(mediaServerName) ? "MyTunesRSS: " + mediaServerName : "MyTunesRSS", new ManufacturerDetails("Codewave Software"), new ModelDetails("MyTunesRSS", "MyTunesRSS Media Server", MyTunesRss.VERSION));
            org.fourthline.cling.model.meta.Icon icon = null;
            try {
                File tempFile = File.createTempFile("mytunesrss-mediaserver-", ".png");
                try (InputStream is = MyTunesRss.class.getResourceAsStream("/de/codewave/mytunesrss/mediaserver48.png"); OutputStream os = new FileOutputStream(tempFile)) {
                    IOUtils.copyLarge(is, os);
                }
                icon = new org.fourthline.cling.model.meta.Icon("image/png", 48, 48, 8, tempFile);
            } catch (RuntimeException | IOException e) {
                LOGGER.warn("Could not create icon for UPnP Media Server.", e);
            }
            directoryService.setManager(new DefaultServiceManager(directoryService, MyTunesRssContentDirectoryService.class) {
                @Override
                protected int getLockTimeoutMillis() {
                    return MyTunesRss.CONFIG.getUpnpMediaServerLockTimeoutSeconds() * 1000;
                }
            });
            LocalService<ConnectionManagerService> connectionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
            connectionManagerService.setManager(new DefaultServiceManager<>(connectionManagerService, ConnectionManagerService.class));
            try {
                myClingService.getRegistry().addDevice(new LocalDevice(identity, type, details, icon, new LocalService[]{directoryService, connectionManagerService}));
                MyTunesRssEventManager.getInstance().addListener(myUpnpDatabaseUpdateListener);
            } catch (ValidationException e) {
                LOGGER.warn("Could not add UPnP Media Server device.");
            }
        }
    }

    public void shutdownMediaServer() {
        LOGGER.info("Shutting down UPnP Media Server.");
        MyTunesRssEventManager.getInstance().removeListener(myUpnpDatabaseUpdateListener);
        myUpnpDatabaseUpdateListener = null;
        myClingService.getRegistry().removeDevice(UDN_MEDIASERVER);
    }
}
