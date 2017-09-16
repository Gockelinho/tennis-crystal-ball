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
public class NewCompletedEventsJob {

	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(NewCompletedEventsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.load-new-completed-events:0 10 1 * * *}")
	public void loadNewCompletedEvents() {
		if (dataLoad("LoadNewCompletedEvents", "-nt", "-c 1") == 0)
			clearCaches();
	}

	private void clearCaches() {
		dataService.evictGlobal("Tournaments");
		int cacheCount = dataService.clearCaches("Tournament.*");
		LOGGER.info("{} cache(s) cleared.", cacheCount);
	}
}
