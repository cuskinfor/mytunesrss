package de.codewave.mytunesrss.statistics;

import java.io.Externalizable;

public interface StatisticsEvent extends Externalizable {
    String getUser();
}
