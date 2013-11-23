package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.SortOrder;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OffHeapSessionStore {

    public static final String CURRENT_LIST_ID = "currentListId";

    public static OffHeapSessionStore get(HttpServletRequest request) {
        return OffHeapSessionStoreListener.getOffHeapSessionStore(request);
    }

    private String myCurrentListId;
    private List myCurrentList;
    private SortOrder myCurrentSortOrder;

    OffHeapSessionStore() {
        // Only the listener in the same package creates instances.
    }

    public synchronized List getCurrentList(String id) {
        if (StringUtils.equals(id, myCurrentListId)) {
            return myCurrentList;
        }
        return null;
    }

    public synchronized String newCurrentList() {
        myCurrentListId = UUID.randomUUID().toString();
        myCurrentList = new ArrayList();
        return myCurrentListId;
    }

    public synchronized void addToCurrentList(Object o) {
        if (myCurrentList == null) {
            throw new IllegalStateException("No current list available");
        }
        myCurrentList.add(o);
    }

    public synchronized void removeCurrentList() {
        myCurrentListId = null;
        myCurrentList = null;
    }

    public SortOrder getCurrentSortOrder() {
        return myCurrentSortOrder;
    }

    public void setCurrentSortOrder(SortOrder currentSortOrder) {
        myCurrentSortOrder = currentSortOrder;
    }
}
