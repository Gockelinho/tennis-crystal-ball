package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

import static org.strangeforest.tcb.stats.jobs.DataLoadCommand.*;

@Component
@Profile("jobs")
public class InProgressEventsJob {

	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(InProgressEventsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.reload-in-progress-events:0 0 * * * *}")
	public void reloadInProgressEvents() {
		if (dataLoad("ReloadInProgressEvents", "-ip", "-c 1") == 0)
			clearCaches();
	}

	private void clearCaches() {
		dataService.evictGlobal("InProgressEvents");
		int cacheCount = dataService.clearCaches("InProgressEventForecast");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
