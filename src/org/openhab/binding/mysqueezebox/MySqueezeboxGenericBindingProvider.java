package org.openhab.binding.mysqueezebox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 *
 * @author Bertrand Martel
 */
public class MySqueezeboxGenericBindingProvider extends
		AbstractGenericBindingProvider implements MySqueezeboxBindingProvider
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MySqueezeboxGenericBindingProvider.class);
	private final Map<String, Item> items = new HashMap<String, Item>();

	/**
	 * Inherited from AbstractGenericBindingProvider. {@inheritDoc}
	 */
	@Override
	public String getBindingType()
	{
		return "mysqueezebox";
	}

	/**
	 * Inherited from AbstractGenericBindingProvider. {@inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException
	{
		String[] parts = this.parseConfigString(bindingConfig);
		if (parts.length != 2)
		{
			throw new BindingConfigParseException(
					"item config must have playerId,windowMinutes");
		}
	}

	/**
	 * Inherited from AbstractGenericBindingProvider. Processes MySqueezebox binding
	 * configuration string. {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException
	{
		super.processBindingConfiguration(context, item, bindingConfig);
		String[] parts = parseConfigString(bindingConfig);
		if (parts.length != 2)
		{
			throw new BindingConfigParseException(
					"item config must have playerId,windowMinutes");
		}
		
		MySqueezeboxBindingConfig config = new MySqueezeboxBindingConfig(
				item.getName(), parts[0], Integer.parseInt(parts[1]));
		this.addBindingConfig(item, config);

		MySqueezeboxGenericBindingProvider.LOGGER.info(
				"processing item \"{}\" read from .items file with cfg string {}",
				item.getName(), bindingConfig);
		items.put(item.getName(), item);
	}

	/**
	 * Inherited from MySqueezeboxBindingProvider. {@inheritDoc}
	 */
	@Override
	public MySqueezeboxBindingConfig getMySqueezeboxBindingConfig(String itemName)
	{
		return (MySqueezeboxBindingConfig) this.bindingConfigs.get(itemName);
	}

	/**
	 * Parses binding configuration string. The config string has the format:
	 *
	 * xx.xxx.xxx:playerId,windowMinutes
	 *
	 * @param bindingConfig
	 *          string with binding parameters
	 * @return String array with split arguments:
	 *         [address,prodKey,features+params]
	 * @throws BindingConfigParseException
	 *           if parameters are invalid
	 */
	private String[] parseConfigString(String bindingConfig)
			throws BindingConfigParseException
	{
		String shouldBe = "should be playerId:windowMinutes, e.g. 01:23:45:67:89:ab,30";
		String[] dev = bindingConfig.split(",");

		if (dev.length != 2)
		{
			throw new BindingConfigParseException(
					"missing colon in item format: " + bindingConfig + ", " + shouldBe);
		}
		String[] retval = { dev[0], dev[1]};

		return retval;
	}

	@Override
	public Collection<MySqueezeboxBindingConfig> getMySqueezeboxBindingConfigs()
	{
		List<MySqueezeboxBindingConfig> l = new ArrayList<MySqueezeboxBindingConfig>();
		for (BindingConfig c : this.bindingConfigs.values())
		{
			l.add((MySqueezeboxBindingConfig) c);
		}
		
		return l;
	}
}