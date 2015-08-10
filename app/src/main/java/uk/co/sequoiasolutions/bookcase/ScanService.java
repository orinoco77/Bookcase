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

    /**
     * Starts this service to perform action ScanEbooks with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionScanEbooks(Context context) {
        Intent intent = new Intent(context, ScanService.class);
        intent.setAction(ACTION_SCAN_EBOOKS);

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
                handleActionScanEbooks();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionScanEbooks() {
        String url = "https://www.googleapis.com/books/v1/volumes?q=The+Colour+of+Magic+-+Terry+Pratchett.epub&key=AIzaSyDNxF8IsW8_TzLK8Jt_98qgOiQW2KFQ6Hc";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
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
    }
}