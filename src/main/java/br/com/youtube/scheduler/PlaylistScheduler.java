package br.com.youtube.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import br.com.youtube.entities.SchedulerLog;
import br.com.youtube.services.SchedulerLogService;
import br.com.youtube.utils.YouTubeUtil;
import io.quarkus.runtime.util.ExceptionUtil;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;

/**
 * Scheduler to perform playlist tasks.
 *
 * @author Kevin Pinho Iamin
 */
@ApplicationScoped
public class PlaylistScheduler {

    @Inject
    SchedulerLogService schedulerLogService;

    /**
     * Retrieves json files from the playlist folder to perform multiple tasks.
     */
    @Scheduled(every = "1h", concurrentExecution = ConcurrentExecution.SKIP)
    void run() {
        Long schedulerLogId = this.schedulerLogService.logStart(SchedulerLog.Type.PLAYLIST);

        try {

            // Get folder from resources
            File folder = new File(PlaylistScheduler.class.getClassLoader().getResource("/youtube/playlist").toURI());

            // Iterate through the file list
            for (File file : folder.listFiles()) {

                // Convert the file to a JSON String
                String json = IOUtils.toString(new FileInputStream(file), "UTF-8");
                System.out.println(json);

                // Call playlist creation method
                YouTubeUtil.createPlaylist();
            }

            this.schedulerLogService.logEnd(schedulerLogId, SchedulerLog.Status.SUCCESS);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.schedulerLogService.logEnd(schedulerLogId,
                    SchedulerLog.Status.ERROR,
                    ExceptionUtils.getStackTrace(ex));
        }
    }

}
