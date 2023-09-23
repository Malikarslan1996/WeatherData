import com.fasterxml.jackson.databind.JsonNode;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;

public class ContentServer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <ServerName:Port> <FilePath>");
            return;
        }


        String[] serverInfo = args[0].split(":");
        if (serverInfo.length != 2) {
            System.out.println("Invalid server information. It should be in the format ServerName:Port");
            return;
        }

        String serverName = serverInfo[0];
        int portNumber;
        try {
            portNumber = Integer.parseInt(serverInfo[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
            return;
        }


        String filePath = args[1];


        try (Socket socket = new Socket(serverName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {


            JsonNode payload = JsonFormatter.format(filePath);

            if (payload != null) {
                out.println("PUT");
                out.println(payload.toString());


                String response = in.readLine();
                System.out.println("Received from Aggregation Server: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
