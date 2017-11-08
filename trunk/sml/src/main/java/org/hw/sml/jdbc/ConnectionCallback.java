package org.hw.sml.jdbc;

import java.sql.Connection;

public interface ConnectionCallback<T> {
	public T doInConnection(Connection conn) ;
}
