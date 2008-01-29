package de.codewave.mytunesrss.remote.render;

/**
 * de.codewave.mytunesrss.remote.render.XmlRpcRenderer
 */
public interface XmlRpcRenderer<R, T> {
    R render(T o);
}