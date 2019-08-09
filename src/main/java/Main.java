import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final String APPLICATION_NAME = "Enderstudy Youtube Sync";
    private static final File DATA_STORE_DIR = new File(
            System.getProperty("user.home"), ".credentials/enderstudy-youtube-sync");

    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    private static final List<String> SCOPES =
            Arrays.asList(YouTubeScopes.YOUTUBE_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static Credential authorise() throws IOException {
        InputStream in = Main.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");

        return credential;
    }

    public static YouTube getYouTubeService() throws IOException {
        Credential credential = authorise();
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        List<Playlist> playlists = getPlaylists();
        for(Playlist playlist: playlists) {
            System.out.printf("Getting items for playlist ID %s", playlist.getId());
            getPlaylistItems(playlist);
        }
    }

    private static List<Playlist> getPlaylists() {
        YouTube youtube = null;

        try {
            youtube = getYouTubeService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Playlist> playlists = null;

        try {
            YouTube.Playlists.List playlistList = youtube.playlists().list("id,contentDetails");
            playlistList.setChannelId("UCWa54B9bkK71u4LSsqiy04g");

            PlaylistListResponse response = playlistList.execute();
            playlists = response.getItems();
        } catch (GoogleJsonResponseException ex) {
            ex.printStackTrace();
            System.err.println("Youtube API Service error: " + ex.getDetails().getCode());
            System.err.println(ex.getDetails().getMessage());
        } catch (Throwable t) {
            System.err.println("Something bad happened");
            t.printStackTrace();
        }

        return playlists;
    }

    private static List<PlaylistItem> getPlaylistItems(Playlist playlist) {
        YouTube youtube = null;

        try {
            youtube = getYouTubeService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<PlaylistItem> playlistItems = null;

        try {
            YouTube.PlaylistItems.List playlistItemsRequest = youtube.playlistItems().list("id,contentDetails");
            playlistItemsRequest.setPlaylistId(playlist.getId());

            PlaylistItemListResponse response = playlistItemsRequest.execute();
            playlistItems = response.getItems();

            System.out.println(response.toPrettyString());
        } catch (GoogleJsonResponseException ex) {
            ex.printStackTrace();
            System.err.println("Youtube API Service error: " + ex.getDetails().getCode());
            System.err.println(ex.getDetails().getMessage());
        } catch (Throwable t) {
            System.err.println("Something bad happened");
            t.printStackTrace();
        }

        return playlistItems;
    }
}
