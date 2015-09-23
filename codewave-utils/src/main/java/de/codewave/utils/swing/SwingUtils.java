/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.swing;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * de.codewave.utils.swing.SwingUtils
 */
public class SwingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwingUtils.class);

    public static void showMessage(final JFrame parent, final int type, final String title, final String message, final int messageMaxLength) {
        invokeAndWait(new Runnable() {
            public void run() {
                JOptionPane pane = createPane(type, message, messageMaxLength);
                JDialog dialog = pane.createDialog(parent, title);
                dialog.setVisible(true);
            }
        });
    }

    private static JOptionPane createPane(int type, String message, int messageMaxLength) {
        JOptionPane pane = createMaxLengthOptionPane(messageMaxLength);
        pane.setMessageType(type);
        pane.setMessage(message);
        return pane;
    }

    public static Object showOptionsMessage(final JFrame parent, final int type, final String title, final String message, final int messageMaxLength,
                                            final Object[] options) {
        final Set<Object> returnHolder = new HashSet<Object>();
        invokeAndWait(new Runnable() {
            public void run() {
                JOptionPane pane = createPane(type, message, messageMaxLength);
                pane.setOptions(options);
                JDialog dialog = pane.createDialog(parent, title);
                dialog.setVisible(true);
                returnHolder.add(pane.getValue());
            }
        });
        return returnHolder.size() == 1 ? returnHolder.iterator().next() : null;
    }

    public static void enableElementAndLabel(final JComponent element, final boolean enabled) {
        invokeAndWait(new Runnable() {
            public void run() {
                element.setEnabled(enabled);
                Component[] components = element.getParent().getComponents();
                if (components != null && components.length > 0) {
                    for (int i = 0; i < components.length; i++) {
                        if (components[i] instanceof JLabel && ((JLabel) components[i]).getLabelFor() == element) {
                            components[i].setEnabled(enabled);
                        }
                    }
                }
            }
        });
    }

    public static void removeEmptyTooltips(final JComponent component) {
        invokeAndWait(new Runnable() {
            public void run() {
                removeEmptyTooltipsInternal(component);
            }
        });
    }

    public static void removeEmptyTooltipsInternal(JComponent component) {
        String toolTipText = component.getToolTipText();
        if (toolTipText != null && StringUtils.isEmpty(toolTipText.trim())) {
            component.setToolTipText(null);
        }
        Component[] childComponents = component.getComponents();
        for (int i = 0; i < childComponents.length; i++) {
            if (childComponents[i] instanceof JComponent) {
                removeEmptyTooltipsInternal((JComponent) childComponents[i]);
            }
        }
    }

    public static JOptionPane createMaxLengthOptionPane(final int maxLength) {
        return new JOptionPane() {
            @Override
            public int getMaxCharactersPerLineCount() {
                return maxLength;
            }
        };
    }

    public static void invokeAndWait(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // keep interrupted indicator
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Invoke and wait failed.", e);
                }
            } catch (InvocationTargetException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Invoke and wait failed.", e);
                }
            }
        }
    }

    public static void packAndShow(final Dialog dialog) {
        syncEventQueue();
        invokeAndWait(new Runnable() {
            public void run() {
                final Point location = dialog.getLocation();
                dialog.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
                if (dialog.isModal()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dialog.dispose();
                            dialog.pack();
                            dialog.setLocation(location);
                        }
                    });
                    dialog.setVisible(true);
                }
                dialog.setVisible(true);
                if (!dialog.isModal()) {
                    dialog.pack();
                    dialog.setLocation(location);
                }
            }
        });
    }

    public static void packAndShow(final Frame frame) {
        syncEventQueue();
        invokeAndWait(new Runnable() {
            public void run() {
                final Point location = frame.getLocation();
                frame.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
                frame.setVisible(true);
                frame.pack();
                frame.setLocation(location);
            }
        });
    }

    public static void packAndShowRelativeTo(final Dialog dialog, final Component component) {
        syncEventQueue();
        invokeAndWait(new Runnable() {
            public void run() {
                dialog.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
                if (dialog.isModal()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dialog.dispose();
                            dialog.pack();
                            dialog.setLocationRelativeTo(component);
                        }
                    });
                    dialog.setVisible(true);
                }
                dialog.setVisible(true);
                if (!dialog.isModal()) {
                    dialog.pack();
                    dialog.setLocationRelativeTo(component);
                }
            }
        });
    }

    public static void packAndShowRelativeTo(final Frame frame, final Component component) {
        syncEventQueue();
        invokeAndWait(new Runnable() {
            public void run() {
                frame.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
                frame.setVisible(true);
                frame.pack();
                if (component == null) {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
                } else {
                    frame.setLocationRelativeTo(component);
                }
            }
        });
    }

    public static void syncEventQueue() {
        if (SwingUtilities.isEventDispatchThread()) {
            final JDialog dialog = new JDialog();
            dialog.setModal(true);
            dialog.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.dispose();
                }
            });
            dialog.setVisible(true);
        }
    }

    /**
     * Look for a JDialog or JFrame in the parent hierarchy of this specified container
     * and return that JDialog or JFrame. If none is found the method returns <code>null</code>.
     *
     * @param container The container to start looking for a JDialog or JFrame in the parent hierarchy.
     * @return The JDialog or JFrame found or <code>null</code> if none was found.
     */
    public static Object findParentDialogOrFrame(Container container) {
        while (container != null) {
            if (container instanceof JFrame || container instanceof JDialog) {
                return container;
            }
            container = container.getParent();
        }
        return null;
    }
}
