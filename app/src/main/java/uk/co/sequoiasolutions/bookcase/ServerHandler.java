package uk.co.sequoiasolutions.bookcase;

/**
 * Created by ajs on 07/08/2015.
 */

import org.orman.mapper.C;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.SimpleWebServer;

public class ServerHandler extends NanoHTTPD {

    public int Port;
    public String Path;

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

            String jsonString = "";
            for (String param : params) {
                String[] keyValue = param.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equals("getauthors")) {
                    List<Author> authors = Model.fetchQuery(ModelQuery.select().from(Author.class).where(C.like(Author.class, "name", "% " + value + "%")).getQuery(), Author.class);
                    answer = getAuthorsJSON(authors);
                }
                if (key.equals("getauthorbooks")) {
                    Author author = (Author) Model.fetchSingle(ModelQuery.select().from(Author.class).where(C.eq(Author.class, "id", value)).getQuery(), Author.class);
                    answer = getAuthorEbooksJSON(author.Ebooks);
                }
                if (key.equals("getsearch")) {
                    List<Author> authors = Model.fetchQuery(ModelQuery.select().from(Author.class).where(C.like("name", "%" + value + "%")).getQuery(), Author.class);
                    List<Ebook> ebooks = new ArrayList<>();
                    for (Author author : authors) {
                        ebooks.addAll(author.Ebooks);
                    }
                    ebooks.addAll(Model.fetchQuery(ModelQuery.select().from(Ebook.class).where(C.like("title", "%" + value + "%")).getQuery(), Ebook.class));
                    answer = getAuthorEbooksJSON(ebooks);
                }
                if (key.equals("refresh")) {
                    ScanService.startActionScanEbooks(MainActivity.ma.getApplicationContext(), Path, MainActivity.ma.mReceiver);
                }
            }
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", answer);
    }

    private String getAuthorsJSON(List<Author> authors) {
        String jsonString = "";
        jsonString += "{\n \"count\": " + authors.size() + ",";

        jsonString += "\n \"author\": [";
        String authorsString = "";
        for (Author author : authors) {
            if (authorsString.length() > 0)
                authorsString += ",";
            authorsString += "\n {";
            authorsString += "\n \"name\": \"" + author.Name + "\",";
            authorsString += "\n \"id\": " + author.Id;
            authorsString += "\n }";
        }
        jsonString += authorsString;
        jsonString += "\n ]";

        jsonString += "\n }";
        return jsonString;
    }

    private String getAuthorEbooksJSON(List<Ebook> ebooks) {
        String jsonString = "";
        jsonString += "{\n \"count\": " + ebooks.size() + ",";
        jsonString += "\n \"ebook\": [";
        String ebookString = "";
        for (Ebook book : ebooks) {
            if (ebookString.length() > 0)
                ebookString += ",";
            ebookString += "\n {";
            ebookString += "\n \"title\": \"" + book.Title.replace("\"", "\\\"") + "\",";
            String authors = "";
            for (Author author : book.Authors) {
                if (!authors.equals("")) {
                    authors += "; ";
                }
                authors += author.Name;
            }
            ebookString += "\n \"id\": " + book.Id + ",";
            ebookString += "\n \"author\": \"" + authors.replace("\"", "\\\"").replace("\n", "") + "\",";
            ebookString += "\n \"description\": \"" + book.Description.replace("\"", "\\\"").replace("\n", "") + "\",";
            ebookString += "\n \"imageUrl\": \"" + book.ImageUrl.replace("\n", "") + "\",";
            ebookString += "\n \"ebookUrl\": \"" + book.EbookUrl + "\"";
            ebookString += "\n }";
        }
        jsonString += ebookString;
        jsonString += "\n ]";
        jsonString += "\n }";
        return jsonString;
    }
}