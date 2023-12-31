import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.URL;

public class GETClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final LamportClock lamportClock = new LamportClock();
    private static final int MAX_RETRIES = 3;

    public static void main(String[] args) {
        String serverName;
        int portNumber;

        try {
            URL url = new URL(args[0].startsWith("http://") ? args[0] : "http://" + args[0]);
            serverName = url.getHost();
            portNumber = url.getPort();

            if (portNumber == -1) {
                System.out.println("Port number must be specified.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Invalid server information.");
            return;
        }

        String stationID = null;
        if (args.length >= 2) {
            stationID = args[1];
        }

        for (int i = 0; i < MAX_RETRIES; i++) {
            try (Socket socket = new Socket(serverName, portNumber);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                lamportClock.tick();

                if (stationID != null && !stationID.isEmpty()) {
                    System.out.println("Sending request: " + "GET " + stationID + " TIMESTAMP " + lamportClock.getTime());
                    out.println("GET " + stationID + " TIMESTAMP " + lamportClock.getTime());
                } else {
                    System.out.println("Sending request: " + "GET TIMESTAMP " + lamportClock.getTime());
                    out.println("GET TIMESTAMP " + lamportClock.getTime());
                }

                String response = in.readLine();

                if (isValidJSON(response)) {
                    JsonNode jsonObject = objectMapper.readTree(response);


                    if (jsonObject.isObject()) {

                        formatAndPrintData(jsonObject);
                    } else if (jsonObject.isArray()) {

                        for (final JsonNode objNode : jsonObject) {
                            formatAndPrintData(objNode);
                        }
                    }
                } else {
                    System.out.println("Received an invalid JSON response.");
                }

                break;
            } catch (IOException e) {
                if (i == MAX_RETRIES - 1) {
                    System.out.println("Failed after " + MAX_RETRIES + " attempts.");
                    e.printStackTrace();
                } else {
                    System.out.println("Attempt " + (i + 1) + " failed. Retrying...");
                }
            }
        }
    }

    private static void formatAndPrintData(JsonNode jsonObject) {
        formatAndPrintData(jsonObject, "");
    }

    private static void formatAndPrintData(JsonNode jsonObject, String prefix) {
        jsonObject.fieldNames().forEachRemaining(field -> {
            JsonNode node = jsonObject.get(field);
            if (node.isObject()) {
                System.out.println(prefix + field + ":");
                formatAndPrintData(node, prefix + "  ");
            } else {
                System.out.printf(prefix + "%-30s : %s%n", field, node);
            }
        });
        System.out.println();
    }
    private static boolean isValidJSON(String test) {
        try {
            objectMapper.readTree(test);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
