package org.vaadin.henrik.refresher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.henrik.refresher.client.ui.VRefresher;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.vaadin.henrik.refresher.client.ui.VRefresher.class)
public class Refresher extends AbstractComponent {

    public interface RefreshListener extends Serializable {
        public void refresh(Refresher source);
    }

    private static final long serialVersionUID = -2818447361687554688L;

    private final List<RefreshListener> refreshListeners = new ArrayList<RefreshListener>();

    private long refreshIntervalInMillis = -1;

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        target.addAttribute("pollinginterval", refreshIntervalInMillis);
    }

    /**
     * Define a refresh interval.
     * 
     * @param intervalInMillis
     *            The desired refresh interval in milliseconds. An interval of
     *            zero or less temporarily inactivates the refresh.
     */
    public void setRefreshInterval(final long intervalInMillis) {
        refreshIntervalInMillis = intervalInMillis;
        requestRepaint();
    }

    /**
     * Get the currently used refreshing interval.
     * 
     * @return The refresh interval in milliseconds. A result of zero or less
     *         means that the refresher is currently inactive.
     */
    public long getRefreshInterval() {
        return refreshIntervalInMillis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void changeVariables(final Object source, final Map variables) {
        super.changeVariables(source, variables);

        if (variables.containsKey(VRefresher.VARIABLE_REFRESH_EVENT)) {
            fireRefreshEvents();
        }
    }

    private void fireRefreshEvents() {
        for (final RefreshListener listener : refreshListeners) {
            listener.refresh(this);
        }
    }

    public boolean addListener(final RefreshListener listener) {
        if (listener != null) {
            return refreshListeners.add(listener);
        } else {
            return false;
        }
    }

    public boolean removeListener(final RefreshListener listener) {
        return refreshListeners.remove(listener);
    }

}
