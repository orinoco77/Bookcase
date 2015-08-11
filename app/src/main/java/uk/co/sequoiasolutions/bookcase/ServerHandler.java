package uk.co.sequoiasolutions.bookcase;

/**
 * Created by ajs on 07/08/2015.
 */

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;
import fi.iki.elonen.SimpleWebServer;

public class ServerHandler extends NanoHTTPD {

    public int Port;
    public String Path;

    public ServerHandler(int port) {
        super(port);
        Port = port;
    }

    public void run() {
        ServerRunner.run(SimpleWebServer.class);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String querystring = session.getQueryParameterString();
        String answer = "";
        String filepath = Path
                + session.getUri();
        if (querystring == null) {

            FileInputStream fis = null;
            long bytes = 0;
            try {
                fis = new FileInputStream(filepath);
                File file = new File(filepath);
                bytes = file.length();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return newFixedLengthResponse(Response.Status.OK, "application/epub+zip", fis, bytes);

        } else {
            String[] params = querystring.split("&");
            for (String param : params) {
                String key = param.split("=")[0];
                if (key.equals("getall")) {
                    EbookDataSource dataSource = new EbookDataSource(MainActivity.ma.getApplicationContext()); //ugly but possibly unavoidable
                    dataSource.open();
                    List<Ebook> ebooks = dataSource.getAllEbooks();
                    for (Ebook book : ebooks) {
                        try {
                            answer += new JSONObject().put("title", book.getTitle()).put("author", book.getAuthor()).toString();
                        } catch (JSONException ex) {
                            answer += ""; //stupid, I know, but I couldn't leave an empty catch in there
                        }
                    }
                }
            }
        }

        return newFixedLengthResponse(answer);
    }
}