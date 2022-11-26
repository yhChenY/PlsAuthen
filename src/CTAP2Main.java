public class CTAP2Main {
    public static void main(String[] args) {
        Token token = new Token("2022113");
        token.setup();
        token.bind();
        Client.authorize("", token);
    }
}
