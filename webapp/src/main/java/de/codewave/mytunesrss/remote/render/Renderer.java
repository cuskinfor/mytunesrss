package de.codewave.mytunesrss.remote.render;

/**
 * de.codewave.mytunesrss.remote.render.Renderer
 */
public interface Renderer<R, T> {
    R render(T o);
}