package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class UpdateImageStatement extends InsertOrUpdateImageStatement {
    public UpdateImageStatement(String trackId, int size, byte[] data) {
        super(trackId, size, data);
    }

    protected String getStatementName() {
        return "updateImage";
    }
}