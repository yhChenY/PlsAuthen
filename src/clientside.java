public class clientside {
    public static void main(String[] args) {
        Sender s = new Sender("localhost", 12345);
//        s.startSending();
        s.startReceiving();
        s.send("sendtest");
    }
}
