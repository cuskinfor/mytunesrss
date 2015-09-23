/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.sql;

import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * de.codewave.utils.sql.SmartStatementDescription
 */
public class SmartStatementDescription {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartStatementDescription.class);

    private List<SmartSql> mySqls = new ArrayList<SmartSql>();
    private Map<String, Object> myDefaults = new HashMap<String, Object>();
    private Map<String, String> myParamTypes = new HashMap<String, String>();

    SmartStatementDescription(JXPathContext context, Map<String, String> fragments) {
        for (Iterator<JXPathContext> sqlContextIterator = JXPathUtils.getContextIterator(context, "sql"); sqlContextIterator.hasNext();) {
            JXPathContext sqlContext = sqlContextIterator.next();
            String sql = JXPathUtils.getStringValue(sqlContext, ".", "").trim();
            boolean replaced;
            do {
                replaced = false;
                for (Map.Entry<String, String> fragment : fragments.entrySet()) {
                    if (sql.contains("{" + fragment.getKey() + "}")) {
                        sql = sql.replace("{" + fragment.getKey() + "}", fragment.getValue());
                        replaced = true;
                    }
                }
            } while (replaced);
            mySqls.add(new SmartSql(sql, JXPathUtils.getStringValue(sqlContext, "@loop", null), JXPathUtils.getIntValue(sqlContext, "@loopbatch", 0), JXPathUtils.getIntValue(sqlContext, "@loopcommit", 0)));
        }
        for (Iterator<JXPathContext> defaultContextIterator = JXPathUtils.getContextIterator(context, "default"); defaultContextIterator.hasNext();) {
            JXPathContext defaultContext = defaultContextIterator.next();
            String name = JXPathUtils.getStringValue(defaultContext, "@name", "").trim();
            String value = JXPathUtils.getStringValue(defaultContext, "@value", null);
            if (value != null) {
                value = value.trim();
            }
            String type = JXPathUtils.getStringValue(defaultContext, "@type", "java.lang.String").trim();
            myDefaults.put(name, value != null ? createValueObject(type, value) : null);
            myParamTypes.put(name, type);
        }
        for (Iterator<JXPathContext> defaultContextIterator = JXPathUtils.getContextIterator(context, "parameter"); defaultContextIterator.hasNext();) {
            JXPathContext defaultContext = defaultContextIterator.next();
            String name = JXPathUtils.getStringValue(defaultContext, "@name", "").trim();
            String type = JXPathUtils.getStringValue(defaultContext, "@type", "java.lang.String").trim();
            if (StringUtils.isNotEmpty(name)) {
                myParamTypes.put(name, type);
            }
        }
    }

    private Object createValueObject(String type, String value) {
        try {
            return Class.forName(type).getConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create default value object for type \"" + type + "\" and value \"" + value + "\".", e);
            }
            return null;
        }
    }

    Map<String, Object> getDefaults() {
        return myDefaults;
    }

    String getParamType(String name) {
        return myParamTypes.get(name);
    }

    List<SmartSql> getSqls() {
        List<SmartSql> list = new ArrayList<SmartSql>();
        for (SmartSql sql : mySqls) {
            list.add(new SmartSql(sql));
        }
        return list;
    }
}
