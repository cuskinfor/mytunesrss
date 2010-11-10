/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "motd")
public class MessageOfTheDay {

    @XmlElement(name = "item")
    private List<MessageOfTheDayItem> items;

    public List<MessageOfTheDayItem> getItems() {
        return items;
    }
}
