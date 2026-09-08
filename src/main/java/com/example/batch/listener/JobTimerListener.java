package com.example.batch.listener;

import jakarta.batch.api.listener.AbstractJobListener;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.jbosslog.JBossLog;

@Named("jobTimerListener")
@Dependent
@JBossLog
public class JobTimerListener extends AbstractJobListener {

	private final JobContext jobContext;
	private long startTime;

    @Inject
    public JobTimerListener(JobContext jobContext) {
    	this.jobContext = jobContext;
    }

    @Override
    public void beforeJob() {
        startTime = System.currentTimeMillis();
        log.infof("Job starting: %s", jobContext.getJobName());
    }

    @Override
    public void afterJob() {
        long duration = System.currentTimeMillis() - startTime;
        log.infof("Job %s completed in %d ms (%.2f seconds)",
                jobContext.getJobName(), duration, duration / 1000.0);
    }
}
