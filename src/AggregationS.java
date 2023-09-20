import org.json.JSONObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationS {
    public static void main(String[] args) throws IOException {
        int port = 4567;
        String savedPayload = "";

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String inputLine = in.readLine();

                    if ("GET".equalsIgnoreCase(inputLine)) {
                        out.println(savedPayload);
                    } else {
                        savedPayload = inputLine;
                        try (FileWriter file = new FileWriter("/Weather Data/src/data1.txt")) {
                            file.write(inputLine);
                        }
                        // Send OK status to Content Server
                        out.println("OK");
                    }
                }
            }
        }
    }
}
