package de.codewave.utils.swing;

/**
 * de.codewave.utils.swing.Task
 */
public abstract class Task {
    /**
     * Thread which is executing the task. Only set if the task executor was used to start the task.
     */
    private Thread myExecutionThread;

    /**
     * The worker method of the task.
     *
     * @throws Exception
     */
    public abstract void execute() throws Exception;

    /**
     * Handle an exception that occured while executing the worker method.
     *
     * @param e
     */
    public void handleException(Exception e) {
        Thread thread = Thread.currentThread();
        if (thread.getUncaughtExceptionHandler() != null) {
            thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
        } else if (thread.getThreadGroup() != null) {
            thread.getThreadGroup().uncaughtException(thread, e);
        } else {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the thread executing this task. Set by the task executor.
     *
     * @param thread The executing thread.
     */
    protected void setExecutionThread(Thread thread) {
        myExecutionThread = thread;
    }

    /**
     * Get the thread executing this task.
     *
     * @return The executing thread.
     */
    protected Thread getExecutionThread() {
        return myExecutionThread;
    }

    /**
     * Interrupt the execution thread of this task. If the execution thread was not set before, this method does nothing and simply returns.
     */
    public void interrupt() {
        if (myExecutionThread != null) {
            myExecutionThread.interrupt();
        }
    }
}