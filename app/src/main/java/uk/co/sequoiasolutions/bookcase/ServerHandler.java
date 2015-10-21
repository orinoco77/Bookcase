package uk.co.sequoiasolutions.bookcase;

/**
 * Created by ajs on 07/08/2015.
 */

import android.support.annotation.NonNull;
import android.util.Base64;

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

            String jsonString = "";
            for (String param : params) {
                String[] keyValue = param.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equals("getall")) {
                    EbookDataSource dataSource = new EbookDataSource(MainActivity.ma.getApplicationContext()); //ugly but possibly unavoidable
                    dataSource.open();
                    List<Ebook> ebooks = dataSource.getAllEbooks();
                    answer = getJSONString(ebooks, ebooks.size());
                    }
                if (key.equals("getbatch")) {
                    EbookDataSource dataSource = new EbookDataSource(MainActivity.ma.getApplicationContext()); //ugly but possibly unavoidable
                    dataSource.open();
                    List<Ebook> ebooks = dataSource.getBatch(Integer.parseInt(value), 20);
                    int max = dataSource.getAllEbooks().size();
                    answer = getJSONString(ebooks, max);
                }
                if (key.equals("refresh")) {
                    ScanService.startActionScanEbooks(MainActivity.ma.getApplicationContext(), Path, MainActivity.ma.mReceiver);
                }
            }
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", answer);
    }

    @NonNull
    private String getJSONString(List<Ebook> ebooks, int max) {
        String jsonString = "";
        jsonString += "{\n \"count\": " + ebooks.size() + ",";
        jsonString += "\n \"max\": " + max + ",";
        jsonString += "\n \"ebook\": [";
        for (Ebook book : ebooks) {
            jsonString += "\n {";
            jsonString += "\n \"title\": \"" + book.getTitle().replace("\"", "\\\"") + "\",";
            jsonString += "\n \"author\": \"" + book.getAuthor().replace("\"", "\\\"") + "\",";
            jsonString += "\n \"description\": \"" + book.getDescription().replace("\"", "\\\"") + "\",";
            jsonString += "\n \"imageUrl\": \"" + Base64.encodeToString(book.getImageUrl(), Base64.DEFAULT) + "\",";
            jsonString += "\n \"ebookUrl\": \"" + book.getEbookUrl() + "\"";
            jsonString += "\n },";
        }
        jsonString = jsonString.substring(0, jsonString.length() - 1);
        jsonString += "\n ]";
        jsonString += "\n }";
        return jsonString;
    }
}