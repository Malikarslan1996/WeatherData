import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JsonFormatter {

    public static JsonNode format(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            ObjectNode currentObject = null;

            for (String line : lines) {
                String[] keyValue = line.split(":");
                if (keyValue.length < 2) {
                    System.out.println("Warning: Skipping invalid line - " + line);
                    continue;
                }
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if ("id".equalsIgnoreCase(key)) {
                    currentObject = mapper.createObjectNode();
                    root.set(value, currentObject);
                } else if (currentObject != null) {
                    if (isNumeric(value)) {
                        currentObject.put(key, Double.parseDouble(value));
                    } else {
                        currentObject.put(key, value);
                    }
                }
            }
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
