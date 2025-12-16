import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String DB_FILE = "FavoritesDB.txt";

    // Save Favorite to File
    public static void saveFavorite(String parentName, String schoolName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DB_FILE, true))) {
            bw.write(parentName + "," + schoolName);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load Favorites from File
    public static List<String> loadFavorites(String parentName) {
        List<String> schoolNames = new ArrayList<>();
        File file = new File(DB_FILE);
        if (!file.exists()) return schoolNames;

        try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    if (parts[0].trim().equalsIgnoreCase(parentName)) {
                        schoolNames.add(parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return schoolNames;
    }
}