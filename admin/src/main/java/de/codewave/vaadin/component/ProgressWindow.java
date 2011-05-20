package de.codewave.vaadin.component;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.*;

public class ProgressWindow extends Window implements Refresher.RefreshListener {

    public static interface Task extends Runnable {
        int getProgress();
        void onWindowClosed();
    }

    private Task myTask;

    private Thread myThread;

    private Object myProgressBar;

    public ProgressWindow(float width, int units, Resource icon, String caption, String message, boolean progressBar, long refreshIntervalMillis, Task task) {
        if (caption != null) {
            setCaption(caption);
        }
        if (icon != null) {
            setIcon(icon);
        }
        setWidth(width, units);
        setModal(true);
        setClosable(false);
        setResizable(false);
        setDraggable(false);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        setContent(verticalLayout);
        Label label = new Label(message);
        addComponent(label);
        Panel panel = new Panel();
        addComponent(panel);
        verticalLayout.setComponentAlignment(panel, Alignment.MIDDLE_RIGHT);
        panel.addStyleName("light");
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        panel.setContent(horizontalLayout);
        horizontalLayout.setSpacing(true);
        verticalLayout.setComponentAlignment(panel, Alignment.MIDDLE_RIGHT);
        if (progressBar) {
            // TODO: add progress bar
        }
        Refresher refresher = new Refresher();
        addComponent(refresher);
        refresher.setRefreshInterval(refreshIntervalMillis);
        refresher.addListener(this);
        myTask = task;
        myThread = new Thread(task);
        myThread.start();
    }

    public void show(Window parent) {
        parent.addWindow(this);
    }

    public void refresh(Refresher refresher) {
        if (myProgressBar != null) {
            int progress = myTask.getProgress();
            // TODO: refresh progress
        }
        if (!myThread.isAlive()) {
            getParent().removeWindow(this);
            myTask.onWindowClosed();
        }
    }
}
