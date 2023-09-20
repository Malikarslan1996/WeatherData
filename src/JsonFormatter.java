import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JsonFormatter {

    public static JSONObject format(String filename) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            JSONObject json = new JSONObject();

            for (String line : lines) {
                String[] keyValue = line.split(":");
                if (keyValue.length < 2) {
                    System.out.println("Warning: Skipping invalid line - " + line);
                    continue;
                }
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if (isNumeric(value)) {
                    json.put(key, Double.parseDouble(value));
                } else {
                    json.put(key, value);
                }
            }
            return json;
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
