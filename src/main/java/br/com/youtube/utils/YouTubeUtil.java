package br.com.youtube.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.common.collect.Lists;

/**
 * YouTube util to perform differents tasks.
 *
 * @author Kevin Pinho Iamin
 */
public class YouTubeUtil {

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new GsonFactory();

    /** Global instance of YouTube object to make all API requests. */
    private static YouTube youtube;

    /*
     * Global instance of the video id we want to post as the first PlaylistItem in
     * our new playlist.
     */
    private static String VIDEO_ID = "SZj6rAYkYOg";

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @param scopes list of scopes needed to run upload.
     */
    private static Credential authorize(List<String> scopes) throws Exception {

        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(
                        YouTubeUtil.class.getResourceAsStream("/youtube/credentials/client_secrets.json")));

        // Checks that the defaults have been replaced (Default = "Enter X here").
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential"
                            + "into youtube-cmdline-playlistupdates-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }

        // Set up file credential store.
        DataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home"),
                ".credentials/youtube-api-playlistupdates.json"));

        // Set up authorization code flow.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                scopes)
                .setDataStoreFactory(dataStoreFactory).build();

        // Build the local server and bind it to port 9000
        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8888).build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }

    /**
     * Authorizes user and list channels by the list of IDs.
     *
     * @param ids list of channel IDs.
     */
    public static List<Channel> getChannelsByIdList(String ids) {
        // General read/write scope for YouTube APIs.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.readonly");

        // Authorization.
        try {
            // Authorization.
            Credential credential = authorize(scopes);

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-playlistupdates-sample")
                    .build();

            // Define a list to store items in the list of channels.
            List<Channel> channelList = new ArrayList<Channel>();

            // Create request, filter by IDs and execute it
            YouTube.Channels.List channelListRequest = youtube.channels().list("contentDetails");
            channelListRequest.setId(ids);

            String nextToken = "";

            // Call the API one or more times to retrieve all items in the
            // list. As long as the API response returns a nextPageToken,
            // there are still more items to retrieve.
            do {
                channelListRequest.setPageToken(nextToken);
                ChannelListResponse channelItemResult = channelListRequest.execute();

                channelList.addAll(channelItemResult.getItems());

                nextToken = channelItemResult.getNextPageToken();
            } while (nextToken != null);

            // Return the items
            return channelList;
        } catch (GoogleJsonResponseException e) {
            System.err.println(
                    "There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }

        return null;
    }

    /**
     * Authorizes user and list channels by the list of IDs.
     *
     * @param playlistId playlist ID.
     */
    public static List<PlaylistItem> getPlaylistItems(String playlistId, Date publishedAfter) {
        // General read/write scope for YouTube APIs.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.readonly");

        // Authorization.
        try {
            // Authorization.
            Credential credential = authorize(scopes);

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-playlistupdates-sample")
                    .build();

            // Define a list to store items in the list of uploaded videos.
            List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

            // Create request, filter by ID and execute it
            YouTube.PlaylistItems.List playlistItemRequest = youtube.playlistItems().list("contentDetails");
            playlistItemRequest.setPlaylistId(playlistId);
            playlistItemRequest.setMaxResults(5L);

            String nextToken = "";

            // Call the API one or more times to retrieve all items in the
            // list. As long as the API response returns a nextPageToken,
            // there are still more items to retrieve.
            do {
                playlistItemRequest.setPageToken(nextToken);
                PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                // playlistItemList.addAll(playlistItemResult.getItems());

                List<PlaylistItem> currentPageItems = playlistItemResult.getItems();
                currentPageItems = currentPageItems.stream().filter(playlistItem -> {
                    Date publishedAt = new Date(playlistItem.getContentDetails().getVideoPublishedAt().getValue());
                    // long diffInMinutes = TimeUnit.MINUTES.convert(publishedAfter.getTime() -
                    // publishedAt.getTime(),
                    // TimeUnit.MILLISECONDS);
                    // return diffInMinutes <= 0;
                    return publishedAt.after(publishedAfter) || publishedAt.equals(publishedAfter);
                }).collect(Collectors.toList());

                // Add items to playlist
                playlistItemList.addAll(currentPageItems);

                // Check if is full page to make more requests
                if (currentPageItems.size() == 5) {
                    nextToken = playlistItemResult.getNextPageToken();
                } else {
                    nextToken = null;
                }

            } while (nextToken != null);

            return playlistItemList;
        } catch (GoogleJsonResponseException e) {
            System.err.println(
                    "There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }

        return null;
    }

    /**
     * Authorizes user, creates a playlist, adds a playlistitem with a video to that
     * new playlist.
     *
     * @param args command line args (not used).
     */
    public static void createPlaylist() {
        // General read/write scope for YouTube APIs.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        // Authorization.
        try {
            // Authorization.
            Credential credential = authorize(scopes);

            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-playlistupdates-sample")
                    .build();

            // Creates a new playlist in the authorized user's channel.
            String playlistId = insertPlaylist();

            // If a valid playlist was created, adds a new playlistitem with a video to that
            // playlist.
            insertPlaylistItem(playlistId, VIDEO_ID);
        } catch (GoogleJsonResponseException e) {
            System.err.println(
                    "There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Creates YouTube Playlist and adds it to the authorized account.
     */
    private static String insertPlaylist() throws IOException {
        /*
         * We need to first create the parts of the Playlist before the playlist itself.
         * Here we are
         * creating the PlaylistSnippet and adding the required data.
         */
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle("Test Playlist " + Calendar.getInstance().getTime());
        playlistSnippet.setDescription("A private playlist created with the YouTube API v3");

        // Here we set the privacy status (required).
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("private");

        /*
         * Now that we have all the required objects, we can create the Playlist itself
         * and assign the
         * snippet and status objects from above.
         */
        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);

        /*
         * This is the object that will actually do the insert request and return the
         * result. The
         * first argument tells the API what to return when a successful insert has been
         * executed. In
         * this case, we want the snippet and contentDetails info. The second argument
         * is the playlist
         * we wish to insert.
         */
        YouTube.Playlists.Insert playlistInsertCommand = youtube.playlists().insert("snippet,status", youTubePlaylist);
        Playlist playlistInserted = playlistInsertCommand.execute();

        // Pretty print results.
        System.out.println("New Playlist name: " + playlistInserted.getSnippet().getTitle());
        System.out.println(" - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
        System.out.println(" - Description: " + playlistInserted.getSnippet().getDescription());
        System.out.println(" - Posted: " + playlistInserted.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");

        return playlistInserted.getId();
    }

    /**
     * Creates YouTube PlaylistItem with specified video id and adds it to the
     * specified playlist id
     * for the authorized account.
     *
     * @param playlistId assign to newly created playlistitem
     * @param videoId    YouTube video id to add to playlistitem
     */
    public static String insertPlaylistItem(String playlistId, String videoId) throws IOException {
        /*
         * The Resource type (video,playlist,channel) needs to be set along with the
         * resource id. In
         * this case, we are setting the resource to a video id, since that makes sense
         * for this
         * playlist.
         */
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        /*
         * Here we set all the information required for the snippet section. We also
         * assign the
         * resource id from above to the snippet object.
         */
        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle("First video in the test playlist");
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        /*
         * Now that we have all the required objects, we can create the PlaylistItem
         * itself and assign
         * the snippet object from above.
         */
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        /*
         * This is the object that will actually do the insert request and return the
         * result. The
         * first argument tells the API what to return when a successful insert has been
         * executed. In
         * this case, we want the snippet and contentDetails info. The second argument
         * is the
         * playlistitem we wish to insert.
         */
        YouTube.PlaylistItems.Insert playlistItemsInsertCommand = youtube.playlistItems()
                .insert("snippet,contentDetails", playlistItem);
        PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

        // Pretty print results.

        System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
        System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
        System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
        return returnedPlaylistItem.getId();

    }

}
