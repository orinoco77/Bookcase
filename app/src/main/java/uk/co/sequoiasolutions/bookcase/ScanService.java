package uk.co.sequoiasolutions.bookcase;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

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
    private ResultReceiver rec;
    private SharedPreferences sharedPref;
    /**
     * Starts this service to perform action ScanEbooks with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionScanEbooks(Context context, String path, ScanResultReceiver mReceiver) {
        Intent intent = new Intent(context, ScanService.class);
        intent.setAction(ACTION_SCAN_EBOOKS);
        intent.putExtra(PATH, path);
        intent.putExtra("receiverTag", mReceiver);

        context.startService(intent);
    }

    public ScanService() {
        super("ScanService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            rec = intent.getParcelableExtra("receiverTag");
            final String action = intent.getAction();
            if (ACTION_SCAN_EBOOKS.equals(action)) {
                final String path = intent.getStringExtra(PATH);
                handleActionScanEbooks();
                Bundle b = new Bundle();
                rec.send(0, b);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionScanEbooks() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String path = sharedPref.getString("path", "");
        if (path.equals(""))
            path = "/sdcard/";
        getBooksFromPath(path);
    }

    private void getBooksFromPath(String path) {
        File f = new File(path);
        File files[] = f.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    getBooksFromPath(file.getAbsolutePath());
                if (file.getName().endsWith(".epub"))
                    try {
                        scanEbook(file.getAbsolutePath());
                    } catch (IOException e) {
                        Bundle b = new Bundle();
                        b.putString("errorMessage", "Failed to find the path specified.");
                        rec.send(1, b);
                    }
            }
        } else {
            Bundle b = new Bundle();
            b.putString("errorMessage", "Failed to find the path specified.");
            rec.send(1, b);
        }
    }


    private void scanEbook(String path) throws IOException {

        EbookDataSource dataSource = new EbookDataSource(this);
        dataSource.open();
        // Load Book from inputStream

        Book book = (new EpubReader()).readEpub(new FileInputStream(path));

        Ebook ebook = new Ebook();
        String authors = "";
        for (int i = 0; i < book.getMetadata().getAuthors().size(); i++) {
            authors += book.getMetadata().getAuthors().get(i) + "; ";
        }
        authors = authors.substring(0, authors.length() - 2);
        ebook.setAuthor(authors);
        ebook.setTitle(book.getTitle());
        if (!book.getMetadata().getDescriptions().isEmpty()) {
            ebook.setDescription(book.getMetadata().getDescriptions().get(0));
        } else {
            ebook.setDescription("");
        }
        ebook.setEbookUrl(new File(path).getName());
        ebook.setImageUrl(book.getCoverImage().getData());
            if (!dataSource.ebookExists(ebook.getEbookUrl())) {
                dataSource.createEbook(ebook);
            }

        dataSource.close();
    }

}
