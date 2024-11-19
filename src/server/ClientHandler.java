package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final String[] banPhrases;
    private final Socket handler;
    private final Server server;

    private String[] recipients;

    private String message;


    private int MessageCounter;

    public ClientHandler(Socket socket, String[] banPhrases, Server server) {
        this.handler = socket;
        this.banPhrases = banPhrases;
        this.server = server;
        MessageCounter = 0;
    }

    public void splitMessage(String message){
        String[] messageParts = message.split(":");
        this.recipients = messageParts[0].split("\\|");
        this.message = messageParts[1];
    }

    public boolean validCheck(){
        String[] messageWords = message.trim().split(" ");
        for (String word: messageWords){
            for (String banWord: banPhrases){
                if (word.equals(banWord)){
                    return false;
                }
            }
        }
        return true;
    }

    public Socket getClientSocket(){
        return handler;
    }

    @Override
    public void run() {
        try (BufferedReader clientIn = new BufferedReader(new InputStreamReader(handler.getInputStream()))) {
            String message;
            while ((message = clientIn.readLine()) != null) {
                System.out.println(message);
                if (MessageCounter == 0 && message.matches("^@.+")) {
                    server.addClient(message, this);
                    System.out.println("client " + message + " added");
                    MessageCounter++;
                }else if (message.matches("^#@([a-zA-Z0-9_]+(\\|@[a-zA-Z0-9_]+)*):.+$")){
                    splitMessage(message);
                    if (validCheck()){
                        server.sendEveryoneExcept(this.message, recipients);
                    }else {
                        break;
                    }
                }else if (message.matches("^@([a-zA-Z0-9_]+(\\|@[a-zA-Z0-9_]+)*):.+$")) {
                    splitMessage(message);
                    if (validCheck()){
                        server.sendToThisList(this.message, recipients);
                    }else {
                        break;
                    }
                }else if (message.matches("^@.+:.+$")) {
                    splitMessage(message);
                    if (validCheck()){
                        server.sendToThis(this.message, recipients[0]);
                    }else {
                        break;
                    }
                }else if (message.equals("send ban phrases")){
                    server.sendBanPhrases(this);
                } else if (message.equals("disconnect")){
                    server.sendToEveryone("User" + server.getClientName(this) + "has been disconnected.", this);
                }
                else {
                    this.message = message;
                    if (validCheck()){
                        server.sendToEveryone(this.message, this);
                    }else {
                        break;
                    }
                }

            }
            ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



}
