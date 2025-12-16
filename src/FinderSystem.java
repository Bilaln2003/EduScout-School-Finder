import java.io.*; // For File I/O (Inquiries)
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class FinderSystem {

    // --- Core Data Structures ---
    private List<Institution> institutions = new ArrayList<>();
    private HashMap<String, Institution> schoolMap = new HashMap<>();
    private List<Institution> currentVisibleList = new ArrayList<>();

    // --- NEW DSA Structures ---
    // 1. Multi-Level HashMap for Area Search (O(1) lookup)
    private HashMap<String, ArrayList<Institution>> areaMap = new HashMap<>();
    // 2. Stack for Undo functionality (LIFO)
    private Stack<List<Institution>> historyStack = new Stack<>();
    // 3. Queue for Inquiry management (FIFO)
    private Queue<String> inquiryQueue = new LinkedList<>();

    // File path for permanent inquiry storage
    // Save to the User's Documents folder so Windows doesn't block it
    private final String INQUIRY_FILE = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "EduScout_Inquiries.txt";

    public FinderSystem() {
        loadDataFromCSV();
        loadInquiriesFromFile(); // Restore queue from file on startup
    }

    // --- Data Loading ---

    public void loadDataFromCSV() {
        institutions.clear();
        schoolMap.clear();
        areaMap.clear();

        // YOUR SPECIFIC LIVE GIST URL
        String onlineUrl = "https://gist.githubusercontent.com/Bilaln2003/66783c4d755fed579c7e1b07cf879a85/raw/58310477a5e9b90328f4019b137372ba0a4da5dd/Schools.csv";
        boolean loadedOnline = false;

        try {
            URL url = new URL(onlineUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            parseData(br);
            loadedOnline = true;
            System.out.println("SUCCESS: Loaded live data from cloud.");
        } catch (Exception e) {
            System.out.println("WARNING: Could not load online data. Loading local backup.");
        }

        if (!loadedOnline) {
            try (BufferedReader br = new BufferedReader(new FileReader("Schools.csv"))) {
                parseData(br);
                System.out.println("SUCCESS: Loaded local backup data.");
            } catch (Exception e) {
                System.err.println("CRITICAL: Failed to load data from both cloud and local CSV.");
            }
        }
        currentVisibleList = new ArrayList<>(institutions);
    }

    private void parseData(BufferedReader br) throws Exception {
        String line;
        boolean isHeader = true;
        while ((line = br.readLine()) != null) {
            if (isHeader) { isHeader = false; continue; }
            String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (values.length < 5) continue;

            String name = values[0].trim();
            String address = values[1].trim();
            double fee = Double.parseDouble(values[2].trim());
            double rating = Double.parseDouble(values[3].trim());
            String levelStr = values[4].replace("\"", "").trim();
            List<String> levels = new ArrayList<>(Arrays.asList(levelStr.split(",")));
            levels.replaceAll(String::trim);

            Institution institution = new Institution(name, address, fee, rating, levels);

            institutions.add(institution);
            schoolMap.put(name.toLowerCase(), institution);

            // Build Area Map
            String area = extractAreaFromAddress(address);
            if (area != null) {
                areaMap.computeIfAbsent(area, k -> new ArrayList<>()).add(institution);
            }
        }
    }

    private String extractAreaFromAddress(String address) {
        String lowerAddress = address.toLowerCase();
        if (lowerAddress.contains("gulshan")) return "Gulshan";
        if (lowerAddress.contains("clifton")) return "Clifton";
        if (lowerAddress.contains("defense") || lowerAddress.contains("dha")) return "Defense";
        if (lowerAddress.contains("north")) return "North Nazimabad";
        if (lowerAddress.contains("jauhar")) return "Gulistan-e-Jauhar";
        return "Other";
    }

    // --- Search & Retrieval ---

    public List<Institution> getAllInstitutions() {
        return institutions;
    }

    public Set<String> getAreas() {
        return areaMap.keySet();
    }

    public Institution getSchoolByName(String name) {
        if (name == null) return null;
        return schoolMap.get(name.toLowerCase());
    }

    // Named 'search' to match MainFX call
    public List<Institution> search(String nameQuery, double minFee, double maxFee) {
        if (!currentVisibleList.isEmpty()) {
            historyStack.push(new ArrayList<>(currentVisibleList));
        }

        List<Institution> filteredList = institutions.stream()
                .filter(institution -> {
                    boolean feeMatch = institution.getFee() >= minFee && institution.getFee() <= maxFee;
                    boolean nameMatch = true;
                    if (nameQuery != null && !nameQuery.trim().isEmpty()) {
                        nameMatch = institution.getName().toLowerCase().contains(nameQuery.trim().toLowerCase());
                    }
                    return feeMatch && nameMatch;
                })
                .collect(Collectors.toList());

        currentVisibleList = filteredList;
        return filteredList;
    }

    // --- Advanced DSA Methods ---

    public List<Institution> findSchoolsByArea(String area) {
        if (!currentVisibleList.isEmpty()) {
            historyStack.push(new ArrayList<>(currentVisibleList));
        }

        List<Institution> results = areaMap.getOrDefault(area, new ArrayList<>());
        currentVisibleList = results;
        return results;
    }

    public List<Institution> undoLastAction() {
        if (!historyStack.isEmpty()) {
            currentVisibleList = historyStack.pop();
            return currentVisibleList;
        }
        return currentVisibleList;
    }

    // --- Inquiry Queue (With File Persistence) ---

    public void submitInquiry(String parentName, String schoolName) {
        String request = parentName + " - Visit Request for: " + schoolName + " @ " + java.time.LocalDateTime.now();

        // 1. Add to RAM Queue
        inquiryQueue.offer(request);
        System.out.println("Inquiry submitted: " + request);

        // 2. Save to Disk (Permanent)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(INQUIRY_FILE, true))) {
            bw.write(request);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error saving inquiry: " + e.getMessage());
        }
    }

    private void loadInquiriesFromFile() {
        File file = new File(INQUIRY_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                inquiryQueue.offer(line);
            }
            System.out.println("Restored " + inquiryQueue.size() + " inquiries from file.");
        } catch (IOException e) {
            System.err.println("Error loading inquiries: " + e.getMessage());
        }
    }
}