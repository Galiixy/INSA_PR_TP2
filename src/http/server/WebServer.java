///A Simple Web Server (WebServer.java)

package http.server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Author : Gaëlle & Mathis
 */
public class WebServer {

    /**
     * WebServer constructor.
     */
    protected void start() {

        ServerSocket s;

        System.out.println("Webserver starting up on port 3000");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(3000);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        while (true) {
            try {
                // wait for a connection
                Socket remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");

                new WebServerCommunication(remote).start();

            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
    }

    /**
     * Start the application.
     *
     * @param args Command line parameters are not used.
     */
    public static void main(String args[]) {
        WebServer ws = new WebServer();
        ws.start();
    }
}
