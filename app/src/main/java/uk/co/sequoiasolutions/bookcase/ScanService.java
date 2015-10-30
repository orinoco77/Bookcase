package uk.co.sequoiasolutions.bookcase;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Base64;

import org.orman.mapper.C;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
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

        File files[] = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".epub") || new File(dir.getAbsolutePath() + "/" + filename).isDirectory();
            }
        });
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getBooksFromPath(file.getAbsolutePath());
                    continue;
                }

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

        Book book = (new EpubReader()).readEpub(new FileInputStream(path));

        Ebook ebook = new Ebook();

        ebook.Title = book.getTitle();
        String description = "";
        for (int i = 0; i < book.getMetadata().getDescriptions().size(); i++) {

            description += book.getMetadata().getDescriptions().get(i) + "\n";
        }
        ebook.Description = description;
        ebook.EbookUrl = new File(path).getAbsolutePath();
        if (book.getCoverImage() != null) {
            ImageData imageData = new ImageData();
            imageData.Base64CoverImage = Base64.encodeToString(book.getCoverImage().getData(), Base64.DEFAULT);
            imageData.insert();
            ebook.ImageId = imageData.Id;
        }
        String ebookMatchCount = (String) Model.fetchSingleValue(ModelQuery.select().from(Ebook.class).where(C.eq(Ebook.class, "EbookUrl", ebook.EbookUrl)).count().getQuery());
        if (Integer.parseInt(ebookMatchCount) == 0) {
            ebook.insert();
            for (int i = 0; i < book.getMetadata().getAuthors().size(); i++) {

                Author author = new Author();
                author.Name = book.getMetadata().getAuthors().get(i).getFirstname() + " " + book.getMetadata().getAuthors().get(i).getLastname();
                Author matchAuthor = Model.fetchSingle(ModelQuery.select().from(Author.class).where(C.eq(Author.class, "Name", author.Name)).getQuery(), Author.class);
                if (matchAuthor == null) {
                    author.insert();
                    if (!author.Ebooks.contains(ebook))
                        author.Ebooks.add(ebook);
                    if (!ebook.Authors.contains(author))
                        ebook.Authors.add(author);
                } else {
                    if (!matchAuthor.Ebooks.contains(ebook)) {
                        matchAuthor.Ebooks.add(ebook);
                    }
                    if (!ebook.Authors.contains(matchAuthor)) {
                        ebook.Authors.add(matchAuthor);
                    }
                }
            }
        }

    }

}
