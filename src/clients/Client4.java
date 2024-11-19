package clients;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Client4 {
    public static void main(String[] args) {
        Properties clientProps = new Properties();
        try(FileInputStream inputStream = new FileInputStream("src\\clients\\client4Config.properties")) {
            clientProps.load(inputStream);
            String name = clientProps.getProperty("name");
            String host = clientProps.getProperty("host");
            int port = Integer.parseInt(clientProps.getProperty("port"));
            new Client(name, host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
