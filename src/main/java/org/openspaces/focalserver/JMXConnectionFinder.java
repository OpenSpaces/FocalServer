/*
 * Copyright GigaSpaces Technologies Inc. 2006
 */

package org.openspaces.focalserver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.rmi.RMISecurityManager;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.j_spaces.core.service.ServiceConfigLoader;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;

/**
 * An MBean that uses a Jini ServiceDiscoveryManager to locate
 * MBeans for management by an MBeanServer.
 */
public class JMXConnectionFinder
        implements JMXConnectionFinderMBean, ServiceDiscoveryListener,
        DiscoveryListener, MBeanRegistration {
// ------------------------------ FIELDS ------------------------------

    public static final String ALL_GROUPS = "ALL_GROUPS";
    /**
     * reserved jmx key for the original domain of the remote mbean
     * mbeans must not use this key to avoid conflict
     */
    public static final String REMOTE_DOMAIN = "remoteDomain";

    final public static String[] DEFAULT_GROUPS = LookupDiscovery.ALL_GROUPS;
    final public static LookupLocator[] DEFAULT_LOCATORS = new LookupLocator[0];
    private static final Logger LOGGER = Logger.getLogger(
            JMXConnectionFinder.class.getName()
    );

    /**
     * controls Jini group and locators
     */
    protected LookupDiscoveryManager ldm;
    private ServiceDiscoveryManager sdm;
    private LookupCache cache;
    /**
     * reference to local mbeanserver that is currently registered to
     */
    private MBeanServer mbeanServer;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Instaniate the MBean. Create and set a security manager
     * so that the ServiceDiscoveryManager will work.
     * Get a reference to the MBeanServer running in this VM
     */
    public JMXConnectionFinder() throws Exception {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        ldm = new LookupDiscoveryManager(
                DEFAULT_GROUPS, DEFAULT_LOCATORS, this, ServiceConfigLoader.getConfiguration()
        );
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface DiscoveryListener ---------------------

    /**
     * not really used, just shows that a LookupService
     * was found
     */
    public void discovered(DiscoveryEvent event) {
        LOGGER.log(Level.INFO, event.toString());
    }

    /**
     * Not really used, just shows that a LookupService
     * nas gone away
     */
    public void discarded(DiscoveryEvent event) {
        LOGGER.log(Level.INFO, event.toString());
    }

// --------------------- Interface LookupDisoveryManagerMBean ---------------------

    public String[] getGroups() {
        return ldm.getGroups();
    }

    public void addGroup(String group) throws IOException {
        LOGGER.log(Level.INFO, group);
        if (ldm.getGroups() == null) {
            ldm.setGroups(new String[]{group});
        } else {
            ldm.addGroups(new String[]{group});
        }
    }

    public void setGroups(String[] groups) throws IOException {
        ldm.setGroups(groups);
    }

    public void removeGroup(String group) {
        LOGGER.log(Level.INFO, group);
        ldm.removeGroups(new String[]{group});
    }

    public void setAllGroups() throws IOException {
        LOGGER.log(Level.INFO, ALL_GROUPS);
        ldm.setGroups(LookupDiscovery.ALL_GROUPS);
    }

    public String[] getLocators() {
        LookupLocator[] lookupLocators = ldm.getLocators();
        String[] urls = new String[lookupLocators.length];
        for (int i = 0; i < lookupLocators.length; i++) {
            LookupLocator lookupLocator = lookupLocators[i];
            String url = lookupLocator.toString();
            urls[i] = url;
        }
        return urls;
    }

    public void addLocator(String url) throws MalformedURLException {
        LOGGER.log(Level.INFO, url);
        LookupLocator locator = new LookupLocator(url);
        ldm.addLocators(new LookupLocator[]{locator});
    }

    public void removeLocator(String url)
            throws MalformedURLException {
        LOGGER.log(Level.INFO, url);
        LookupLocator locator = new LookupLocator(url);
        ldm.removeLocators(new LookupLocator[]{locator});
    }

    public void setLocators(String[] urls) throws MalformedURLException {
        LookupLocator[] locators = new LookupLocator[urls.length];
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i];
            LookupLocator locator = new LookupLocator(url);
            locators[i] = locator;
        }
        ldm.setLocators(locators);
    }

// --------------------- Interface MBeanRegistration ---------------------

    /**
     * receives mbeanserver reference
     */
    public ObjectName preRegister(
            MBeanServer mBeanServer, ObjectName objectName
    ) throws Exception {
        mbeanServer = mBeanServer;
        return objectName;
    }

    /**
     * start discovery is successfully registers to mbeanserver
     */
    public void postRegister(Boolean successful) {
        if (successful.booleanValue()) {
            try {
                start();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to start", e);
            }
        }
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

// --------------------- Interface ServiceDiscoveryListener ---------------------

    /**
     * Called by ServiceDiscoveryManager when a new services was discovered
     */
    public void serviceAdded(ServiceDiscoveryEvent event) {
        ServiceItem item = event.getPostEventServiceItem();
        String url = extractConnectionURL(item);
        if (url != null) {
            handleNewConnection(url);
        }
    }

    public void serviceRemoved(ServiceDiscoveryEvent event) {
        ServiceItem item = event.getPreEventServiceItem();
        String url = extractConnectionURL(item);
        if (url != null) {
            String domain_prefix = makeLegalName(url);
            unregisterAllProxyMBeans(domain_prefix);
        }
    }

    public void serviceChanged(ServiceDiscoveryEvent event) {
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Look for the jmxServiceURL attribute
     */
    String extractConnectionURL(ServiceItem serviceItem) {
        String url = null;
        for (int i = 0; i < serviceItem.attributeSets.length; i++) {
            Entry entry = serviceItem.attributeSets[i];
            try {
                Field field = entry.getClass().getField("jmxServiceURL");
                url = (String) field.get(entry);
                //don't have to look at any more entries
                break;
            } catch (NoSuchFieldException e1) {
            } catch (IllegalAccessException e11) {
            }
        }
        return url;
    }

    /**
     * Discovered new remote connections
     * Register local proxy mbeans for all remote mbeans
     * Listen for connections failure
     *
     * @param url used to connect to remote mbeanserver
     */
    private void handleNewConnection(String url) {
        //connect to remote mbeanServer
        LOGGER.log(Level.INFO, "Registering " + url);
        String domain_prefix = makeLegalName(url);
        try {
            JMXServiceURL jmxServiceURL = new JMXServiceURL(url);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(
                    jmxServiceURL, null
            );

            //listen for disconnect
            jmxConnector.addConnectionNotificationListener(
                new ConnectionNotificationHandler(domain_prefix), null, null
            );

            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            listenForRegistrationChanges(connection, domain_prefix);

            //register remote mbeans
            Iterator it = connection.queryNames(null, null).iterator();
            while (it.hasNext()) {
                ObjectName remoteName = (ObjectName) it.next();
                try {
                    registerProxyMBean(domain_prefix, remoteName, connection);
                } catch (InstanceAlreadyExistsException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                } catch (MBeanRegistrationException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                } catch (NotCompliantMBeanException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                }
            }
        } catch (MalformedObjectNameException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        } catch (InstanceNotFoundException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    /**
     * Listens to remote mbeanserver ur/registration
     */
    private void listenForRegistrationChanges(
            MBeanServerConnection connection, String domain_prefix
    )
            throws MalformedObjectNameException, InstanceNotFoundException,
            IOException {
        //listen for remove events
        ObjectName delegateName = ObjectName.getInstance(
                "JMImplementation:type=MBeanServerDelegate"
        );
        connection.addNotificationListener(
                delegateName, new RemoteRegistrationHandler(
                domain_prefix, connection
        ), null, domain_prefix
        );
    }

    /**
     * Register a local proxy mbean for a remote mbean
     */
    private void registerProxyMBean(
            String domain_prefix, ObjectName remoteName,
            MBeanServerConnection connection
    ) throws MalformedObjectNameException, MBeanRegistrationException,
            NotCompliantMBeanException, InstanceAlreadyExistsException
    {
        ObjectName localName = convertObjectName(domain_prefix, remoteName);
        MBeanProxy mBeanProxy = new MBeanProxy(remoteName, connection);
        try {
            mbeanServer.registerMBean(mBeanProxy, localName);
        } catch (InstanceAlreadyExistsException e) {
            //if already exist, first unregister it then retry
            LOGGER.log(Level.FINE, e.toString(), e);
            try {
                mbeanServer.unregisterMBean(localName);
            } catch (InstanceNotFoundException e1) {
                LOGGER.log(Level.WARNING, e1.toString(), e1);
            }
            mbeanServer.registerMBean(mBeanProxy, localName);
        }
    }

    /**
     * Converts remote mbean ObjectName to a local name
     * by using the host:ip as new domain
     * and putting orginal domain as remoteDomain value
     */
    private ObjectName convertObjectName(
            String domain_prefix, ObjectName oldName
    ) throws MalformedObjectNameException {
        String name = domain_prefix + ":" + REMOTE_DOMAIN + "=" + oldName.getDomain() + ","
            + oldName.getKeyPropertyListString();

        return ObjectName.getInstance(name);
    }

    /**
     * replace jmx reserved chars to _
     *
     * @param url
     * @return
     */
    private String makeLegalName(String url) {
        return url.replace(':', '_').replace('/', '_').replace('.', '_');
    }

    /**
     * Create the ServiceDiscoveryManager and LookupCache to begin
     * looking for jini services within Multicast range.
     */
    private void start() throws Exception {
        try {
            sdm = new ServiceDiscoveryManager(ldm, null, ServiceConfigLoader.getConfiguration());
            cache = sdm.createLookupCache(null, null, this);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    /**
     * Remote connection lost
     * Unregistering all proxy mbeans
     */
    private void unregisterAllProxyMBeans(String domain_prefix) {
        LOGGER.log(Level.INFO, "Unregistering " + domain_prefix);
        try {
            ObjectName query = ObjectName.getInstance(domain_prefix + "*:*");
            Set result = mbeanServer.queryNames(query, null);
            for (Iterator iterator = result.iterator(); iterator.hasNext();) {
                ObjectName objectName = (ObjectName) iterator.next();
                try {
                    mbeanServer.unregisterMBean(objectName);
                } catch (InstanceNotFoundException e) {
                    LOGGER.log(Level.FINE, e.toString(), e);
                } catch (MBeanRegistrationException e) {
                    LOGGER.log(Level.FINE, e.toString(), e);
                }
            }
        } catch (MalformedObjectNameException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Handles remote mbeanserver registration and unregistration events
     * Register and unregister local proxy mbeans accordingly
     */
    private class RemoteRegistrationHandler implements NotificationListener {
        private String domain_prefix;
        private MBeanServerConnection connection;

        public RemoteRegistrationHandler(
                String domain_prefix,
                MBeanServerConnection connection
        ) {
            this.domain_prefix = domain_prefix;
            this.connection = connection;
        }

        public void handleNotification(
                Notification notification, Object handback
        ) {
            if (notification instanceof MBeanServerNotification) {
                MBeanServerNotification mBeanServerNotification
                        = (MBeanServerNotification) notification;
                ObjectName remoteName = mBeanServerNotification.getMBeanName();
                try {
                    if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION
                            .equals(mBeanServerNotification.getType())) {
                        ObjectName localName = convertObjectName(
                                domain_prefix, remoteName
                        );
                        mbeanServer.unregisterMBean(localName);
                    } else {
                        registerProxyMBean(
                                domain_prefix, remoteName, connection
                        );
                    }
                } catch (MalformedObjectNameException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                } catch (InstanceNotFoundException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                } catch (MBeanRegistrationException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                } catch (InstanceAlreadyExistsException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                } catch (NotCompliantMBeanException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                }
            }
        }
    }

    /**
     * Handle remote connection failure
     * Unregister all the proxy mbeans
     */
    private class ConnectionNotificationHandler
            implements NotificationListener {
        private String domain_prefix;
        private boolean failed;

        public ConnectionNotificationHandler(
                String domain_prefix
        ) {
            this.domain_prefix = domain_prefix;
        }

        public void handleNotification(
                Notification notification, Object object
        ) {
            if (failed) {
                LOGGER.log(Level.FINEST, "Already Failed.");
                return;
            }
            LOGGER.log(Level.INFO, notification.toString());
            failed = true;
            // Don't unreigister if there is a connection problem
            // let serviceRemoved handle it
            // unregisterAllProxyMBeans(domain_prefix);
        }
    }
}
