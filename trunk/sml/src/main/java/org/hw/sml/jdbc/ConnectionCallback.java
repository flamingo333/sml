package org.hw.sml.jdbc;

import java.sql.Connection;

public interface ConnectionCallback<T> {
	T doInConnection(Connection conn) ;
}
