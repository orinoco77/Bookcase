package uk.co.sequoiasolutions.bookcase;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ScanService extends IntentService {

    private static final String ACTION_SCAN_EBOOKS = "uk.co.sequoiasolutions.bookcase.action.ScanEbooks";
    private static final String PATH = "uk.co.sequoiasolutions.bookcase.extra.Path";
    /**
     * Starts this service to perform action ScanEbooks with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionScanEbooks(Context context, String path) {
        Intent intent = new Intent(context, ScanService.class);
        intent.setAction(ACTION_SCAN_EBOOKS);
        intent.putExtra(PATH, path);

        context.startService(intent);
    }

    public ScanService() {
        super("ScanService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SCAN_EBOOKS.equals(action)) {
                final String path = intent.getStringExtra(PATH);
                handleActionScanEbooks(path);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionScanEbooks(String path) {
        String responseString = "";
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + path.replace(" ", "%20").replace(".epub", "") + "&key=AIzaSyDNxF8IsW8_TzLK8Jt_98qgOiQW2KFQ6Hc";
        try {
            //it's deprecated, but it works, which is more than can be said for the alternatives.
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
                //..more logic
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        EbookDataSource dataSource = new EbookDataSource(this);
        dataSource.open();
        Ebook ebook = null;
        try {
            ebook = getEbookFromJSON(responseString, path);
            if (!dataSource.ebookExists(ebook.getEbookUrl())) {
                dataSource.createEbook(ebook);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataSource.close();
    }

    private Ebook getEbookFromJSON(String responseString, String path) throws JSONException {
        Ebook result = new Ebook();
        JSONObject json = null;
        json = new JSONObject(responseString);
        if (json != null) {
            JSONArray items = null;
            items = json.getJSONArray("items");
            if (items != null) {
                JSONObject item = null;
                item = items.getJSONObject(0);
                if (item != null) {
                    JSONObject info = item.getJSONObject("volumeInfo");
                    if (info != null) {
                        try {
                            result.setTitle(info.getString("title"));
                        } catch (JSONException ex) {
                            result.setTitle("");
                        }
                        try {
                            result.setAuthor(info.getJSONArray("authors").getString(0));
                        } catch (JSONException ex) {
                            result.setAuthor("");
                        }
                        try {
                            result.setDescription(info.getString("description"));
                        } catch (JSONException ex) {
                            result.setDescription("");
                        }
                        try {
                            result.setImageUrl(info.getJSONObject("imageLinks").getString("smallThumbnail"));
                        } catch (JSONException ex) {
                            result.setImageUrl("");
                        }
                        result.setEbookUrl(path);
                    }
                }
            }
        }
        return result;
    }
}
