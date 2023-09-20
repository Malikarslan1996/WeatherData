import org.json.JSONObject;
import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <ServerName:Port> <FilePath>");
            return;
        }

        // Split server name and port from the first argument
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

        // Read the file path from the second argument
        String filePath = args[1];

        // Existing logic to interact with the server
        // Change the server name and port in the Socket constructor
        try (Socket socket = new Socket(serverName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Your existing code for sending "GET" and handling the response
            out.println("GET");
            String response = in.readLine();

            // Parse the response string into a JSON object
            JSONObject jsonObject = new JSONObject(response);

            // Accessing and displaying all fields in a formatted manner
            System.out.println("Name: " + jsonObject.getString("name"));
            System.out.println("State: " + jsonObject.getString("state"));
            System.out.println("Air Temperature: " + jsonObject.getDouble("air_temp") + "°C");
            System.out.println("Wind Speed (km/h): " + jsonObject.getDouble("wind_spd_kmh"));
            System.out.println("Wind Speed (kt): " + jsonObject.getInt("wind_spd_kt"));
            System.out.println("Wind Direction: " + jsonObject.getString("wind_dir"));
            System.out.println("Time Zone: " + jsonObject.getString("time_zone"));
            System.out.println("Humidity: " + jsonObject.getInt("rel_hum") + "%");
            System.out.println("Cloud: " + jsonObject.getString("cloud"));
            System.out.println("Longitude: " + jsonObject.getDouble("lon"));
            System.out.println("Latitude: " + jsonObject.getDouble("lat"));
            System.out.println("Dew Point: " + jsonObject.getDouble("dewpt"));
            System.out.println("Apparent Temperature: " + jsonObject.getDouble("apparent_t"));
            System.out.println("Pressure: " + jsonObject.getDouble("press"));
            System.out.println("Local Date Time (Full): " + jsonObject.getDouble("local_date_time_full"));
            System.out.println("Local Date Time: " + jsonObject.getString("local_date_time"));
            System.out.println("ID: " + jsonObject.getString("id"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
