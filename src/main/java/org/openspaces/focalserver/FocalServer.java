/*
 * Copyright GigaSpaces Technologies Inc. 2006
 */

package org.openspaces.focalserver;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.security.Permission;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


/**
 *
 */
public class FocalServer implements NotificationListener, MBeanRegistration,
FocalServerMBean
{
	public final static Logger LOGGER = Logger.getLogger(FocalServer.class.getName());

	// ------------------------------ FIELDS ------------------------------
	protected MBeanServer mbeanServer;

	// --------------------------- CONSTRUCTORS ---------------------------

	public FocalServer() {
		installSecurityManager();
	}

	/**
	 * Install a Grant All security manager
	 */
	private static void installSecurityManager() {
		System.setSecurityManager(
				new RMISecurityManager() {
					public void checkPermission(Permission perm) {

					}

					public void checkPermission(
							Permission perm, Object context
							) {

					}
				}
				);
	}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface FocalServerMBean ---------------------

	public void shutdown() {
		Logger logger = Logger.getLogger(FocalServer.class.getName()); 
		logger.log(Level.INFO, "FocalServer Shutdown.");
		System.exit(0);
	}

	// --------------------- Interface MBeanRegistration ---------------------

	public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
		mbeanServer = mBeanServer;
		return objectName;
	}

	public void postRegister(Boolean success) {
		if (success.booleanValue()) {
			listenForRegistration();
		}
	}

	public void preDeregister() throws Exception {
	}

	public void postDeregister() {
	}

	// --------------------- Interface NotificationListener ---------------------

	/**
	 * Logs registration events from the local mbeanserver
	 */
	public void handleNotification(Notification notification, Object object) {
		if (notification instanceof MBeanServerNotification) {
			Logger logger = Logger.getLogger(FocalServer.class.getName());
			MBeanServerNotification mBeanServerNotification = (MBeanServerNotification) notification;

			if (mBeanServerNotification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
				ObjectName beanName = mBeanServerNotification.getMBeanName();
				logger.log(Level.FINE, "Registered:" + beanName);
			} else {
				logger.log(Level.FINE, "Unregistered:" + mBeanServerNotification.getMBeanName());
			}
		}
	}

	// -------------------------- OTHER METHODS --------------------------

	private void listenForRegistration() {
		Logger logger = Logger.getLogger(FocalServer.class.getName());
		try {
			ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
			mbeanServer.addNotificationListener(delegateName, this, null, null);
		}
		catch (InstanceNotFoundException e1) {
			logger.log(Level.WARNING, e1.toString(), e1);
		} catch (MalformedObjectNameException e1) {
			logger.log(Level.WARNING, e1.toString(), e1);
		}
	}

	// --------------------------- main() method ---------------------------

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			printUsage();
			System.exit(1);
		}
		LOGGER.info("\n==================================================\n" + 
				"GigaSpaces Focal Server starting using " + Arrays.asList(args) + " \n"+
				"Log created by <" + System.getProperty("user.name") + "> on " + new Date().toString()+"\n"+
				"==================================================");
		ApplicationContext applicationContext;
		try
		{
			applicationContext = new FileSystemXmlApplicationContext(args);
		}
		catch (Exception e)
		{
			LOGGER.fine("Failed starting GigaSpaces Focal Server using " + Arrays.asList(args) + ", will try to load from classpath. " + e.getMessage());
			applicationContext = new ClassPathXmlApplicationContext(args);
		}

	}

	private static void printUsage() {
		System.out.println("Usage:" + FocalServer.class.getName() + " [Spring XML files in a file or classpath syntax]");
	}
}
