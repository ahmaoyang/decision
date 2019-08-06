package com.ry.cbms.decision.server.config;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import com.ry.cbms.decision.server.model.JobModel;
import com.ry.cbms.decision.server.schedule.RefreshMt4Token;
import com.ry.cbms.decision.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class JobConfig {

	public static final String KEY = "applicationContextSchedulerContextKey";

	@Autowired
	private JobService jobService;
	@Autowired
	private TaskExecutor taskExecutor;

	@Bean
	public SchedulerFactoryBean quartzScheduler(DataSource dataSource) {
		SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();

		try {
			quartzScheduler.setQuartzProperties(
					PropertiesLoaderUtils.loadProperties(new ClassPathResource("quartz.properties")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		quartzScheduler.setDataSource(dataSource);
		quartzScheduler.setOverwriteExistingJobs(true);
		quartzScheduler.setApplicationContextSchedulerContextKey(KEY);
		quartzScheduler.setStartupDelay(10);

		return quartzScheduler;
	}


//	/**
//	 * 初始化一个定时删除日志的任务
//	 */
//	@PostConstruct
//	public void initDeleteLogsJob() {
//		taskExecutor.execute(() -> {
//			JobModel jobModel = new JobModel();
//			jobModel.setJobName("delete-logs-job");
//			jobModel.setCron("0 0 0 * * ?");
//			jobModel.setDescription("定时删除三个月前日志");
//			jobModel.setSpringBeanName("sysLogServiceImpl");
//			jobModel.setMethodName("deleteLogs");
//			jobModel.setIsSysJob(true);
//			jobModel.setStatus(1);
//
//			jobService.saveJob(jobModel);
//		});
//	}

//	@PostConstruct
//	public void loginMt4() {
//		taskExecutor.execute(() -> {
//			JobModel jobModel = new JobModel();
//			jobModel.setJobName("登陆MT4账户");
//			jobModel.setCron("0/5 0 0 * * ?");
//			jobModel.setDescription("服务启动开始MT4登陆账户");
//		    jobModel.setSpringBeanName("refreshMt4Token");
//			jobModel.setMethodName("refreshHeartBeat");
//			jobModel.setIsSysJob(true);
//			jobModel.setStatus(1);
//			jobService.saveJob(jobModel);
//		});
//	}
}
