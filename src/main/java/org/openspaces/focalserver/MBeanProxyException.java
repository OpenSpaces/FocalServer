/*
 * Copyright GigaSpaces Technologies Inc. 2006
 */

package org.openspaces.focalserver;

import javax.management.JMRuntimeException;

public class MBeanProxyException extends JMRuntimeException
{
	private static final long serialVersionUID = -2814818930830294589L;
	
	private final Exception exception;

    public MBeanProxyException() {
        this(null, null);
    }

    public MBeanProxyException(String message) {
        this(message, null);
    }

    public MBeanProxyException(Exception exception) {
        this(null, exception);
    }

    public MBeanProxyException(String message, Exception exception) {
        super(message);
        this.exception = exception;
    }

    @Override
	public Throwable getCause() {
        return exception;
    }
}
