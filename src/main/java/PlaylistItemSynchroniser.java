import com.google.api.services.youtube.model.PlaylistItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PlaylistItemSynchroniser {

    private String url;

    public PlaylistItemSynchroniser() throws IOException {
        InputStream input = this.getClass().getResourceAsStream("application.properties");
        Properties props = new Properties();

        if (input == null) {
            System.err.println("Unable to load application.properties!");
            return;
        }

        props.load(input);
        url = props.getProperty("api.lesson_meta_url");
    }

    public boolean synchronise(PlaylistItem playlistItem, String playlistId) {
        String json = playlistItem.toString();
        String idJson = ",\"playlistId\": \"" + playlistId + "\"}";

        String newJson = "";
        for (int i = 0; i < json.length(); i++) {
            newJson += json.charAt(i);

            if (i == json.length() - 2) {
                newJson += idJson;
            }
        }

        HttpRequestSender requestSender = new HttpRequestSender();

        if (!requestSender.sendPostRequest(newJson, url)) {
            System.err.println("Sync attempt failed for playlist items!");
            return false;
        } else {
            System.out.println("Sync attempt successful for playlist items");
            return true;
        }
    }
}
