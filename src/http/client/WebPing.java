package http.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class WebPing {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage java WebPing <server host name> <server port number>");
            return;
        }

        String httpServerHost = args[0];
        int httpServerPort = Integer.parseInt(args[1]);

        try {
            InetAddress addr;
            Socket sock = new Socket(httpServerHost, httpServerPort);
            addr = sock.getInetAddress();

            // A voir si ça sert plus tard
            PrintStream sender = new PrintStream(sock.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            System.out.println("Connected to " + addr);
            sock.close();
        } catch (java.io.IOException e) {
            System.out.println("Can't connect to " + httpServerHost + ":" + httpServerPort);
            System.out.println(e);
        }
    }
}