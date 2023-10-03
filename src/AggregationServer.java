import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class AggregationServer {
    private static final LamportClock lamportClock = new LamportClock();
    private static final HashSet<String> connectedServers = new HashSet<>();
    private static final ConcurrentHashMap<String, Long> serverTimestamps = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Set<String>> serverToIds = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final int MAX_THREAD_COUNT = 10; // You can adjust this value as needed
    private static final ThreadPoolExecutor clientHandlerExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREAD_COUNT);


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

        final String[] savedPayload = {"{}"};


        String dataPath = "data1.txt";


        String tempPath = "data1_temp.txt";


        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            boolean isDataModified = false;

            synchronized (serverToIds) {
                for (Iterator<Map.Entry<String, Long>> iterator = serverTimestamps.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<String, Long> entry = iterator.next();
                    if (currentTime - entry.getValue() > 30_000) {
                        String serverIP = entry.getKey();

                        // Remove data for IDs sent by this server
                        Set<String> idsForServer = serverToIds.getOrDefault(serverIP, new HashSet<>());
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode rootNode = mapper.readTree(savedPayload[0]);
                            for (String id : idsForServer) {
                                if (rootNode.has(id)) {
                                    ((ObjectNode) rootNode).remove(id);
                                    isDataModified = true;
                                }
                            }
                            savedPayload[0] = rootNode.toString();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        connectedServers.remove(serverIP);
                        iterator.remove();
                        serverToIds.remove(serverIP);
                    }
                }
            }

            if (isDataModified) {
                try (FileWriter file = new FileWriter(dataPath)) {
                    file.write(savedPayload[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 30, TimeUnit.SECONDS);



        ObjectMapper objectMapper = new ObjectMapper(); // Object mapper for JSON parsing and generating

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (true) {
                final Socket clientSocket = serverSocket.accept();
                clientHandlerExecutor.submit(() -> {
                    try (
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                    ) {
                        lamportClock.tick();
                        String inputLine = in.readLine();
                        String serverIP = clientSocket.getInetAddress().getHostAddress();
                        serverTimestamps.put(serverIP, System.currentTimeMillis());
                        if (inputLine == null || inputLine.trim().isEmpty()) {
                            out.println("204 - No Content");
                            return;
                        }

                    if (inputLine.startsWith("GET / HTTP/1.1")) { // Handling HTTP request from a web browser
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: text/plain");
                        out.println();  // blank line between headers and content, very important for HTTP protocol
                        out.println(savedPayload[0]);
                        out.println("Clock: " + lamportClock.getTime());
                    } else if (inputLine.startsWith("GET")) { // Handling Terminal request
                        out.println(savedPayload[0]);
                        out.println("Clock: " + lamportClock.getTime());
                    } else if ("PUT".equalsIgnoreCase(inputLine)) {
                        String newDataPayload = in.readLine();

                        if (newDataPayload == null || newDataPayload.trim().isEmpty()) {
                            out.println("204 - No Content");
                            return;
                        }

                        if (!isValidJSON(newDataPayload)) {
                            out.println("500 - Internal Server Error");
                            return;
                        }

                        JsonNode newRootNode = null;
                        JsonNode existingRootNode = null;

                        try {
                            newRootNode = objectMapper.readTree(newDataPayload);
                            existingRootNode = objectMapper.readTree(savedPayload[0].isEmpty() ? "{}" : savedPayload[0]);
                        } catch (IOException e) {
                            out.println("500 - Internal Server Error");
                            return;
                        }

                        for (Iterator<String> it = newRootNode.fieldNames(); it.hasNext(); ) {
                            String id = it.next();
                            JsonNode newData = newRootNode.get(id);
                            JsonNode existingData = existingRootNode.get(id);

                            if (existingData == null ||
                                    newData.get("local_date_time_full").asLong() > existingData.get("local_date_time_full").asLong()) {
                                ((ObjectNode) existingRootNode).set(id, newData);
                            }
                        }

                        savedPayload[0] = existingRootNode.toString();

                        File mainFile = new File(dataPath);
                        boolean fileExistsBefore = mainFile.exists();

                        try (FileWriter file = new FileWriter(tempPath)) {
                            file.write(savedPayload[0]);
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
                        synchronized (serverToIds) {
                            // Track which IDs are sent by this server
                            Set<String> idsForServer = new HashSet<>();
                            for (Iterator<String> it = newRootNode.fieldNames(); it.hasNext(); ) {
                                idsForServer.add(it.next());
                            }
                            serverToIds.put(serverIP, idsForServer);
                        }
                    } else {
                        out.println("400 - Bad Request");
                    }
                }catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
