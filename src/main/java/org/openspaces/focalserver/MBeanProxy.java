/*******************************************************************************
 * Copyright (c) 2006-2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.focalserver;

import java.io.IOException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;

public class MBeanProxy
        implements DynamicMBean, NotificationEmitter, MBeanRegistration
{
    private final ObjectName remoteObjectName;
    private final JMXConnector connector;
    private final MBeanServerConnection connection;

/*
    public MBeanProxy(
            ObjectName remoteObjectName, JMXServiceURL url, Map environment,
            Subject delegate
    ) throws IOException {
        this(
                remoteObjectName, JMXConnectorFactory.newJMXConnector(
                url, environment
        ), environment, delegate
        );
    }
*/

/*
    public MBeanProxy(
            ObjectName remoteObjectName, JMXConnector connector,
            Map environment, Subject delegate
    ) throws IOException {
        this.remoteObjectName = remoteObjectName;
        this.connector = connector;
        this.connector.connect(environment);
        this.connection = connector.getMBeanServerConnection(delegate);
    }
*/

    public MBeanProxy(
            ObjectName remoteObjectName, MBeanServerConnection connection
    ) {
        this.remoteObjectName = remoteObjectName;
        this.connector = null;
        this.connection = connection;
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
        JMXConnector cntor = getJMXConnector();
        if (cntor != null) cntor.close();
    }

    public void postDeregister() {
    }

    protected ObjectName getRemoteObjectName() {
        return remoteObjectName;
    }

    protected MBeanServerConnection getMBeanServerConnection() {
        return connection;
    }

    protected JMXConnector getJMXConnector() {
        return connector;
    }

    public MBeanInfo getMBeanInfo() {
        try {
            return getMBeanServerConnection().getMBeanInfo(
                    getRemoteObjectName()
            );
        }
        catch (Exception x) {
            throw new MBeanProxyException(x);
        }
    }

    public Object getAttribute(String attribute) throws
            AttributeNotFoundException, MBeanException, ReflectionException {
        try {
            return getMBeanServerConnection().getAttribute(
                    getRemoteObjectName(), attribute
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }

    public void setAttribute(Attribute attribute) throws
            AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        try {
            getMBeanServerConnection().setAttribute(
                    getRemoteObjectName(), attribute
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }

    public AttributeList getAttributes(String[] attributes) {
        try {
            return getMBeanServerConnection().getAttributes(
                    getRemoteObjectName(), attributes
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (ReflectionException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }

    public AttributeList setAttributes(AttributeList attributes) {
        try {
            return getMBeanServerConnection().setAttributes(
                    getRemoteObjectName(), attributes
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (ReflectionException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }

    public Object invoke(String method, Object[] arguments, String[] params)
            throws MBeanException, ReflectionException {
        try {
            return getMBeanServerConnection().invoke(
                    getRemoteObjectName(), method, arguments, params
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return getMBeanInfo().getNotifications();
    }

    public void addNotificationListener(
            NotificationListener listener, NotificationFilter filter,
            Object handback
    ) throws IllegalArgumentException {
        try {
            getMBeanServerConnection().addNotificationListener(
                    getRemoteObjectName(), listener, filter, handback
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }

    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException {
        try {
            getMBeanServerConnection().removeNotificationListener(
                    getRemoteObjectName(), listener
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }

    public void removeNotificationListener(
            NotificationListener listener, NotificationFilter filter,
            Object handback
    ) throws ListenerNotFoundException {
        try {
            getMBeanServerConnection().removeNotificationListener(
                    getRemoteObjectName(), listener, filter, handback
            );
        }
        catch (InstanceNotFoundException x) {
            throw new MBeanProxyException(x);
        }
        catch (IOException x) {
            throw new MBeanProxyException(x);
        }
    }
}
