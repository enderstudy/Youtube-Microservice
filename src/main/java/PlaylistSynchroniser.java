import com.google.api.services.youtube.model.Playlist;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PlaylistSynchroniser {

    private String url;

    public PlaylistSynchroniser() throws IOException {
        InputStream input = this.getClass().getResourceAsStream("application.properties");
        Properties props = new Properties();

        if (input == null) {
            System.err.println("Unable to load application.properties!");
            return;
        }

        props.load(input);
        url = props.getProperty("api.course_meta_url");
    }

    public boolean synchronise(Playlist playlist) {
        String json = playlist.toString();

        HttpRequestSender requestSender = new HttpRequestSender();
        if (!requestSender.sendPostRequest(json, url)) {
            System.err.println("Sync attempt failed for playlist: " + playlist.getId());
            return false;
        } else {
            System.out.println("Sync attempt successful for playlist: " + playlist.getId());
            return true;
        }
    }
}
