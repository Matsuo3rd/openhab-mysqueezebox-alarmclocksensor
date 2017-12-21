package org.openhab.binding.mysqueezebox;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MySqueezeboxAlarmWatchdog implements Job
{
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		((MySqueezeboxActiveBinding) context.getMergedJobDataMap()
				.get("activeBinding")).execute();
	}
}
