import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFX extends Application {

    private FinderSystem fs = new FinderSystem();
    private Parent parent = null;
    private final Label statusLabel = new Label("System Ready.");

    // DSA #5: HashSet (For Unique Comparisons)
    private Set<Institution> comparisonSet = new HashSet<>();

    // UI Constants
    private final String SIDEBAR_COLOR = "-fx-background-color: #2c3e50;";
    private final String HEADER_COLOR = "-fx-background-color: #34495e;";
    private final String TEXT_COLOR_WHITE = "-fx-text-fill: white;";
    private final String[] levelOptions = {"All", "Montessori", "Primary", "Secondary", "O-Levels"};

    @Override
    public void start(Stage primaryStage) {
        fs.loadDataFromCSV();

        // --- 1. HEADER ---
        Label titleLabel = new Label("EduScout: School Finder System");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setStyle(TEXT_COLOR_WHITE);
        HBox header = new HBox(titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(HEADER_COLOR);

        // --- 2. SIDEBAR ---
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(260);
        sidebar.setStyle(SIDEBAR_COLOR);

        // Login Section
        TextField nameField = new TextField();
        nameField.setPromptText("Enter Parent Name");
        Button loginBtn = new Button("Login / Load Data");
        styleButton(loginBtn, "#e67e22");

        // Filter Section
        ComboBox<String> levelBox = new ComboBox<>(FXCollections.observableArrayList(levelOptions));
        levelBox.setValue("All");
        levelBox.setMaxWidth(Double.MAX_VALUE);
        TextField feeField = new TextField();
        feeField.setPromptText("Max Fee (PKR)");
        TextField ratingField = new TextField();
        ratingField.setPromptText("Min Rating (0-5)");

        Button searchBtn = new Button("Apply Filters");
        styleButton(searchBtn, "#2980b9");
        Button resetBtn = new Button("Show All");
        styleButton(resetBtn, "#95a5a6");
        Button viewFavBtn = new Button("My Favorites");
        styleButton(viewFavBtn, "#27ae60");

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        // Comparison Section
        Label compareLbl = new Label("Comparison Tool");
        compareLbl.setStyle(TEXT_COLOR_WHITE + "-fx-font-weight: bold;");
        Button compareBtn = new Button("Compare Selected (0)");
        styleButton(compareBtn, "#8e44ad");
        compareBtn.setDisable(true);

        // --- NEW DSA FEATURES SECTION ---
        Separator sep3 = new Separator();

        // 1. Undo Button (Stack Logic)
        Button undoBtn = new Button("Undo Last Action");
        styleButton(undoBtn, "#f39c12");

        // 2. Area Search (Multi-Level HashMap Logic)
        ComboBox<String> areaBox = new ComboBox<>(FXCollections.observableArrayList(fs.getAreas()));
        areaBox.setPromptText("Select Area for Search");
        areaBox.setMaxWidth(Double.MAX_VALUE);
        Button areaSearchBtn = new Button("Search by Area");
        styleButton(areaSearchBtn, "#1abc9c");

        // 3. Inquiry Form (Queue Logic)
        Separator sep4 = new Separator();
        Label inquiryLbl = new Label("Submit Inquiry (Queue)");
        inquiryLbl.setStyle(TEXT_COLOR_WHITE + "-fx-font-weight: bold;");
        TextField schoolNameInquiryField = new TextField();
        schoolNameInquiryField.setPromptText("School Name for Inquiry");
        Button submitInquiryBtn = new Button("Submit Visit Request");
        styleButton(submitInquiryBtn, "#c0392b");

        sidebar.getChildren().addAll(
                new Label("Parent Login"), nameField, loginBtn, sep1,
                new Label("Filters"), levelBox, feeField, ratingField, searchBtn, resetBtn, viewFavBtn, sep2,
                compareLbl, compareBtn,
                // New DSA Elements
                sep3,
                new Label("Navigation & Area Search"),
                areaBox, areaSearchBtn, undoBtn,
                sep4, inquiryLbl, schoolNameInquiryField, submitInquiryBtn
        );
        sidebar.getChildren().stream().filter(n -> n instanceof Label).forEach(n -> n.setStyle(TEXT_COLOR_WHITE));

        // --- 3. CENTER TABLE ---
        TableView<Institution> table = new TableView<>();

        // Col 1: Checkbox for Comparison
        TableColumn<Institution, Void> selectCol = new TableColumn<>("Compare");
        selectCol.setMinWidth(70);
        selectCol.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    Institution inst = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        if (comparisonSet.size() >= 3) {
                            checkBox.setSelected(false);
                            showAlert("Limit Reached", "You can only compare up to 3 schools.");
                        } else {
                            comparisonSet.add(inst);
                        }
                    } else {
                        comparisonSet.remove(inst);
                    }
                    compareBtn.setText("Compare Selected (" + comparisonSet.size() + ")");
                    compareBtn.setDisable(comparisonSet.isEmpty());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    setGraphic(checkBox);
                    checkBox.setSelected(comparisonSet.contains(getTableView().getItems().get(getIndex())));
                }
            }
        });

        TableColumn<Institution, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(180);

        TableColumn<Institution, String> feeCol = new TableColumn<>("Fee");
        feeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.format("%.0f", c.getValue().getFee())));

        TableColumn<Institution, String> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

        TableColumn<Institution, String> classCol = new TableColumn<>("Levels");
        classCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.join(", ", c.getValue().getAvailableClasses())));
        classCol.setMinWidth(200);

        TableColumn<Institution, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(getButtonCellFactory());

        table.getColumns().addAll(selectCol, nameCol, feeCol, ratingCol, classCol, actionCol);

        VBox centerLayout = new VBox(10, table, statusLabel);
        centerLayout.setPadding(new Insets(15));
        centerLayout.setVgrow(table, Priority.ALWAYS); // Table grows to fill space

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(centerLayout);

        // --- EVENT HANDLERS ---
        loginBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showAlert("Error", "Enter name."); return; }
            parent = new Parent(name);
            nameField.setDisable(true);
            loginBtn.setDisable(true);

            // Load DB Logic
            List<String> savedFavs = FavoritesManager.loadFavorites(name);
            for(String sName : savedFavs) {
                Institution i = fs.getSchoolByName(sName);
                if(i != null) parent.addFavorite(i);
            }
            statusLabel.setText("Welcome " + name + ". Loaded " + savedFavs.size() + " favorites.");
        });

        searchBtn.setOnAction(e -> {
            try {
                // Name query empty by default
                String nameQuery = "";
                double minFee = 0.0;
                double maxFee = feeField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(feeField.getText());

                // Call standard search
                table.getItems().setAll(fs.search(nameQuery, minFee, maxFee));

                comparisonSet.clear();
                compareBtn.setText("Compare Selected (" + comparisonSet.size() + ")");
                compareBtn.setDisable(true);
            } catch (Exception ex) {
                showAlert("Error", "Check your Max Fee input. Must be a number.");
            }
        });

        resetBtn.setOnAction(e -> {
            table.getItems().setAll(fs.getAllInstitutions());
            statusLabel.setText("Showing all institutions.");
        });

        viewFavBtn.setOnAction(e -> {
            if(parent == null) { showAlert("Login Required", "Login first."); return; }
            table.getItems().setAll(parent.getFavorites());
            statusLabel.setText("Showing favorites for " + parent.getName());
            comparisonSet.clear();
            compareBtn.setText("Compare Selected (0)");
            compareBtn.setDisable(true);
        });

        compareBtn.setOnAction(e -> showComparisonWindow());

        // --- NEW DSA EVENT HANDLERS ---

        // Undo Button Handler
        undoBtn.setOnAction(e -> {
            table.getItems().setAll(fs.undoLastAction());
            statusLabel.setText("Undo successful. Returned to previous state.");
        });

        // Area Search Button Handler
        areaSearchBtn.setOnAction(e -> {
            String selectedArea = areaBox.getValue();
            if (selectedArea == null || selectedArea.isEmpty()) {
                showAlert("Selection Required", "Please select an area.");
                return;
            }
            table.getItems().setAll(fs.findSchoolsByArea(selectedArea));
            statusLabel.setText("Showing schools in: " + selectedArea);
        });

        // Inquiry Button Handler
        submitInquiryBtn.setOnAction(e -> {
            if (parent == null) {
                showAlert("Login Required", "Login first to submit an inquiry.");
                return;
            }
            String schoolName = schoolNameInquiryField.getText().trim();
            if (schoolName.isEmpty()) {
                showAlert("Input Required", "Enter a school name.");
                return;
            }
            fs.submitInquiry(parent.getName(), schoolName);
            schoolNameInquiryField.clear();
            statusLabel.setText("Visit Request submitted for " + schoolName + ". Saved to disk.");
        });

        Scene scene = new Scene(root, 1150, 700);
        primaryStage.setTitle("EduScout - Advanced School Finder");
        primaryStage.setScene(scene);
        primaryStage.show();

        table.getItems().setAll(fs.getAllInstitutions());
    }

    private void showComparisonWindow() {
        Stage stage = new Stage();
        stage.setTitle("Comparison Analysis");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15); grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: #ecf0f1;");

        String[] headers = {"Attribute", "School Name", "Fee (PKR)", "Rating", "Location", "Levels"};
        for(int i=0; i<headers.length; i++) addHeaderLabel(grid, headers[i], 0, i);

        int col = 1;
        for (Institution inst : comparisonSet) {
            grid.add(new Label(inst.getName()), col, 1);

            Label feeLbl = new Label(String.format("%.0f", inst.getFee()));
            if (inst.getFee() > 20000) feeLbl.setTextFill(javafx.scene.paint.Color.RED);
            else feeLbl.setTextFill(javafx.scene.paint.Color.GREEN);
            grid.add(feeLbl, col, 2);

            grid.add(new Label(String.valueOf(inst.getRating())), col, 3);
            grid.add(new Label(inst.getAddress()), col, 4);

            Label lvlLbl = new Label(String.join("\n", inst.getAvailableClasses()));
            lvlLbl.setWrapText(true); lvlLbl.setMaxWidth(150);
            grid.add(lvlLbl, col, 5);
            col++;
        }

        ScrollPane scroll = new ScrollPane(grid);
        stage.setScene(new Scene(scroll, 700, 450));
        stage.show();
    }

    private void addHeaderLabel(GridPane grid, String text, int col, int row) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        grid.add(lbl, col, row);
    }

    private void styleButton(Button btn, String colorHex) {
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(javafx.scene.Cursor.HAND);
    }

    private Callback<TableColumn<Institution, Void>, TableCell<Institution, Void>> getButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Institution, Void> call(final TableColumn<Institution, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Add Fav");
                    {
                        btn.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7;");
                        btn.setOnAction(event -> {
                            if (parent != null) {
                                Institution inst = getTableView().getItems().get(getIndex());
                                parent.addFavorite(inst);
                                FavoritesManager.saveFavorite(parent.getName(), inst.getName());
                                btn.setText("Saved ❤");
                                btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                            } else showAlert("Login Required", "Login first.");
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null); else setGraphic(btn);
                    }
                };
            }
        };
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}