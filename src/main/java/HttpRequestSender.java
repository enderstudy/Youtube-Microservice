import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class HttpRequestSender {

    /**
     * Sends a HTTP Request. Return type is boolean since we're not interested
     * in anything the API sends back, we only need to know if it was successful or not.
     *
     * @param data - The JSON serialised data to be sent to the API
     * @return boolean
     */
    public boolean sendPostRequest(String data, String url) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(data, ContentType.APPLICATION_JSON);

        request.addHeader("Content-Type", "application/json");
        request.setEntity(requestEntity);

        try {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                System.err.println("HTTP status code: " + statusCode);
                System.err.println(response.getStatusLine().getReasonPhrase());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
