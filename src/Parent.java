import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parent {
    private String name;

    // DSA #1: ArrayList for Favorites
    private ArrayList<Institution> favorites;

    // DSA #2: Stack for History (Undo/History feature)
    private Stack<Institution> viewHistory;

    public Parent(String name) {
        this.name = name;
        this.favorites = new ArrayList<>();
        this.viewHistory = new Stack<>();
    }

    public void addFavorite(Institution inst) {
        if (!favorites.contains(inst)) {
            favorites.add(inst);
        }
    }

    // --- THIS METHOD WAS MISSING ---
    public void viewFavorites() {
        if (favorites.isEmpty()) {
            System.out.println(name + "'s Favorites: No schools added yet.");
        } else {
            System.out.println("--- " + name + "'s Favorites ---");
            for (Institution i : favorites) {
                System.out.println(i.getName() + " | Fee: " + i.getFee() + " | Rating: " + i.getRating());
            }
        }
    }
    // -------------------------------

    // Add to history (LIFO)
    public void addToHistory(Institution inst) {
        viewHistory.push(inst);
    }

    public List<Institution> getFavorites() {
        return favorites;
    }

    public String getName() {
        return name;
    }
}