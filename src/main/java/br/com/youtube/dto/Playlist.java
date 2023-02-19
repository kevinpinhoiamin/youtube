package br.com.youtube.dto;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Playlist {

    private String playlistId;
    private List<PlaylistChannel> channels;

    public String getChannelIdsAsString() {
        return channels.stream().map(c -> c.getId()).collect(Collectors.joining(","));
    }

    @Data
    public class PlaylistChannel {
        private String url;
        private String id;
    }

}
