package uk.co.sequoiasolutions.bookcase;

/**
 * Handles incoming http requests
 */

import android.util.Base64;

import com.google.gson.Gson;

import org.orman.mapper.C;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.SimpleWebServer;

public class ServerHandler extends NanoHTTPD {

    private int Port;
    private String Path;

    public ServerHandler(int port, String path) {
        super(port);
        Port = port;
        Path = path;
    }

    public void run() {
        ServerRunner.run(SimpleWebServer.class);
    }

    @Override
    public Response serve(IHTTPSession session) {

        String querystring = session.getQueryParameterString();
        String answer = "";
        String filepath = session.getUri().substring(1);
        if (querystring == null) {
            Ebook ebook = Model.fetchSingle(ModelQuery.select().from(Ebook.class).where(C.eq("id", filepath)).getQuery(), Ebook.class);
            FileInputStream fis = null;
            long bytes = 0;
            try {
                fis = new FileInputStream(ebook.EbookUrl);
                File file = new File(ebook.EbookUrl);
                bytes = file.length();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK, "application/epub+zip", fis, bytes);

        } else {
            String[] params = querystring.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equals("getauthors")) {
                    List<Author> authors = Model.fetchQuery(ModelQuery.select().from(Author.class).where(C.like(Author.class, "surname", value + "%")).getQuery(), Author.class);
                    Gson gson = new Gson();
                    List<AuthorJson> jsonAuthors = new ArrayList<>();
                    for (Author author : authors) {
                        jsonAuthors.add(AuthorJson.For(author));
                    }
                    answer = gson.toJson(jsonAuthors);
                    //answer = getAuthorsJSON(authors);
                }
                if (key.equals("getauthorbooks")) {
                    Author author = Model.fetchSingle(ModelQuery.select().from(Author.class).where(C.eq(Author.class, "id", value)).getQuery(), Author.class);
                    Gson gson = new Gson();
                    List<EbookJson> ebookJson = new ArrayList<>();
                    for (Ebook ebook : author.Ebooks) {
                        ebookJson.add(EbookJson.For(ebook));
                    }
                    answer = gson.toJson(ebookJson);
                    //answer = getAuthorEbooksJSON(author.Ebooks);
                }
                if (key.equals("getsearch")) {
                    List<Author> authors = Model.fetchQuery(ModelQuery.select().from(Author.class).where(C.like("forename || ' ' || surname", "%" + value + "%")).getQuery(), Author.class);
                    List<Ebook> ebooks = new ArrayList<>();
                    for (Author author : authors) {
                        ebooks.addAll(author.Ebooks);
                    }
                    ebooks.addAll(Model.fetchQuery(ModelQuery.select().from(Ebook.class).where(C.like("title", "%" + value + "%")).getQuery(), Ebook.class));
                    Gson gson = new Gson();
                    List<EbookJson> ebookJson = new ArrayList<>();
                    for (Ebook ebook : ebooks) {
                        ebookJson.add(EbookJson.For(ebook));
                    }
                    answer = gson.toJson(ebookJson);
                    //answer = getAuthorEbooksJSON(ebooks);
                }
                if (key.equals("getCover")) {
                    ImageData image = Model.fetchSingle(ModelQuery.select().from(ImageData.class).where(C.eq("Id", value)).getQuery(), ImageData.class);
                    byte[] bytes = Base64.decode(image.Base64CoverImage, Base64.DEFAULT);
                    ByteArrayInputStream fis;
                    long length;

                    fis = new ByteArrayInputStream(bytes);

                    length = bytes.length;

                    return newFixedLengthResponse(Response.Status.OK, "image/jpeg", fis, length);

                }
                if (key.equals("refresh")) {
                    ScanService.startActionScanEbooks(MainActivity.ma.getApplicationContext(), Path, MainActivity.ma.mReceiver);
                }
            }
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", answer);
    }
}