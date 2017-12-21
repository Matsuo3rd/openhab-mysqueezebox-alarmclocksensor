package org.openhab.binding.mysqueezebox;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator.
 */
public final class MySqueezeboxActivator implements BundleActivator
{
	private static Logger logger = LoggerFactory.getLogger(MySqueezeboxActivator.class);
	private static BundleContext context;

	/**
	 * Called whenever the OSGi framework starts our bundle
	 * 
	 * @param bc
	 *          the bundle's execution context within the framework
	 */
	@Override
	public void start(BundleContext bc) throws Exception
	{
		context = bc;
		logger.info("MySqueezebox binding has been started.");
	}

	/**
	 * Called whenever the OSGi framework stops our bundle
	 * 
	 * @param bc
	 *          the bundle's execution context within the framework
	 */
	@Override
	public void stop(BundleContext bc) throws Exception
	{
		context = null;
		logger.warn("MySqueezebox binding has been stopped.");
	}

	/**
	 * Returns the bundle context of this bundle
	 * 
	 * @return the bundle context
	 */
	public static BundleContext getContext()
	{
		return context;
	}

	/**
	 * Returns the current version of the bundle.
	 * 
	 * @return the current version of the bundle.
	 */
	public static Version getVersion()
	{
		return context.getBundle().getVersion();
	}
}