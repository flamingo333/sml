package org.hw.sml.jdbc.exception;

public class SqlException extends RuntimeException {
	public static boolean isSqlLog;

	private static final long serialVersionUID = 8680655384447511510L;

	public SqlException() {
		super();
	}

	public SqlException(String message) {
		super(message);
	}

	public SqlException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlException(Throwable cause) {
		super(cause.getMessage());
	}
	public SqlException(Throwable cause,String sql) {
		super(cause.getMessage()+(isSqlLog?("\t"+sql):""));
	}
}
