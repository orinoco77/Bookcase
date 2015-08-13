package uk.co.sequoiasolutions.bookcase;

/**
 * Created by ajs on 07/08/2015.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
        String ipAddress = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipAddress = inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
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
                String key = param.split("=")[0];
                if (key.equals("getall")) {
                    EbookDataSource dataSource = new EbookDataSource(MainActivity.ma.getApplicationContext()); //ugly but possibly unavoidable
                    dataSource.open();
                    List<Ebook> ebooks = dataSource.getAllEbooks();
                    jsonString += "{\n \"count\": " + ebooks.size() + ",";
                    jsonString += "\n \"ebook\": [";
                        for (Ebook book : ebooks) {
                            jsonString += "\n {";
                            jsonString += "\n \"title\": \"" + book.getTitle() + "\",";
                            jsonString += "\n \"author\": \"" + book.getAuthor() + "\",";
                            jsonString += "\n \"description\": \"" + book.getDescription().replace("\"", "\\\"") + "\",";
                            jsonString += "\n \"imageUrl\": \"" + book.getImageUrl() + "\",";
                            jsonString += "\n \"ebookUrl\": \"" + book.getEbookUrl() + "\"";
                            jsonString += "\n },";
                        }
                    jsonString = jsonString.substring(0, jsonString.length() - 1);
                    jsonString += "\n ]";
                    jsonString += "\n }";
                    answer = jsonString;
                    }
            }
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", answer);
    }
}