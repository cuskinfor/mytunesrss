package de.codewave.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventListener;

/**
 * Works on apple systems only but can be compiled on any system since it uses reflection to call any apple specific methods
 * and instantiate apple specific classes.
 */
public class AppleExtensions {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppleExtensions.class);

    public static void activate(final EventListener listener) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        final Method handleQuitMethod = listener.getClass().getMethod("handleQuit");
        final Method handleReOpenApplicationMethod = listener.getClass().getMethod("handleReOpenApplication");
        final Method applicationGetApplication = Class.forName("com.apple.eawt.Application").getMethod("getApplication");
        final Method applicationSetEnabledAboutMenu= Class.forName("com.apple.eawt.Application").getMethod("setEnabledAboutMenu", Boolean.TYPE);
        final Method applicationSetEnabledPreferencesMenu = Class.forName("com.apple.eawt.Application").getMethod("setEnabledPreferencesMenu", Boolean.TYPE);
        final Method applicationAddApplicationListener= Class.forName("com.apple.eawt.Application").getMethod("addApplicationListener", Class.forName("com.apple.eawt.ApplicationListener"));
        Object application = applicationGetApplication.invoke(null);
        applicationSetEnabledAboutMenu.invoke(application, false);
        applicationSetEnabledPreferencesMenu.invoke(application, false);
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                switch (method.getName()) {
                    case "handleReOpenApplication":
                        try {
                            handleReOpenApplicationMethod.invoke(listener);
                        } catch (Exception e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("Could not invoke method for handling apple \"reOpenApplication\".", e);
                            }
                        }
                        break;
                    case "handleQuit":
                        try {
                            handleQuitMethod.invoke(listener);
                        } catch (Exception e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("Could not invoke method for handling apple menu \"quit\".", e);
                            }
                        }
                        break;
                    default:
                        break;
                }
                return null;
            }
        };
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {Class.forName("com.apple.eawt.ApplicationListener")}, invocationHandler);
        applicationAddApplicationListener.invoke(application, proxy);
    }

    public static void setDockIconImage(Image image) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method applicationGetApplication = Class.forName("com.apple.eawt.Application").getMethod("getApplication");
        final Method applicationSetDockIconImage = Class.forName("com.apple.eawt.Application").getMethod("setDockIconImage", Image.class);
        Object application = applicationGetApplication.invoke(null);
        applicationSetDockIconImage.invoke(application, image);
    }
}