package de.codewave.mytunesrss.mediaserver;

import org.fourthline.cling.binding.annotations.*;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.model.types.csv.CSVString;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

@UpnpService(
        serviceId = @UpnpServiceId("ContentDirectory"),
        serviceType = @UpnpServiceType(value = "ContentDirectory", version = 1)
)

@UpnpStateVariables({
        @UpnpStateVariable(
                name = "A_ARG_TYPE_ObjectID",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_Result",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_BrowseFlag",
                sendEvents = false,
                datatype = "string",
                allowedValuesEnum = BrowseFlag.class),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_Filter",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_SortCriteria",
                sendEvents = false,
                datatype = "string"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_Index",
                sendEvents = false,
                datatype = "ui4"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_Count",
                sendEvents = false,
                datatype = "ui4"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_UpdateID",
                sendEvents = false,
                datatype = "ui4"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_URI",
                sendEvents = false,
                datatype = "uri"),
        @UpnpStateVariable(
                name = "A_ARG_TYPE_SearchCriteria",
                sendEvents = false,
                datatype = "string")
})
public abstract class AbstractContentDirectoryService {

    static final ThreadLocal<RemoteClientInfo> REMOTE_CLIENT_INFO = new ThreadLocal<>();
    
    @UpnpStateVariable(sendEvents = false)
    private final CSV<String> searchCapabilities;

    @UpnpStateVariable(sendEvents = false)
    private final CSV<String> sortCapabilities;

    @UpnpStateVariable(
            sendEvents = true,
            defaultValue = "0",
            eventMaximumRateMilliseconds = 200
    )
    private UnsignedIntegerFourBytes systemUpdateID = new UnsignedIntegerFourBytes(0);

    protected final PropertyChangeSupport propertyChangeSupport;

    protected AbstractContentDirectoryService() {
        this(new ArrayList<String>(), new ArrayList<String>(), null);
    }

    protected AbstractContentDirectoryService(List<String> searchCapabilities, List<String> sortCapabilities) {
        this(searchCapabilities, sortCapabilities, null);
    }

    protected AbstractContentDirectoryService(List<String> searchCapabilities, List<String> sortCapabilities,
                                              PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport != null ? propertyChangeSupport : new PropertyChangeSupport(this);
        this.searchCapabilities = new CSVString();
        this.searchCapabilities.addAll(searchCapabilities);
        this.sortCapabilities = new CSVString();
        this.sortCapabilities.addAll(sortCapabilities);
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "SearchCaps"))
    public CSV<String> getSearchCapabilities() {
        return searchCapabilities;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "SortCaps"))
    public CSV<String> getSortCapabilities() {
        return sortCapabilities;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "Id"))
    public synchronized UnsignedIntegerFourBytes getSystemUpdateID() {
        return systemUpdateID;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    /**
     * Call this method after making changes to your content directory.
     * <p>
     * This will notify clients that their view of the content directory is potentially
     * outdated and has to be refreshed.
     * </p>
     */
    protected synchronized void changeSystemUpdateID() {
        Long oldUpdateID = getSystemUpdateID().getValue();
        systemUpdateID.increment(true);
        getPropertyChangeSupport().firePropertyChange(
                "SystemUpdateID",
                oldUpdateID,
                getSystemUpdateID().getValue()
        );
    }

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "Result",
                    stateVariable = "A_ARG_TYPE_Result",
                    getterName = "getResult"),
            @UpnpOutputArgument(name = "NumberReturned",
                    stateVariable = "A_ARG_TYPE_Count",
                    getterName = "getCount"),
            @UpnpOutputArgument(name = "TotalMatches",
                    stateVariable = "A_ARG_TYPE_Count",
                    getterName = "getTotalMatches"),
            @UpnpOutputArgument(name = "UpdateID",
                    stateVariable = "A_ARG_TYPE_UpdateID",
                    getterName = "getContainerUpdateID")
    })
    public BrowseResult browse(
            @UpnpInputArgument(name = "ObjectID", aliases = "ContainerID") String objectId,
            @UpnpInputArgument(name = "BrowseFlag") String browseFlag,
            @UpnpInputArgument(name = "Filter") String filter,
            @UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_Index") UnsignedIntegerFourBytes firstResult,
            @UpnpInputArgument(name = "RequestedCount", stateVariable = "A_ARG_TYPE_Count") UnsignedIntegerFourBytes maxResults,
            @UpnpInputArgument(name = "SortCriteria") String orderBy,
            RemoteClientInfo remoteClientInfo)
            throws ContentDirectoryException {

        REMOTE_CLIENT_INFO.set(remoteClientInfo);
        SortCriterion[] orderByCriteria;
        try {
            try {
                orderByCriteria = SortCriterion.valueOf(orderBy);
            } catch (RuntimeException ex) {
                throw new ContentDirectoryException(ContentDirectoryErrorCode.UNSUPPORTED_SORT_CRITERIA, ex.toString());
            }

            try {
                return browse(
                        objectId,
                        BrowseFlag.valueOrNullOf(browseFlag),
                        filter,
                        firstResult.getValue(), maxResults.getValue(),
                        orderByCriteria
                );
            } catch (RuntimeException ex) {
                throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
            }
        } finally {
            REMOTE_CLIENT_INFO.remove();
        }
    }

    /**
     * Implement this method to implement browsing of your content.
     * <p>
     * This is a required action defined by <em>ContentDirectory:1</em>.
     * </p>
     * <p>
     * You should wrap any exception into a {@link ContentDirectoryException}, so a propery
     * error message can be returned to control points.
     * </p>
     */
    public abstract BrowseResult browse(String objectID, BrowseFlag browseFlag,
                                        String filter,
                                        long firstResult, long maxResults,
                                        SortCriterion[] orderby) throws ContentDirectoryException;


    @UpnpAction(out = {
            @UpnpOutputArgument(name = "Result",
                    stateVariable = "A_ARG_TYPE_Result",
                    getterName = "getResult"),
            @UpnpOutputArgument(name = "NumberReturned",
                    stateVariable = "A_ARG_TYPE_Count",
                    getterName = "getCount"),
            @UpnpOutputArgument(name = "TotalMatches",
                    stateVariable = "A_ARG_TYPE_Count",
                    getterName = "getTotalMatches"),
            @UpnpOutputArgument(name = "UpdateID",
                    stateVariable = "A_ARG_TYPE_UpdateID",
                    getterName = "getContainerUpdateID")
    })
    public BrowseResult search(
            @UpnpInputArgument(name = "ContainerID", stateVariable = "A_ARG_TYPE_ObjectID") String containerId,
            @UpnpInputArgument(name = "SearchCriteria") String searchCriteria,
            @UpnpInputArgument(name = "Filter") String filter,
            @UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_Index") UnsignedIntegerFourBytes firstResult,
            @UpnpInputArgument(name = "RequestedCount", stateVariable = "A_ARG_TYPE_Count") UnsignedIntegerFourBytes maxResults,
            @UpnpInputArgument(name = "SortCriteria") String orderBy,
            RemoteClientInfo remoteClientInfo)
            throws ContentDirectoryException {

        REMOTE_CLIENT_INFO.set(remoteClientInfo);
        try {
            SortCriterion[] orderByCriteria;
            try {
                orderByCriteria = SortCriterion.valueOf(orderBy);
            } catch (RuntimeException ex) {
                throw new ContentDirectoryException(ContentDirectoryErrorCode.UNSUPPORTED_SORT_CRITERIA, ex.toString());
            }

            try {
                return search(
                        containerId,
                        searchCriteria,
                        filter,
                        firstResult.getValue(), maxResults.getValue(),
                        orderByCriteria
                );
            } catch (RuntimeException ex) {
                throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
            }
        } finally {
            REMOTE_CLIENT_INFO.remove();
        }
    }

    /**
     * Override this method to implement searching of your content.
     * <p>
     * The default implementation returns an empty result.
     * </p>
     */
    public BrowseResult search(String containerId, String searchCriteria, String filter,
                               long firstResult, long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {

        try {
            return new BrowseResult(new DIDLParser().generate(new DIDLContent()), 0, 0);
        } catch (Exception ex) {
            throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
        }
    }

}
