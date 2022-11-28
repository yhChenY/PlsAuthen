public class CTAP2Main {
    public static void main(String[] args) {
        Token token = new Token("202213");
        token.setup();
        token.bind();
//        Client.authorize("", token);
        Client client = new Client();
        Server server = new Server();
        server.ids = "example.com";
        client.clientId = 1;
        client.register(token,"example.com",server);
    }
}
