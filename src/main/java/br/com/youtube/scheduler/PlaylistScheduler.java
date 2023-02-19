package br.com.youtube.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.gson.Gson;

import br.com.youtube.dto.Playlist;
import br.com.youtube.entities.SchedulerLog;
import br.com.youtube.services.SchedulerLogService;
import br.com.youtube.utils.YouTubeUtil;
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
        SchedulerLog lastSchedulerLog = this.schedulerLogService.findLastByType(SchedulerLog.Type.PLAYLIST);
        SchedulerLog currentSchedulerLog = this.schedulerLogService.logStart(SchedulerLog.Type.PLAYLIST);

        try {

            // Get folder from resources
            File folder = new File(PlaylistScheduler.class.getClassLoader().getResource("/youtube/playlist").toURI());

            // Get GSON
            Gson gson = new Gson();

            // Get published after date
            Date publishedAfter = calculatePublishedAfterDate(lastSchedulerLog, currentSchedulerLog);

            System.out.println("Running PlaylistScheduler: " + publishedAfter.toString());

            // Iterate through the file list
            for (File file : folder.listFiles()) {
                if (!file.getPath().endsWith(".json")) {
                    continue;
                }

                // Convert the file to a JSON String
                String json = IOUtils.toString(new FileInputStream(file), "UTF-8");

                // Conver JSON String to object
                Playlist playlist = gson.fromJson(json, Playlist.class);

                // Get channel list
                List<Channel> channels = YouTubeUtil.getChannelsByIdList(playlist.getChannelIdsAsString());
                if (channels.size() > 0) {

                    for (Channel channel : channels) {
                        String playlistId = channel.getContentDetails().getRelatedPlaylists().getUploads();
                        List<PlaylistItem> playlistItems = YouTubeUtil.getPlaylistItems(playlistId, publishedAfter);

                        for (PlaylistItem playlistItem : playlistItems) {
                            YouTubeUtil.insertPlaylistItem(
                                    playlist.getPlaylistId(),
                                    playlistItem.getContentDetails().getVideoId());
                        }
                    }
                }
            }

            this.schedulerLogService.logEnd(
                    currentSchedulerLog.getId(),
                    SchedulerLog.Status.SUCCESS);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.schedulerLogService.logEnd(
                    currentSchedulerLog.getId(),
                    SchedulerLog.Status.ERROR,
                    ExceptionUtils.getStackTrace(ex));
        }
    }

    private Date calculatePublishedAfterDate(SchedulerLog lastSchedulerLog, SchedulerLog currentSchedulerLog) {
        Date date = null;

        if (lastSchedulerLog == null) {
            Date now = currentSchedulerLog.getDateCreated();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR, -1);
            date = calendar.getTime();
        } else {
            date = lastSchedulerLog.getDateCreated();
        }

        return date;
    }

}
