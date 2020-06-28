package com.helidon.se.persistence.context;

import oracle.jdbc.OracleConnection;

public interface DBAction<T> {

    T accept(OracleConnection commection) throws Exception;
}
