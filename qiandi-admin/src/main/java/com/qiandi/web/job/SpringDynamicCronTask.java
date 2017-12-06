package com.qiandi.web.job;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.qiandi.service.SettingService;
import com.qiandi.web.util.AdminUtils;

/**
 * Spring动态周期定时任务<br>
 * 在不停应用的情况下更改任务执行周期
 * @Author 许亮
 * @Create 2016-11-10 16:31:29
 */
@Lazy(false)
@Component
@EnableScheduling
public class SpringDynamicCronTask implements SchedulingConfigurer {
	private static final Logger logger = LogManager.getLogger(SpringDynamicCronTask.class);
	@Autowired
	private SettingService settingService;
	
	public static String cron;
	
	public SpringDynamicCronTask() {
		cron = "0/59 * * * * ?";

		// 开启新线程模拟外部更改了任务执行周期
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(15 * 1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				Integer hour = Integer.parseInt(AdminUtils.getValue(settingService, "recived_fail_hour"));
				Integer minute = Integer.parseInt(AdminUtils.getValue(settingService, "recived_fail_minute"));
				SpringDynamicCronTask.cron = "0 " + minute + " " + hour + " ? * *";
				logger.debug("cron change to: " + cron);
			}
		}).start();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.addTriggerTask(new Runnable() {
			@Override
			public void run() {
				// 任务逻辑

				logger.debug("dynamicCronTask is running...");
			}
		}, new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {
				// 任务触发，可修改任务的执行周期
				CronTrigger trigger = new CronTrigger(cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
			}
		});
	}
}
