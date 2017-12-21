package org.openhab.binding.mysqueezebox;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openhab.binding.mysqueezebox.internal.MySqueezeboxClient;
import org.openhab.binding.mysqueezebox.internal.MySqueezeboxUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqueezeboxActiveBinding extends
		AbstractActiveBinding<MySqueezeboxBindingProvider> implements ManagedService
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MySqueezeboxActiveBinding.class);
	private HashMap<String, String> m_config = null;
	private long m_refreshInterval = 60000L;
	private Scheduler scheduler = null;

	/**
	 * Inherited from AbstractBinding. Activates the binding. There is nothing we
	 * can do at this point if we don't have the configuration information, which
	 * usually comes in later, when updated() is called. {@inheritDoc}
	 */
	@Override
	public void activate()
	{
		// MySqueezeboxActiveBinding.LOGGER.info("Activating binding MySqueezebox");
		this.initialize();
	}

	/**
	 * Inherited from AbstractBinding. Deactivates the binding. The Controller is
	 * stopped and the serial interface is closed as well. {@inheritDoc}
	 */
	@Override
	public void deactivate()
	{
		// MySqueezeboxActiveBinding.LOGGER.info("Deactivating binding
		// MySqueezebox");
		this.shutdown();
	}

	/**
	 * Inherited from AbstractActiveBinding. {@inheritDoc}
	 */
	@Override
	protected String getName()
	{
		return "MySqueezebox";
	}

	/**
	 * Inherited from AbstractActiveBinding. Periodically called by the framework
	 * to execute a refresh of the binding. {@inheritDoc}
	 */
	@Override
	protected void execute()
	{
		MySqueezeboxGenericBindingProvider p = null;
		try
		{
			p = (MySqueezeboxGenericBindingProvider) this.providers.iterator().next();
		}
		catch (NoSuchElementException e)
		{
			return;
		}
		// Only call status update once per Player Id.
		Map<String, List<MySqueezeboxBindingConfig>> configByPlayerId = new HashMap<String, List<MySqueezeboxBindingConfig>>();
		for (MySqueezeboxBindingConfig c : p.getMySqueezeboxBindingConfigs())
		{
			List<MySqueezeboxBindingConfig> configs = configByPlayerId
					.get(c.getPlayerId());
			if (configs == null)
			{
				configs = new ArrayList<MySqueezeboxBindingConfig>();
				configByPlayerId.put(c.getPlayerId(), configs);
			}
			configs.add(c);
		}

		for (String playerId : configByPlayerId.keySet())
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("playerId", playerId);
			MySqueezeboxClient client = null;
			boolean isAlarmInTimeWindow = false;
			Integer timeWindowMinutes = configByPlayerId.get(playerId).get(0)
					.getWindowMinutes();
			try
			{
				MySqueezeboxActiveBinding.LOGGER
						.debug("Polling MySqueezebox alarms " + playerId);
				client = new MySqueezeboxClient(this.m_config.get("email"),
						this.m_config.get("password"), playerId);
				isAlarmInTimeWindow = MySqueezeboxUtils.pollAlarms(client,
						timeWindowMinutes);
			}
			catch (Throwable t)
			{
				MySqueezeboxActiveBinding.LOGGER
						.error("Could not poll MySqueezebox alarms", t);
				throw new RuntimeException(t);
			}
			for (MySqueezeboxBindingConfig c : configByPlayerId.get(playerId))
			{
				if (isAlarmInTimeWindow)
				{
					this.publishState(c.getName(), OpenClosedType.OPEN);
				}
				else
				{
					this.publishState(c.getName(), OpenClosedType.CLOSED);
				}
			}
		}
	}

	private void publishState(String name, State state)
	{
		this.eventPublisher.postUpdate(name, state);
	}

	/**
	 * Inherited from AbstractActiveBinding. Returns the refresh interval (time
	 * between calls to execute()) in milliseconds. {@inheritDoc}
	 */
	@Override
	protected long getRefreshInterval()
	{
		return this.m_refreshInterval;
	}

	/**
	 * Inherited from AbstractActiveBinding. This method is called by the
	 * framework whenever there are changes to a binding configuration.
	 *
	 * @param provider
	 *          the binding provider where the binding has changed
	 * @param itemName
	 *          the item name for which the binding has changed
	 */
	@Override
	public void bindingChanged(BindingProvider provider, String itemName)
	{
		super.bindingChanged(provider, itemName);
		this.initialize();
	}

	/**
	 * Initialize the binding: initialize the driver etc
	 */
	private void initialize()
	{
		MySqueezeboxActiveBinding.LOGGER.debug("initializing...");

		MySqueezeboxGenericBindingProvider p = (MySqueezeboxGenericBindingProvider) providers
				.iterator().next();
		// Create configs per Player ID
		Map<String, Set<MySqueezeboxBindingConfig>> configByPlayerID = new HashMap<String, Set<MySqueezeboxBindingConfig>>();
		for (MySqueezeboxBindingConfig c : p.getMySqueezeboxBindingConfigs())
		{
			this.publishState(c.getName(), OpenClosedType.CLOSED);
			Set<MySqueezeboxBindingConfig> configs = configByPlayerID
					.get(c.getPlayerId());
			if (configs == null)
			{
				configs = new HashSet<MySqueezeboxBindingConfig>();
				configByPlayerID.put(c.getPlayerId(), configs);
			}
			configs.add(c);
		}
	}

	/**
	 * Clean up all state.
	 */
	private void shutdown()
	{
		MySqueezeboxActiveBinding.LOGGER.debug("shutting down binding");
		if (this.scheduler != null)
		{
			try
			{
				this.scheduler.shutdown();
			}
			catch (Throwable t)
			{
				MySqueezeboxActiveBinding.LOGGER
						.error("Could not shutdown Cron Scheduler", t);
			}
		}
	}

	@Override
	public void updated(@SuppressWarnings("rawtypes") Dictionary properties)
			throws ConfigurationException
	{
		HashMap<String, String> newConfig = new HashMap<String, String>();
		if (this.m_config == null)
		{
			MySqueezeboxActiveBinding.LOGGER.debug(
					"seems like our configuration has been erased, will reset everything!");
			if (properties != null)
			{
				for (@SuppressWarnings("unchecked")
				Enumeration<String> e = properties.keys(); e.hasMoreElements();)
				{
					String key = e.nextElement();
					String value = properties.get(key).toString();
					newConfig.put(key, value);
				}
			}
			else
			{
				MySqueezeboxActiveBinding.LOGGER.warn(
						"MySqueezebox properties was null, are you sure your config file is correct?");
			}

			this.m_config = newConfig;
			if (this.m_config.containsKey("refreshCron"))
			{
				try
				{
					JobDataMap context = new JobDataMap();
					context.put("activeBinding", this);
					String refreshCron = this.m_config.get("refreshCron");
					this.scheduler = StdSchedulerFactory.getDefaultScheduler();
					JobDetail job = JobBuilder.newJob(MySqueezeboxAlarmWatchdog.class)
							.withIdentity("mysqueezeboxJob", "mysqueezeboxGroup").build();
					CronTrigger trigger = TriggerBuilder.newTrigger()
							.withIdentity("mysqueezeboxTrigger", "mysqueezeboxGroup")
							.usingJobData(context)
							.withSchedule(CronScheduleBuilder.cronSchedule(refreshCron))
							.build();
					this.scheduler.scheduleJob(job, trigger);
					this.scheduler.start();
					// this.setProperlyConfigured(true);
					MySqueezeboxActiveBinding.LOGGER
							.info("MySqueezebox Cron Scheduler launched");
				}
				catch (Throwable t)
				{
					MySqueezeboxActiveBinding.LOGGER
							.error("Could not start Cron Scheduler", t);
				}
			}
			else if (this.m_config.containsKey("refresh"))
			{
				try
				{
					this.m_refreshInterval = Long.parseLong(this.m_config.get("refresh"));
					this.setProperlyConfigured(true);
					MySqueezeboxActiveBinding.LOGGER
							.info("MySqueezebox Fix Refresh Scheduler launched");
				}
				catch (NumberFormatException e)
				{
					MySqueezeboxActiveBinding.LOGGER.error(
							"Are you sure you provided a numerical value for the openhab.cfg option plum:refresh?");
				}
			}
			else
			{
				MySqueezeboxActiveBinding.LOGGER.error(
						"MySqueezebox binding not properly configured: define either 'refresh' or 'refreshCron' in openhab.cfg");
			}
		}
	}
}
