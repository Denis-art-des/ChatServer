package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties serverProps = new Properties();
        try(FileInputStream inputStream = new FileInputStream("src\\server\\config.properties")) {
            serverProps.load(inputStream);
            String name = serverProps.getProperty("name");
            int port = Integer.parseInt(serverProps.getProperty("port"));
            String[] banWords = new String[]{serverProps.getProperty("bun_phrases")};
            Server server = new Server(name, port, banWords);
            server.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
