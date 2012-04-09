/*
 * Copyright GigaSpaces Technologies Inc. 2006
 */

package org.openspaces.focalserver;

/**
 * MBean interface to manage the FocalServer itself
 */
public interface FocalServerMBean {

    /**
     * Shuts down FocalServer process
     */
    void shutdown();
}
