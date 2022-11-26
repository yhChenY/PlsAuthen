public class serverside {
    public static void main(String[] args) {
        Receiver r = new Receiver(12345);
        r.startWaiting();
        //r.startSending();
        //r.startReceiving();
    }
}
