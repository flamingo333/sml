package org.hw.sml.core.resolver.exception;

public class ResolverException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1258801462246858868L;
	
	public ResolverException(String msg){
		super(msg);
	}

	public ResolverException(Throwable cause) {
		super(cause);
	}

	public ResolverException() {
		super();
	}

	public ResolverException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
