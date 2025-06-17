package server; // Pastikan ini sama dengan package Server.java

public class MainApp {

    public static void main(String[] args) throws Exception {
        int port = 8080; // Default port
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        System.out.printf("Listening on port: %s...\n", port);
        new Server(port); // Memulai server dari kelas Server
    }
}