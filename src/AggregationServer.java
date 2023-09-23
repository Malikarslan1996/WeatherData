import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.*;

public class AggregationServer {
    private static final LamportClock lamportClock = new LamportClock();
    private static final HashSet<String> connectedServers = new HashSet<>();
    private static final ConcurrentHashMap<String, Long> serverTimestamps = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static boolean isValidJSON(String test) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(test);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 4567;

        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number provided. Using default port: " + port);
            }
        }

        String savedPayload = "";


        String dataPath = "src/data1.txt";


        String tempPath = "src/data1_temp.txt";


        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            serverTimestamps.forEach((serverIP, lastTimestamp) -> {
                if (currentTime - lastTimestamp > 30_000) {
                    connectedServers.remove(serverIP);
                    serverTimestamps.remove(serverIP);

                }
            });
        }, 0, 30, TimeUnit.SECONDS);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {


                    lamportClock.tick();

                    String inputLine = in.readLine();


                    String serverIP = clientSocket.getInetAddress().getHostAddress();
                    serverTimestamps.put(serverIP, System.currentTimeMillis());

                    if (inputLine == null || inputLine.trim().isEmpty()) {
                        out.println("204 - No Content");
                        continue;
                    }

                    if (inputLine.startsWith("GET")) {
                        out.println(savedPayload);

                        out.println("Clock: " + lamportClock.getTime());
                    } else if ("PUT".equalsIgnoreCase(inputLine)) {
                        savedPayload = in.readLine();

                        if (savedPayload == null || savedPayload.trim().isEmpty()) {
                            out.println("204 - No Content");
                            continue;
                        }

                        if (!isValidJSON(savedPayload)) {
                            out.println("500 - Internal Server Error");
                            continue;
                        }

                        File mainFile = new File(dataPath);
                        boolean fileExistsBefore = mainFile.exists();


                        try (FileWriter file = new FileWriter(tempPath)) {
                            file.write(savedPayload);
                        }

                        File tempFile = new File(tempPath);
                        tempFile.renameTo(mainFile);


                        if (!fileExistsBefore && !connectedServers.contains(serverIP)) {
                            out.println("201 - HTTP_CREATED");
                            connectedServers.add(serverIP);
                        } else {
                            out.println("200 - OK");
                        }


                        out.println("Clock: " + lamportClock.getTime());
                    } else {
                        out.println("400 - Bad Request");
                    }
                }
            }
        }
    }
}
