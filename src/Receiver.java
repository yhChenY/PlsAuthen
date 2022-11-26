import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Receiver {
    ServerSocket server = null;
    Socket socket = null;
    int port = -1;
    boolean close = false;
    int nullCounter = 0;

    public Receiver(int port) {
        try {
            server = new ServerSocket(port);
            this.port = port;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startWaiting() {
        while (!server.isClosed()) {
            try {
                socket = server.accept();
                SocketThread socketThread = new SocketThread(socket, server);
                socketThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
