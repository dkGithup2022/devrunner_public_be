package dev.devrunner.application.batch.cron.sync;

import dev.devrunner.sync.task.task.es.CommunityPostEsSyncTask;
import dev.devrunner.sync.task.task.es.JobEsSyncTask;
import dev.devrunner.sync.task.task.es.TechBlogEsSyncTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.devrunner.application.batch.ScheduleUtils.executeBatchTask;

@RequiredArgsConstructor
@Component
@Slf4j
public class EsSyncTask {

    private final JobEsSyncTask jobEsSyncTask;
    private final TechBlogEsSyncTask techBlogEsSyncTask;
    private final CommunityPostEsSyncTask communityPostEsSyncTask;

    private static final AtomicBoolean JOB_ES_SYNC_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean TECHBLOG_ES_SYNC_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean COMMUNITYPOST_ES_SYNC_RUNNING = new AtomicBoolean(false);

    /**
     * Job ES 동기화: 3초마다 실행
     */

    @Scheduled(cron = "*/3 * * * * ?")
    //@Scheduled(cron = "*/10 * * * * ?")
    public void syncJob() {
        executeBatchTask(JOB_ES_SYNC_RUNNING, "es_sync_job", jobEsSyncTask::run);
    }

    /**
     * TechBlog ES 동기화: 3초마다 실행
     */
    @Scheduled(cron = "*/3 * * * * ?")
    //@Scheduled(cron = "*/10 * * * * ?")
    public void syncTechBlog() {
        executeBatchTask(TECHBLOG_ES_SYNC_RUNNING, "es_sync_techblog", techBlogEsSyncTask::run);
    }

    /**
     * CommunityPost ES 동기화: 3초마다 실행
     */
    @Scheduled(cron = "*/3 * * * * ?")
    //@Scheduled(cron = "*/10 * * * * ?")
    public void syncCommunityPost() {
        executeBatchTask(COMMUNITYPOST_ES_SYNC_RUNNING, "es_sync_communitypost", communityPostEsSyncTask::run);
    }
}
