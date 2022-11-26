import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Sender {
    String host = null;
    int port = -1;
    Socket socket = null;
    InputStream inS = null;
    OutputStream outS = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    PrintWriter pw = null;
    boolean closed = false;

    public Sender(String host, int port) {
        try {
            socket = new Socket(host, port);
            inS = socket.getInputStream();
            isr = new InputStreamReader(inS);
            outS = socket.getOutputStream();
            br = new BufferedReader(isr);
            pw = new PrintWriter(outS);
            System.out.printf("Socket Established at host = %s, port = %d\n", host, port);
            this.host = host;
            this.port = port;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String s){
        pw.println(s);
        pw.flush();
    }

//    public void startSending() {
//        Runnable r = new Runnable() {
//            @Override
//
//            public void run() {
//                Scanner sc = new Scanner(System.in);
//                while (!closed&&!socket.isClosed()) {
//                    String str = sc.nextLine();
//                    if(str.equals("exit")){
//                        try {
//                            inS.close();
//                            isr.close();
//                            outS.close();
//                            br.close();
//                            pw.close();
//                            socket.close();
//                            closed=true;
//                            break;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    pw.println(str);
//                    pw.flush();
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        Thread sendThread = new Thread(r);
//        sendThread.start();
//    }
    
    public void startReceiving() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (!closed && !socket.isClosed()) {
                    String str;
                    try {
                        str = br.readLine();
                        System.out.println("Received: " + str);
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
//                        e.printStackTrace();
                    }
                }
            }
        };
        Thread receiveThread = new Thread(r);
        receiveThread.start();
    }
    
}
