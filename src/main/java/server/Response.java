
package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Response {

    private HttpExchange httpExchange;
    private Headers headers;
    private StringBuilder stringBuilder;
    private boolean isSent;

    public Response(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        this.stringBuilder = new StringBuilder();
        this.isSent = false;
    }

    public void setBody(String string) {
        stringBuilder.setLength(0);
        stringBuilder.append(string);
    }

    public void send(int status) {
        try {
            this.httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            this.httpExchange.sendResponseHeaders(status, 0);

            String body = stringBuilder.toString();
            PrintStream out = new PrintStream(this.httpExchange.getResponseBody());
            out.write(body.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException ioe) {
            System.err.println("Problem encountered when sending response.");
            ioe.printStackTrace();
            return;
        } finally {
            this.httpExchange.close();
        }
        this.isSent = true;
    }

    public boolean isSent() {
        if (this.httpExchange.getResponseCode() != -1)
            this.isSent = true;
        return isSent;
    }
}