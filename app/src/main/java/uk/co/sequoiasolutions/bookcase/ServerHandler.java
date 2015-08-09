package uk.co.sequoiasolutions.bookcase;

/**
 * Created by ajs on 07/08/2015.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
            answer = "<html><head><title>test</title></head><body>Test<br />" + querystring + "</body></html>";
        }

        return newFixedLengthResponse(answer);
    }
}