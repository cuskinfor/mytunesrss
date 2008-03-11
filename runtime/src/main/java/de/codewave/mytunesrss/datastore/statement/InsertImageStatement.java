package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class InsertImageStatement extends InsertOrUpdateImageStatement {
    public InsertImageStatement(String trackId, int size, byte[] data) {
        super(trackId, size, data);
    }

    protected String getStatementName() {
        return "insertImage";
    }
}