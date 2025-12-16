import java.util.List;

public class Institution {
    private String name;
    private String address;
    private double fee;
    private double rating;
    private List<String> availableClasses;

    public Institution(String name, String address, double fee, double rating, List<String> availableClasses) {
        this.name = name;
        this.address = address;
        this.fee = fee;
        this.rating = rating;
        this.availableClasses = availableClasses;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getFee() { return fee; }
    public double getRating() { return rating; }
    public List<String> getAvailableClasses() { return availableClasses; }
}