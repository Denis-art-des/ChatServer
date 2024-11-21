package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server implements Runnable {

    private final String name;
    private final int port;
    private final String[] banPhrases;

    private HashMap<String, ClientHandler> clients;


    public Server(String name, int port, String[] banPhrases) {
        this.name = name;
        this.port = port;
        this.banPhrases = banPhrases;
        this.clients = new HashMap<>();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println(name + " running.");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, banPhrases, this)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addClient(String name, ClientHandler client) {
        clients.put(name, client);
    }


    public synchronized void sendEveryoneExcept(String message, ArrayList<String> notRecipients) {
        Set<String> tmpClients = clients.keySet();
        for (String name : notRecipients) {
            if (tmpClients.contains(name)) {
                tmpClients.remove(name);
            }
        }
        for (String client : tmpClients) {
            try {
                OutputStream clientOutputStream = clients.get(client).getClientSocket().getOutputStream();
                PrintWriter recipientOut = new PrintWriter(clientOutputStream, true);
                recipientOut.println(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void sendToThisList(String message, ArrayList<String> recipients) {
        for (String name : recipients) {
            if (clients.containsKey(name))
                try {
                    OutputStream clientOutputStream = clients.get(name).getClientSocket().getOutputStream();
                    PrintWriter recipientOut = new PrintWriter(clientOutputStream, true);
                    recipientOut.println(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    public synchronized void sendToThis(String message, String recipient) {
        ClientHandler rec = clients.get(recipient);
        if(rec == null){
            return;
        }
        try {
            OutputStream clientOutputStream = rec.getClientSocket().getOutputStream();
            PrintWriter recipientOut = new PrintWriter(clientOutputStream, true);
            recipientOut.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void sendToEveryone(String message, ClientHandler sender) {
        for (ClientHandler client : clients.values()) {
            try {
                if (!client.equals(sender)) {
                    OutputStream clientOutputStream = client.getClientSocket().getOutputStream();
                    PrintWriter recipientOut = new PrintWriter(clientOutputStream, true);
                    recipientOut.println(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void sendBanPhrases(ClientHandler asker) {
        StringBuilder banPhrases = new StringBuilder();
        for (String banPhrase : this.banPhrases) {
            banPhrases.append(banPhrase + " ");
        }
        try {
            OutputStream clientOutputStream = asker.getClientSocket().getOutputStream();
            PrintWriter recipientOut = new PrintWriter(clientOutputStream, true);
            recipientOut.println(banPhrases);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(getClientName(client));
    }

    public synchronized String getClientName(ClientHandler asker) {
        String askerName = null;
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (entry.getValue().equals(asker)) {
                askerName = entry.getKey();
            }
        }
        return askerName;
    }
}
