import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FinderSystem fs = new FinderSystem();
        fs.loadDataFromCSV();

        System.out.println("Welcome to School Finder!");
        System.out.print("Enter parent name: ");
        Parent parent = new Parent(sc.nextLine());

        while(true) {
            System.out.println("\n1. Find Schools (By Class)");
            System.out.println("2. View Favorites");
            System.out.println("3. Exit");
            int choice = sc.nextInt();
            sc.nextLine();

            if(choice == 1) {
                System.out.print("Enter Class (e.g., Class 5, O-Levels): ");
                String cls = sc.nextLine();
                System.out.print("Max Monthly Fee: ");
                double fee = sc.nextDouble();
                System.out.print("Min Rating: ");
                double rating = sc.nextDouble();
                sc.nextLine();

                fs.search(cls, fee, rating);
                // (Add favorite logic same as before if needed)
            } else if(choice == 2) {
                parent.viewFavorites();
            } else {
                break;
            }
        }
        sc.close();
    }
}