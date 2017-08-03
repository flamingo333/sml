package org.hw.sml.support.el;

public class ElException extends Exception{
	public ElException(String string) {
		super(string);
	}
	public ElException(String message, Throwable cause) {
	        super(message, cause);
	}
	private static final long serialVersionUID = 4927053908415703226L;
}
