/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.xml;

import java.util.*;

/**
 * de.codewave.utils.xml.PListHandlerListener
 */
public interface PListHandlerListener {
    boolean beforeDictPut(Map dict, String key, Object value);
    boolean beforeArrayAdd(List array, Object value);
}