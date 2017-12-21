package org.openhab.binding.mysqueezebox.internal;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MySqueezeboxUtils
{
	public static boolean pollAlarms(MySqueezeboxClient client,
			Integer timeWindowMinutes) throws Exception
	{
		try
		{
			client.login();
			JSONObject response = client
					.command(new String[] { "playerpref", "alarmsEnabled", "?" }, 3);
			if (Integer
					.parseInt(response.getJSONObject("result").getString("_p2")) == 1)
			{
				response = client.command(new String[] { "alarms", "" }, 3);
				if (response.getJSONObject("result").getInt("count") > 0)
				{
					Calendar today = Calendar.getInstance();
					today.set(Calendar.HOUR_OF_DAY, 0);
					today.set(Calendar.MINUTE, 0);
					today.set(Calendar.SECOND, 0);
					today.set(Calendar.MILLISECOND, 0);
					Long todayS = (new Date().getTime() - today.getTime().getTime())
							/ 1000;

					JSONArray alarms = response.getJSONObject("result")
							.getJSONArray("alarms_loop");
					for (int i = 0; i < alarms.length(); i++)
					{
						if (Integer
								.parseInt(alarms.getJSONObject(i).getString("enabled")) == 1)
						{
							String[] dow = alarms.getJSONObject(i).getString("dow")
									.split(",");
							if (ArrayUtils.contains(dow,
									Integer.toString(today.get(Calendar.DAY_OF_WEEK) - 1)))
							{
								if (alarms.getJSONObject(i).getLong("time") > todayS
										&& ((alarms.getJSONObject(i).getLong("time")
												- todayS) <= (timeWindowMinutes * 60)))
								{
									return true;
								}
							}
						}
					}
				}
			}
		}
		catch (Throwable t)
		{
			throw new Exception("Error pooling MySqueezebox alarms", t);
		}

		return false;
	}
}
