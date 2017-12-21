# OpenHAB MySqueezebox Alarm Clock Sensor Binding

This [OpenHAB](https://www.openhab.org/) binding creates a sensor accessory based on Squeezebox alarm time.

## Features

This allows you to perform any automation workflow you want when you alarm clock is about to ring. I personally use it to kick in my heating system (Nest Thermostat) so that my home is warm when I get up (it motivates me to get out of bed for catching up that 6:30am plane).

When an alarm is about to be triggered (e.g. 30 minutes before / configurable `window_minutes`), the sensor will be "switched on". The sensor will switch back to initial state once the alarm time has passed. 
In order to determine sensor status, your Squeezebox's alarms states (on/off and schedules) are polled on a frequency defined by the `poll_cron` parameter (e.g. every 5th minute from 5am through 10am).

NOTE 1: This binding is, by design, meant to be configured through the [MySqueezebox.com](http://mysqueezebox.com). I did not want to install a local [Logitech Media Server (LMS)](https://en.wikipedia.org/wiki/Logitech_Media_Server).

NOTE 2: Squeezebox product line is discontinued by the manufacturer (Logitech). However, I still love the product and have not yet found an alternative smart alarm clock that stands comparison (design, multiple alarms, standalone / no phone required).

## Prerequisites

1. OpenHAB installed and running (tested with OpenHAB 1.8)
2. Java runtime version 1.8 or more recent

## Installation

1. Download `dist/org.openhab.binding.mysqueezebox-1.8.0.jar`
2. Copy the jar to `$OPENHAB_HOME/addons`
3. Edit OpenHAB configuration file `$OPENHAB_HOME/configurations/openhab.cfg`. See Binding Configuration section.

## Binding Configuration

This binding can be configured in the file `$OPENHAB_HOME/configurations/openhab.cfg`

|  Property  | Default | Required | Description |
|------------|---------|:--------:|-------------|
| email      |         | Yes      | Your mysqueezebox.com email account |
| password   |         | Yes      | Your mysqueezebox.com password account. Note that MySqueezebox website sends your email and password in clear text. Don't use any password you care about. |
| refreshCron|         | No if `refresh` defined | The [Quartz Cron expression](https://www.freeformatter.com/cron-expression-generator-quartz.html) defining the frequency for polling alarms status. e.g. "0 */5 4-9 ? * *" will poll alarms at every 5th minute from 5am through 10am. |
| refresh    |         | No if `refreshCon` defined | The frequency is milliseconds for polling alarm startus. e.g. "60000" will poll alarms every minute. |

Configuration sample:

```
############################ MySqueezebox Binding ############################
mysqueezebox:email=test@example.com
mysqueezebox:password=squeezeboxpassword
mysqueezebox:refreshCron=0 */5 4-9 ? * *
```

## Item Configuration

In order to bind to the MySqueezebox system you can add items to an item file using the following format:

```
mysqueezebox="PlayerId:WindowMinutes"

```

| Property | Description |
|---------------|-------------|
| PlayerId 		 | The MAC address of your Squeezebox. You can find it from [MySqueezebox.com](http://mysqueezebox.com) under Player section: Player MAC Address.|
| WindowMinutes | When polling alarms, if an alarm time is defined within this many minutes, the sensor will be triggered. |

.items sample:

```
Contact		alarmclock_sensor			"Alarm Clock Sensor"			{mysqueezebox="00:04:20:2a:f1:50,30"}
```

.rules sample

```
rule "Alarm Clock Sensor OPEN"
when
    Item alarmclock_sensor changed to OPEN
then
    logInfo("AlarmClockSensor", "Alarm Clock OPEN !")
end

rule "Alarm Clock Sensor CLOSED"
when
    Item alarmclock_sensor changed to CLOSED
then
    logInfo("AlarmClockSensor", "Alarm Clock CLOSED !")
end
```
