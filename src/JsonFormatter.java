import gvjava.org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JsonFormatter {

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("src/data.txt"));
            JSONObject json = new JSONObject();

            for (String line : lines) {
                String[] keyValue = line.split(":");

                if (keyValue.length < 2) {  // <-- ADD THIS VALIDATION CHECK
                    System.out.println("Warning: Skipping invalid line - " + line);
                    continue; // skip to the next iteration
                }

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if (isNumeric(value)) {
                    json.put(key, Double.parseDouble(value));
                } else {
                    json.put(key, value);
                }
            }

            System.out.println(json.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
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
