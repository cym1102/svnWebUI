package com.cym.task;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling // 2.开启定时任务
public class MonitorTask {


	@Scheduled(cron = "0 * * * * ?")
	public void run() {
	
		
	}


}