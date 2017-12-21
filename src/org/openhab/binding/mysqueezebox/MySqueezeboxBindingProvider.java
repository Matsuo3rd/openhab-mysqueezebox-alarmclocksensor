package org.openhab.binding.mysqueezebox;

import java.util.Collection;

import org.openhab.core.binding.BindingProvider;

/**
 * Binding provider interface. Defines the methods to interact with the binding
 * provider.
 *
 * @author Bertrand Martel
 */
public interface MySqueezeboxBindingProvider extends BindingProvider
{
	/**
   * Returns the binding configuration for the item with this name.
   *
   * @param itemName
   *            the name to get the binding configuration for.
   * @return the binding configuration.
   */
	public MySqueezeboxBindingConfig getMySqueezeboxBindingConfig(
			String itemName);

	public Collection<MySqueezeboxBindingConfig> getMySqueezeboxBindingConfigs();
}