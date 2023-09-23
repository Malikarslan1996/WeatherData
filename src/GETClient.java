import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class GETClient {
    private static int lamportTimestamp = 0;
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
            System.out.println("Invalid server information. It should be in one of the formats: http://servername.domain.domain:portnumber, http://servername:portnumber, or servername:portnumber");
            return;
        }

        String filePath = null;
        String stationID = null;

        if (args.length >= 2) {
            filePath = args[1];
        }

        if (args.length >= 3) {
            stationID = args[2];
        }

        int maxAttempts = 3;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try (Socket socket = new Socket(serverName, portNumber);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                lamportTimestamp++;
                if (stationID != null) {
                    out.println("GET " + stationID + " TIMESTAMP " + lamportTimestamp);
                } else {
                    out.println("GET TIMESTAMP " + lamportTimestamp);
                }

                String response = in.readLine();
                System.out.println("Received response: " + response);
                if (isValidJSON(response)) {
                    try {
                        JsonNode jsonObject = objectMapper.readTree(response);
                        displayAttribute("Name", jsonObject.get("name").asText());
                        displayAttribute("State", jsonObject.get("state").asText());
                        displayAttribute("Air Temperature", jsonObject.get("air_temp").asDouble() + "°C");
                        displayAttribute("Wind Speed (km/h)", jsonObject.get("wind_spd_kmh").asDouble());
                        displayAttribute("Wind Speed (kt)", jsonObject.get("wind_spd_kt").asInt());
                        displayAttribute("Wind Direction", jsonObject.get("wind_dir").asText());
                        displayAttribute("Time Zone", jsonObject.get("time_zone").asText());
                        displayAttribute("Humidity", jsonObject.get("rel_hum").asInt() + "%");
                        displayAttribute("Cloud", jsonObject.get("cloud").asText());
                        displayAttribute("Longitude", jsonObject.get("lon").asDouble());
                        displayAttribute("Latitude", jsonObject.get("lat").asDouble());
                        displayAttribute("Dew Point", jsonObject.get("dewpt").asDouble());
                        displayAttribute("Apparent Temperature", jsonObject.get("apparent_t").asDouble());
                        displayAttribute("Pressure", jsonObject.get("press").asDouble());
                        displayAttribute("Local Date Time (Full)", jsonObject.get("local_date_time_full").asDouble());
                        displayAttribute("Local Date Time", jsonObject.get("local_date_time").asText());
                        displayAttribute("ID", jsonObject.get("id").asText());
                        break;
                    } catch (IOException e) {
                        System.out.println("Error parsing the JSON response.");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Received an invalid JSON response.");
                    return;
                }

            } catch (IOException e) {
                if (attempt == maxAttempts - 1) {
                    System.out.println("Failed after " + maxAttempts + " attempts.");
                    e.printStackTrace();
                } else {

                    try {
                        long backoffTime = (long) (Math.pow(2, attempt) * 1000);
                        TimeUnit.MILLISECONDS.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

        private static boolean isValidJSON(String test) {
            try {
                objectMapper.readTree(test);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }

        private static void displayAttribute(String name, Object value) {
            System.out.printf("%-30s : %s%n", name, value);
        }
    }