package de.codewave.utils.sql;

import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.utils.sql.SmartStatementFactory
 */
public class SmartStatementFactory {
    public static SmartStatementFactory getInstance(JXPathContext... configurations) {
        return new SmartStatementFactory(configurations);
    }

    private Map<String, SmartStatementDescription> myStatementDescriptions = new HashMap<String, SmartStatementDescription>();

    private SmartStatementFactory(JXPathContext... configurations) {
        Map<String, String> fragments = new HashMap<String, String>();
        for (JXPathContext configuration : configurations) {
            for (Iterator<JXPathContext> iterator = JXPathUtils.getContextIterator(configuration, "/statements/fragment"); iterator.hasNext();) {
                JXPathContext fragmentContext = iterator.next();
                fragments.put(JXPathUtils.getStringValue(fragmentContext, "@name", "dummy"), JXPathUtils.getStringValue(fragmentContext,
                                                                                                                        ".",
                                                                                                                        "").trim());
            }
        }
        for (JXPathContext configuration : configurations) {
            for (Iterator<JXPathContext> iterator = JXPathUtils.getContextIterator(configuration, "/statements/statement"); iterator.hasNext();) {
                JXPathContext statement = iterator.next();
                String statementName = JXPathUtils.getStringValue(statement, "@name", "dummy");
                myStatementDescriptions.put(statementName, new SmartStatementDescription(statement, fragments));
            }
        }
    }

    List<SmartSql> getSqls(String name) {
        return new ArrayList<SmartSql>(myStatementDescriptions.get(name).getSqls());
    }

    public SmartStatement createStatement(Connection connection, String name, Map<String, Boolean> conditionals) throws SQLException {
        SmartStatementDescription description = myStatementDescriptions.get(name);
        if (description != null) {
            return new SmartStatement(name, connection, description, conditionals);
        } else {
            throw new IllegalArgumentException("No smart statement with name \"" + name + "\" found.");
        }
    }
}
