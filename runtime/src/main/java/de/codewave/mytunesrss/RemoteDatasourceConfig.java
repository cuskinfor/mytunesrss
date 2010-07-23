/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class RemoteDatasourceConfig extends DatasourceConfig {

    public RemoteDatasourceConfig(String definition) {
        super(definition);
    }

    public RemoteDatasourceConfig(RemoteDatasourceConfig source) {
        super(source);
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Remote;
    }
}