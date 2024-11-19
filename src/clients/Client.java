package clients;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader consoleInput;
    private final String name;
    private final int port;
    private final String host;


    public Client(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        try {
            this.socket = new Socket(this.host, this.port);
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            consoleInput = new BufferedReader(new InputStreamReader(System.in));
            out.println(this.name);

            new Thread(this::waitForMessage).start();
            new Thread(this::sendMessage).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void waitForMessage() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.out.println("You have been disconnected");

        } finally {
            close();
        }
    }


    public void sendMessage() {
        try {
            String input;
            while ((input = consoleInput.readLine()) != null) {
                out.println(input);
                if (input.equals("disconnect")) {
                    close();
                }
            }
        } catch (IOException e) {
            System.out.println("Error during sending message.");
        }
    }

    public void close() {
        try {
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            System.out.printf("Error during closing the socket - %s\n", e.getMessage());
        }
    }

    @Override
    public void run() {

    }
}
