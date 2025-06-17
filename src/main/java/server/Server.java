package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import server.router.Router;
import java.net.InetSocketAddress;

public class Server {
    private HttpServer server;

    // Ini adalah handler utama yang akan mendelegasikan ke router
    private class MainRequestHandler implements HttpHandler {
        private final Router router = new Router(); // Instansiasi Router

        @Override
        public void handle(HttpExchange httpExchange) {
            try {
                router.handle(httpExchange); // Delegasikan permintaan ke Router
            } catch (Exception e) {
                System.err.println("Error in MainRequestHandler: " + e.getMessage());
                e.printStackTrace();
                // Pastikan respons dikirim bahkan jika Router gagal menanganinya
                try {
                    new Response(httpExchange).sendError(java.net.HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error during routing.");
                } catch (IOException ioException) {
                    System.err.println("Failed to send error response: " + ioException.getMessage());
                }
            }
        }
    }

    public Server(int port) throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 128);
        server.createContext("/", new MainRequestHandler()); // Semua request akan diarahkan ke MainRequestHandler
        server.start();
        System.out.println("Server started on port " + port);
    }
}