import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SocketThread extends Thread {
    public Socket socket;
    public InputStream inS = null;
    public OutputStream outS = null;
    public InputStreamReader isr = null;
    public BufferedReader br = null;
    public boolean close = false;
    ServerSocket server = null;
    public int nullCounter = 0;
    public PrintWriter pw = null;

    public SocketThread(Socket socket, ServerSocket serverSocket) throws IOException {
        this.socket = socket;
        this.server = serverSocket;
        inS = socket.getInputStream();
        outS = socket.getOutputStream();
        isr = new InputStreamReader(inS);
        br = new BufferedReader(isr);
        pw = new PrintWriter(outS);
        System.out.printf("Socket Established at port = %d\n", socket.getPort());
    }

    @Override
    public void run() {
        String str;
        while (!close && !server.isClosed()) {
            try {
                str = br.readLine();
                if (str != null) {
                    todo(str);
                    System.out.println("Received: " + str);
                    nullCounter = 0;
                } else {
                    nullCounter++;
                }
                Thread.sleep(500);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startSending() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Scanner sc = new Scanner(System.in);
                while (!close && !server.isClosed()) {
                    String str = sc.nextLine();
                    if (str.equals("exit")) {
                        try {
                            closesocket();
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {

                        }
                    }
                    pw.println(str);
                    pw.flush();
                }
            }
        };
        Thread sendThread = new Thread(r);
        sendThread.start();
    }

    public void send(String s) {
        pw.println(s);
        pw.flush();
    }

    public void todo(String s) {

    }//接受到的信息自后的逻辑处理

    public void closesocket() throws IOException {
        inS.close();
        isr.close();
        outS.close();
        br.close();
        pw.close();
        close = true;
        socket.close();
        this.stop();
    }

}

