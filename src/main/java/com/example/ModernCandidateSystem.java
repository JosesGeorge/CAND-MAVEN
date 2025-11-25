package com.example;

import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 2025 ULTIMATE CANDIDATE EVALUATION SYSTEM
 * Features: Fixed Email Input, No Scrolling, Stable UI
 */
public class ModernCandidateSystem extends Application {

    // --- Data Layer ---
    private ObservableList<Candidate> candidateList = FXCollections.observableArrayList();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private final String DATA_FILE = "candidates.csv";
    private final String USERS_FILE = "users.csv";
    
    // --- State ---
    private Stage primaryStage;
    private StackPane rootStack;
    private BorderPane mainLayout;
    private StackPane centerContainer;
    private boolean isLightMode = false;
    private User currentUser;

    // --- Views ---
    private VBox dashboardView; // Changed from ScrollPane to VBox
    private VBox addCandidateView;
    private HBox databaseView;
    private VBox settingsView;
    private VBox profileView;
    private StackPane authContainer;
    
    // Dashboard Components
    private Label lblTotal, lblHired, lblRate, lblPending;
    private PieChart chartStatus;
    private BarChart<String, Number> chartRoles;
    private AreaChart<String, Number> chartTrend;
    private TableView<Candidate> tableTopPerformers;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        loadData();
        loadUsers();

        rootStack = new StackPane();
        rootStack.getStyleClass().add("root-stack");
        addLiquidBackground(rootStack);

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("glass-panel");
        mainLayout.setMaxSize(1150, 800);
        mainLayout.setVisible(false);

        authContainer = createAuthView();

        centerContainer = new StackPane();
        centerContainer.setPadding(new Insets(0, 0, 0, 20));
        mainLayout.setCenter(centerContainer);

        // Init Views
        dashboardView = createDashboard();
        addCandidateView = createAddCandidateView();
        databaseView = createDatabaseView();
        settingsView = createSettingsView();
        profileView = createProfileView();

        mainLayout.setLeft(createSidebar());
        switchView(dashboardView);

        StackPane wrapper = new StackPane(mainLayout);
        wrapper.setPadding(new Insets(30));
        
        rootStack.getChildren().addAll(wrapper, authContainer);

        Scene scene = new Scene(rootStack, 1280, 850);
        String cssPath = getClass().getResource("/glass-theme.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setTitle("Candidate Evaluation System 2025");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> saveData());
        primaryStage.show();
    }

    // ... [BACKGROUND, AUTH, SIDEBAR Methods Unchanged] ...
    private void addLiquidBackground(Pane root) {
        root.getChildren().addAll(
            createBlob(Color.web("#3b82f6", 0.3), 200, -450, -300, 15),
            createBlob(Color.web("#8b5cf6", 0.25), 250, 450, 350, 20),
            createBlob(Color.web("#10b981", 0.2), 180, -350, 250, 18),
            createBlob(Color.web("#f59e0b", 0.15), 220, 350, -250, 22)
        );
    }
    private Circle createBlob(Color c, double r, double x, double y, double d) { Circle b = new Circle(r, c); b.setEffect(new GaussianBlur(80)); b.setTranslateX(x); b.setTranslateY(y); Timeline t = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(b.translateXProperty(), x), new KeyValue(b.translateYProperty(), y)), new KeyFrame(Duration.seconds(d/2), new KeyValue(b.translateXProperty(), x+40), new KeyValue(b.translateYProperty(), y-40)), new KeyFrame(Duration.seconds(d), new KeyValue(b.translateXProperty(), x), new KeyValue(b.translateYProperty(), y))); t.setAutoReverse(true); t.setCycleCount(Animation.INDEFINITE); t.play(); return b; }
    
    private StackPane createAuthView() {
        StackPane authPane = new StackPane(); authPane.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95);");
        VBox loginCard = new VBox(20); styleAuthCard(loginCard);
        Text loginTitle = new Text("System Access"); loginTitle.setStyle("-fx-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        TextField txtUser = new TextField(); txtUser.setPromptText("Username");
        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Password"); styleAuthField(txtPass);
        GlassButton btnLogin = new GlassButton("Login", Color.web("#3b82f6")); btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> { Optional<User> valid = userList.stream().filter(u -> u.username.equals(txtUser.getText()) && u.password.equals(txtPass.getText())).findFirst(); if (valid.isPresent() || (txtUser.getText().equals("admin") && txtPass.getText().equals("admin"))) { currentUser = valid.orElse(new User("Administrator", "admin", "System Admin")); unlockApp(authPane); } else { shake(loginCard); } });
        loginCard.getChildren().addAll(loginTitle, txtUser, txtPass, btnLogin); authPane.getChildren().add(loginCard); return authPane;
    }
    private void styleAuthCard(VBox card) { card.setMaxSize(400, 300); card.setAlignment(Pos.CENTER); card.getStyleClass().add("glass-panel"); card.setPadding(new Insets(40)); }
    private void styleAuthField(PasswordField pf) { pf.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12;"); }
    private void unlockApp(Pane authPane) { updateProfileView(); FadeTransition ft = new FadeTransition(Duration.millis(500), authPane); ft.setFromValue(1.0); ft.setToValue(0.0); ft.setOnFinished(ev -> { authPane.setVisible(false); mainLayout.setVisible(true); refreshStats(); }); ft.play(); }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(20); sidebar.setPadding(new Insets(30)); sidebar.setPrefWidth(260); sidebar.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 1 0 0;");
        Text title = new Text("Candidate Evaluation\nSystem"); title.getStyleClass().add("header-text"); title.setStyle("-fx-font-size: 20px; -fx-text-alignment: left; -fx-fill: white; -fx-font-weight: bold;");
        GlassButton btnDash = new GlassButton("Dashboard", true); btnDash.setOnAction(e -> { setActive(btnDash); switchView(dashboardView); });
        GlassButton btnAdd = new GlassButton("New Candidate", false); btnAdd.setOnAction(e -> { setActive(btnAdd); switchView(addCandidateView); });
        GlassButton btnDb = new GlassButton("Database", false); btnDb.setOnAction(e -> { setActive(btnDb); switchView(databaseView); });
        GlassButton btnSet = new GlassButton("Settings", false); btnSet.setOnAction(e -> { setActive(btnSet); switchView(settingsView); });
        GlassButton btnProfile = new GlassButton("My Profile", false); btnProfile.setOnAction(e -> { setActive(btnProfile); switchView(profileView); });
        VBox nav = new VBox(15); nav.getChildren().addAll(btnDash, btnAdd, btnDb, btnSet); Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(title, nav, spacer, btnProfile); return sidebar;
    }

    private void switchView(Node newView) {
        if (centerContainer.getChildren().isEmpty()) { centerContainer.getChildren().add(newView); return; }
        Node currentView = centerContainer.getChildren().get(0); if (currentView == newView) return;
        newView.setOpacity(0); newView.setTranslateX(50); centerContainer.getChildren().add(newView);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentView); fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), currentView); slideOut.setByX(-50);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newView); fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0); fadeIn.setDelay(Duration.millis(50));
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), newView); slideIn.setToX(0); slideIn.setInterpolator(Interpolator.EASE_OUT); slideIn.setDelay(Duration.millis(50));
        ParallelTransition pt = new ParallelTransition(fadeOut, slideOut, fadeIn, slideIn);
        pt.setOnFinished(e -> { centerContainer.getChildren().remove(currentView); currentView.setTranslateX(0); }); pt.play(); refreshStats();
    }

    // --- ADD CANDIDATE VIEW (Updated with Gmail Fix) ---
    private VBox createAddCandidateView() {
        VBox container = new VBox(20); container.setPadding(new Insets(40)); container.setAlignment(Pos.TOP_LEFT);
        Text header = new Text("New Candidate Evaluation"); header.getStyleClass().add("header-text");

        StackPane dropZone = new StackPane();
        dropZone.setPrefSize(100, 100); dropZone.setMaxSize(100, 100);
        dropZone.setStyle("-fx-border-color: rgba(255,255,255,0.3); -fx-border-style: dashed; -fx-border-radius: 50; -fx-border-width: 2;");
        Label lblDrop = new Label("Photo"); lblDrop.setStyle("-fx-text-fill: grey;");
        ImageView imgPreview = new ImageView(); imgPreview.setFitWidth(100); imgPreview.setFitHeight(100);
        Circle clip = new Circle(50, 50, 50); imgPreview.setClip(clip);
        dropZone.getChildren().addAll(lblDrop, imgPreview);
        final String[] droppedImgPath = {null};
        dropZone.setOnDragOver(e -> { if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY); e.consume(); });
        dropZone.setOnDragDropped(e -> { Dragboard db = e.getDragboard(); if (db.hasFiles()) { droppedImgPath[0] = db.getFiles().get(0).toURI().toString(); imgPreview.setImage(new Image(droppedImgPath[0])); lblDrop.setVisible(false); } });

        TextField txtName = new TextField(); txtName.setPromptText("Full Candidate Name");

        // --- NEW: EMAIL INPUT GROUP (@gmail.com built-in) ---
        HBox emailGroup = new HBox();
        emailGroup.setAlignment(Pos.CENTER_LEFT);
        emailGroup.getStyleClass().add("input-group"); // Uses the CSS class we defined
        
        TextField txtEmailUser = new TextField();
        txtEmailUser.setPromptText("username");
        txtEmailUser.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-padding: 10 5 10 10; -fx-text-fill: white;");
        HBox.setHgrow(txtEmailUser, Priority.ALWAYS);
        
        Label lblSuffix = new Label("@gmail.com");
        lblSuffix.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14px; -fx-padding: 0 15 0 0;");
        
        emailGroup.getChildren().addAll(txtEmailUser, lblSuffix);
        // -----------------------------------------------------
        
        ComboBox<String> cmbRole = new ComboBox<>();
        cmbRole.getItems().addAll("Software Engineer", "QA Analyst", "Product Manager", "UI/UX Designer", "System Admin");
        cmbRole.setPromptText("Select Job Role"); cmbRole.setMaxWidth(Double.MAX_VALUE);

        GridPane scores = new GridPane(); scores.setHgap(20);
        TextField t1 = new TextField(); t1.setPromptText("Tech Score (0-100)");
        TextField t2 = new TextField(); t2.setPromptText("Comm Score (0-100)");
        TextField t3 = new TextField(); t3.setPromptText("Experience (Years)");
        scores.addRow(0, t1, t2, t3);
        ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(33); scores.getColumnConstraints().addAll(cc, cc, cc);

        GlassButton btnSave = new GlassButton("Calculate & Save Result", Color.web("#10b981"));
        btnSave.setMaxWidth(Double.MAX_VALUE);
        
        btnSave.setOnAction(e -> {
            try {
                String n = txtName.getText(); 
                String userPart = txtEmailUser.getText().trim();
                String m = userPart.isEmpty() ? "" : userPart + "@gmail.com";
                String r = cmbRole.getValue();
                
                if(n.isEmpty() || m.isEmpty() || r == null) throw new Exception("Empty");
                
                int tech = Integer.parseInt(t1.getText());
                int comm = Integer.parseInt(t2.getText());
                int exp = Integer.parseInt(t3.getText());
                double avg = (tech + comm) / 2.0;
                String status = (avg >= 85 && exp >= 2) ? "HIRED" : (avg >= 70 ? "SHORTLISTED" : (avg >= 50 && exp > 5 ? "ON HOLD" : "REJECTED"));
                
                candidateList.add(new Candidate(n, m, r, tech, comm, exp, status, avg, droppedImgPath[0]));
                
                txtName.clear(); txtEmailUser.clear(); t1.clear(); t2.clear(); t3.clear();
                cmbRole.getSelectionModel().clearSelection(); cmbRole.setButtonCell(null);
                imgPreview.setImage(null); lblDrop.setVisible(true); droppedImgPath[0] = null;
                
                btnSave.setText("Saved: " + status);
                PauseTransition pt = new PauseTransition(Duration.seconds(2));
                pt.setOnFinished(ev -> btnSave.setText("Calculate & Save Result")); pt.play();
            } catch(Exception ex) { shake(btnSave); }
        });

        container.getChildren().addAll(header, dropZone, txtName, emailGroup, cmbRole, scores, btnSave);
        return container;
    }

    // --- DASHBOARD (NO SCROLLING) ---
    private VBox createDashboard() {
        VBox content = new VBox(25); 
        content.setPadding(new Insets(30));
        
        HBox headerBox = new HBox(15);
        Text header = new Text("Executive Overview"); header.getStyleClass().add("header-text");
        Text date = new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))); date.setStyle("-fx-fill: rgba(255,255,255,0.5); -fx-font-size: 14px;");
        headerBox.getChildren().add(new VBox(5, header, date));

        HBox kpiRow = new HBox(20); kpiRow.setAlignment(Pos.CENTER_LEFT);
        lblTotal = new Label("0"); lblHired = new Label("0"); lblRate = new Label("0%"); lblPending = new Label("0");
        kpiRow.getChildren().addAll(
            createGradientCard("Total Candidates", lblTotal, "#2563eb", "#1e40af"),
            createGradientCard("Positions Filled", lblHired, "#059669", "#047857"),
            createGradientCard("Success Rate", lblRate, "#7c3aed", "#6d28d9"),
            createGradientCard("Waitlist", lblPending, "#d97706", "#b45309")
        );

        GridPane featureGrid = new GridPane(); featureGrid.setHgap(20); featureGrid.setVgap(20);
        CategoryAxis xTrend = new CategoryAxis(); NumberAxis yTrend = new NumberAxis();
        chartTrend = new AreaChart<>(xTrend, yTrend); chartTrend.setTitle("Talent Quality Trend"); chartTrend.setLegendVisible(false);
        chartStatus = new PieChart(); chartStatus.setTitle("Pipeline"); chartStatus.setLegendVisible(false);
        featureGrid.add(wrapChart("Performance Analytics", chartTrend), 0, 0); featureGrid.add(wrapChart("Pipeline Status", chartStatus), 1, 0);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(66); ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(34); featureGrid.getColumnConstraints().addAll(c1, c2);

        GridPane bottomGrid = new GridPane(); bottomGrid.setHgap(20); bottomGrid.setVgap(20);
        CategoryAxis xRole = new CategoryAxis(); NumberAxis yRole = new NumberAxis();
        chartRoles = new BarChart<>(xRole, yRole); chartRoles.setTitle("Role Distribution"); chartRoles.setLegendVisible(false);
        
        TableColumn<Candidate, String> colName = new TableColumn<>("Name"); colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Candidate, String> colRole = new TableColumn<>("Role"); colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        TableColumn<Candidate, Double> colScore = new TableColumn<>("Score"); colScore.setCellValueFactory(new PropertyValueFactory<>("avg"));
        tableTopPerformers = new TableView<>(); tableTopPerformers.getColumns().addAll(colName, colRole, colScore); tableTopPerformers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        StackPane pTable = wrapChart("Top Talent", tableTopPerformers);
        
        bottomGrid.add(wrapChart("Roles Breakdown", chartRoles), 0, 0); bottomGrid.add(pTable, 1, 0);
        ColumnConstraints b1 = new ColumnConstraints(); b1.setPercentWidth(34); ColumnConstraints b2 = new ColumnConstraints(); b2.setPercentWidth(66); bottomGrid.getColumnConstraints().addAll(b1, b2);

        content.getChildren().addAll(headerBox, kpiRow, featureGrid, bottomGrid);
        return content; // Directly returning VBox, no ScrollPane
    }

    // ... [DB, SETTINGS, PROFILE, HELPERS - Unchanged] ...
    private HBox createDatabaseView() {
        HBox splitView = new HBox(25); splitView.setPadding(new Insets(30));
        VBox leftPane = new VBox(15); HBox.setHgrow(leftPane, Priority.ALWAYS); leftPane.setMaxWidth(350);
        Text header = new Text("Database"); header.getStyleClass().add("header-text");
        TextField search = new TextField(); search.setPromptText("Search...");
        ListView<Candidate> list = new ListView<>(); list.setStyle("-fx-background-color: transparent;"); list.setCellFactory(p -> new CandidateCell());
        FilteredList<Candidate> filtered = new FilteredList<>(candidateList, p->true);
        search.textProperty().addListener((o, old, val) -> filtered.setPredicate(c -> val.isEmpty() || c.name.toLowerCase().contains(val.toLowerCase())));
        list.setItems(filtered); VBox.setVgrow(list, Priority.ALWAYS);
        leftPane.getChildren().addAll(header, search, list);
        VBox detailPane = new VBox(0); detailPane.getStyleClass().add("glass-panel"); detailPane.setPadding(new Insets(30)); HBox.setHgrow(detailPane, Priority.ALWAYS);
        StackPane detailContainer = new StackPane();
        Text emptyText = new Text("Select a candidate"); emptyText.setStyle("-fx-fill: rgba(255,255,255,0.3); -fx-font-size: 16px;");
        detailContainer.getChildren().add(emptyText);
        detailPane.getChildren().add(detailContainer); VBox.setVgrow(detailContainer, Priority.ALWAYS);
        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> { if(newVal != null) updateDetailPane(detailContainer, newVal); });
        splitView.getChildren().addAll(leftPane, detailPane); return splitView;
    }
    private void updateDetailPane(StackPane container, Candidate c) {
        container.getChildren().clear(); BorderPane layout = new BorderPane();
        HBox headerBox = new HBox(20); headerBox.setAlignment(Pos.CENTER_LEFT); headerBox.setPadding(new Insets(0, 0, 20, 0)); headerBox.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;");
        ImageView iv = new ImageView(); if(c.imgPath != null) try { iv.setImage(new Image(c.imgPath)); } catch(Exception e) {} iv.setFitWidth(80); iv.setFitHeight(80); Circle clip = new Circle(40, 40, 40); iv.setClip(clip);
        VBox info = new VBox(5); Text name = new Text(c.name); name.setStyle("-fx-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        Text role = new Text(c.role); role.setStyle("-fx-fill: #3b82f6; -fx-font-size: 16px;");
        String statusColor = c.status.equals("HIRED") ? "#10b981" : (c.status.equals("REJECTED") ? "#ef4444" : "#f59e0b");
        Label badge = new Label(c.status); badge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
        info.getChildren().addAll(name, role, badge); headerBox.getChildren().addAll(iv, info); layout.setTop(headerBox);
        HBox centerSplit = new HBox(40); centerSplit.setAlignment(Pos.CENTER); centerSplit.setPadding(new Insets(20, 0, 20, 0));
        Canvas radar = new RadarChartCanvas(c.tech, c.comm, c.exp * 10);
        GridPane statsGrid = new GridPane(); statsGrid.setHgap(15); statsGrid.setVgap(20); statsGrid.setAlignment(Pos.CENTER_LEFT);
        addStatRow(statsGrid, 0, "Technical", c.tech, "#3b82f6"); addStatRow(statsGrid, 1, "Communication", c.comm, "#8b5cf6"); addStatRow(statsGrid, 2, "Experience", (int)(c.exp*10), "#f59e0b");
        centerSplit.getChildren().addAll(new VBox(10, radar), new Separator(javafx.geometry.Orientation.VERTICAL), statsGrid); layout.setCenter(centerSplit);
        HBox actions = new HBox(15); actions.setAlignment(Pos.CENTER_RIGHT); actions.setPadding(new Insets(20, 0, 0, 0)); actions.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1 0 0 0;");
        if(c.status.equals("HIRED")) { GlassButton btnOffer = new GlassButton("Generate Offer", Color.web("#10b981")); btnOffer.setOnAction(e -> generateHTMLReport(c)); actions.getChildren().add(btnOffer); }
        layout.setBottom(actions); container.getChildren().add(layout); FadeTransition ft = new FadeTransition(Duration.millis(400), layout); ft.setFromValue(0); ft.setToValue(1); ft.play();
    }
    private void addStatRow(GridPane grid, int row, String label, int val, String color) { Text l = new Text(label); l.setStyle("-fx-fill: rgba(255,255,255,0.6); -fx-font-size: 14px;"); ProgressBar pb = new ProgressBar(val / 100.0); pb.setStyle("-fx-accent: " + color + ";"); pb.setPrefWidth(200); Text v = new Text(val + "%"); v.setStyle("-fx-fill: white; -fx-font-weight: bold;"); grid.add(l, 0, row); grid.add(pb, 1, row); grid.add(v, 2, row); }
    private StackPane createGradientCard(String t, Label v, String c1, String c2) { StackPane card = new StackPane(); card.setPadding(new Insets(20)); card.setMinWidth(200); card.setStyle("-fx-background-color: linear-gradient(to bottom right, " + c1 + ", " + c2 + "); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);"); VBox content = new VBox(5); Text title = new Text(t.toUpperCase()); title.setStyle("-fx-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-font-weight: bold;"); v.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;"); content.getChildren().addAll(title, v); card.getChildren().add(content); return card; }
    private StackPane wrapChart(String title, Node content) { BorderPane w = new BorderPane(); w.setStyle("-fx-background-color: rgba(30, 41, 59, 0.6); -fx-background-radius: 18; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 18;"); w.setPadding(new Insets(15)); Text t = new Text(title); t.setStyle("-fx-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"); w.setTop(t); BorderPane.setMargin(t, new Insets(0, 0, 10, 5)); w.setCenter(content); w.setMinHeight(300); return new StackPane(w); }
    private VBox createSettingsView() { VBox c = new VBox(20); c.setAlignment(Pos.CENTER); c.getChildren().add(new Text("Settings")); return c; }
    private VBox createProfileView() { VBox c = new VBox(20); c.setAlignment(Pos.CENTER); return c; }
    private void updateProfileView() { profileView.getChildren().clear(); Text t = new Text("User: " + (currentUser!=null?currentUser.username:"")); t.setStyle("-fx-fill: white; -fx-font-size: 24px;"); profileView.getChildren().add(t); }
    private void setActive(GlassButton b) { ((VBox)b.getParent()).getChildren().forEach(n->{if(n instanceof GlassButton)((GlassButton)n).setActive(false);}); b.setActive(true); }
    private void shake(Node n) { TranslateTransition t = new TranslateTransition(Duration.millis(50), n); t.setByX(10); t.setAutoReverse(true); t.setCycleCount(4); t.play(); }
    private void generateHTMLReport(Candidate c) { try { File f = new File("Offer_" + c.name.replaceAll(" ", "_") + ".html"); PrintWriter pw = new PrintWriter(f); pw.println("<html><body style='font-family: sans-serif; padding: 40px;'><h1>OFFICIAL JOB OFFER</h1><hr><h3>Dear " + c.name + ",</h3><p>We are pleased to offer you the position of <b>" + c.role + "</b>.</p><br><p>HR Team</p></body></html>"); pw.close(); getHostServices().showDocument(f.toURI().toString()); } catch(Exception e) { e.printStackTrace(); } }
    private void refreshStats() { int h=0, r=0, o=0; Map<String, Integer> roles = new HashMap<>(); for(Candidate c:candidateList) { if(c.status.equals("HIRED")) h++; else if(c.status.equals("REJECTED")) r++; else o++; roles.put(c.role, roles.getOrDefault(c.role,0)+1); } lblTotal.setText(candidateList.size()+""); lblHired.setText(h+""); lblRate.setText((candidateList.isEmpty() ? 0 : (h*100/candidateList.size())) + "%"); lblPending.setText(o+""); chartStatus.getData().clear(); chartStatus.getData().addAll(new PieChart.Data("Hired", h), new PieChart.Data("Rejected", r), new PieChart.Data("On Hold", o)); chartRoles.getData().clear(); XYChart.Series<String, Number> s = new XYChart.Series<>(); roles.forEach((k,v) -> s.getData().add(new XYChart.Data<>(k, v))); chartRoles.getData().add(s); chartTrend.getData().clear(); XYChart.Series<String, Number> tr = new XYChart.Series<>(); int i=1; for(Candidate c:candidateList) { if(candidateList.size()>20 && i<candidateList.size()-20){i++; continue;} tr.getData().add(new XYChart.Data<>(String.valueOf(i++), c.avg)); } chartTrend.getData().add(tr); ObservableList<Candidate> sorted = FXCollections.observableArrayList(candidateList); sorted.sort(Comparator.comparingDouble(Candidate::getAvg).reversed()); tableTopPerformers.setItems(FXCollections.observableArrayList(sorted.stream().limit(5).collect(Collectors.toList()))); }
    private void saveData() { try(PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) { for(Candidate c : candidateList) pw.println(c.toCSV()); } catch(Exception e) {} }
    private void loadData() { File f = new File(DATA_FILE); if(!f.exists()) return; try(BufferedReader br = new BufferedReader(new FileReader(f))) { String l; while((l=br.readLine())!=null) candidateList.add(Candidate.fromCSV(l)); } catch(Exception e) {} }
    private void saveUsers() { try(PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) { for(User u : userList) pw.println(u.username + "," + u.password + "," + u.role); } catch(Exception e) {} }
    private void loadUsers() { File f = new File(USERS_FILE); if(!f.exists()) return; try(BufferedReader br = new BufferedReader(new FileReader(f))) { String l; while((l=br.readLine())!=null) { String[] p = l.split(","); if(p.length >= 3) userList.add(new User(p[0], p[1], p[2])); } } catch(Exception e) {} }

    class RadarChartCanvas extends Canvas { public RadarChartCanvas(double t, double c, double e) { super(250, 250); GraphicsContext gc = getGraphicsContext2D(); double cx=125, cy=125, r=90; gc.setStroke(Color.GRAY); gc.setLineWidth(1); for(int i=0; i<3; i++) { double ang = Math.toRadians(i * 120 - 90); gc.strokeLine(cx, cy, cx + Math.cos(ang)*r, cy + Math.sin(ang)*r); } gc.strokeOval(cx-r, cy-r, r*2, r*2); double[] xPoints = { cx + Math.cos(Math.toRadians(-90)) * (t/100.0*r), cx + Math.cos(Math.toRadians(30)) * (c/100.0*r), cx + Math.cos(Math.toRadians(150)) * (e/100.0*r) }; double[] yPoints = { cy + Math.sin(Math.toRadians(-90)) * (t/100.0*r), cy + Math.sin(Math.toRadians(30)) * (c/100.0*r), cy + Math.sin(Math.toRadians(150)) * (e/100.0*r) }; gc.setFill(Color.web("#3b82f6", 0.5)); gc.fillPolygon(xPoints, yPoints, 3); gc.setFill(Color.GRAY); gc.fillText("Tech", cx-15, cy-r-10); gc.fillText("Comm", cx+r-10, cy+r/2); gc.fillText("Exp", cx-r-30, cy+r/2); } }
    static class GlassButton extends Button { private boolean isActive=false; private Color customColor=null; public GlassButton(String t, boolean a) { super(t); isActive=a; init(); } public GlassButton(String t, Color c) { super(t); customColor=c; init(); } private void init() { getStyleClass().add("glass-button"); updateStyle(); setPrefWidth(200); setStyle(getStyle() + "-fx-border-color: transparent; -fx-border-width: 0 0 0 3;"); setOnMouseEntered(e -> { if (customColor != null) setStyle("-fx-background-color: " + toHex(customColor.deriveColor(0, 1, 1.2, 1)) + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-border-width: 0;"); else if (!isActive) setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-background-radius: 12; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent; -fx-border-width: 0 0 0 3;"); setScaleX(1.02); setScaleY(1.02); }); setOnMouseExited(e -> { updateStyle(); setScaleX(1.0); setScaleY(1.0); }); setOnMousePressed(e -> { setScaleX(0.97); setScaleY(0.97); }); setOnMouseReleased(e -> { setScaleX(1.02); setScaleY(1.02); }); } public void setActive(boolean b) { isActive = b; updateStyle(); } private void updateStyle() { if (customColor != null) { String hex = toHex(customColor); setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-border-width: 0;"); } else { String fill = isActive ? "rgba(59, 130, 246, 0.4)" : "rgba(255,255,255,0.05)"; String border = isActive ? "-fx-border-color: #3b82f6;" : "-fx-border-color: transparent;"; setStyle("-fx-background-color: " + fill + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-alignment: CENTER_LEFT; " + border + " -fx-border-width: 0 0 0 3;"); } } private String toHex(Color c) { return String.format("#%02X%02X%02X", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255)); } }
    static class CandidateCell extends ListCell<Candidate> { @Override protected void updateItem(Candidate c, boolean e) { super.updateItem(c, e); if(e || c==null) { setGraphic(null); setText(null); return; } HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT); ImageView iv = new ImageView(); if(c.imgPath != null) try { iv.setImage(new Image(c.imgPath)); } catch(Exception ex) {} iv.setFitWidth(40); iv.setFitHeight(40); Circle clip = new Circle(20, 20, 20); iv.setClip(clip); VBox info = new VBox(2); Text n = new Text(c.name); n.getStyleClass().add("text-primary"); n.setStyle("-fx-font-weight: bold; -fx-fill: -text-primary;"); Text r = new Text(c.role); r.getStyleClass().add("text-secondary"); r.setStyle("-fx-fill: -text-secondary; -fx-font-size: 12px;"); info.getChildren().addAll(n, r); card.getChildren().addAll(iv, info); setGraphic(card); } }
    public static class Candidate { String name, email, role, status, imgPath; int tech, comm, exp; double avg; public Candidate(String n, String e, String rl, int t, int c, int x, String s, double a, String i) { name=n; email=e; role=rl; tech=t; comm=c; exp=x; status=s; avg=a; imgPath=i; } public String getName(){return name;} public String getRole(){return role;} public double getAvg(){return avg;} String toCSV() { return name+","+email+","+role+","+tech+","+comm+","+exp+","+status+","+avg+","+(imgPath==null?"":imgPath); } static Candidate fromCSV(String l) { String[] p = l.split(","); String img = (p.length > 8 && !p[8].isEmpty()) ? p[8] : null; return new Candidate(p[0], p[1], p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5]), p[6], Double.parseDouble(p[7]), img); } }
    static class User { String username, password, role; public User(String u, String p, String r) { username=u; password=p; role=r; } }

    public static void main(String[] args) { launch(args); }
}