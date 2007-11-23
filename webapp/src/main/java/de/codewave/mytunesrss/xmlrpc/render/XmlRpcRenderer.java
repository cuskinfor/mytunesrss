package de.codewave.mytunesrss.xmlrpc.render;

/**
 * de.codewave.mytunesrss.xmlrpc.render.XmlRpcRenderer
 */
public interface XmlRpcRenderer<R, T> {
    R render(T o);
}