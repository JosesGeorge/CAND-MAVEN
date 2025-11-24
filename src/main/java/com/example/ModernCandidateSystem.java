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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 2025 ULTIMATE CANDIDATE EVALUATION SYSTEM
 * Features: Watson-Style Dashboard, Deep Analytics, Interactive DB
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
    private ScrollPane dashboardScroll;
    private VBox addCandidateView;
    private HBox databaseView;
    private VBox settingsView;
    private VBox profileView;
    
    // Auth Views
    private VBox loginCard;
    private VBox signUpCard;
    private StackPane authContainer;

    // Dashboard Live Components
    private Label lblTotal, lblHired, lblRate;
    private PieChart chartStatus;
    private BarChart<String, Number> chartRoles;
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

        dashboardScroll = createDashboard();
        addCandidateView = createAddCandidateView();
        databaseView = createDatabaseView();
        settingsView = createSettingsView();
        profileView = createProfileView();

        mainLayout.setLeft(createSidebar());
        switchView(dashboardScroll);

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

    // ... [BACKGROUND & AUTH METHODS UNCHANGED] ...
    private void addLiquidBackground(Pane root) {
        root.getChildren().addAll(
            createBlob(Color.web("#3b82f6", 0.3), 200, -450, -300, 15),
            createBlob(Color.web("#8b5cf6", 0.25), 250, 450, 350, 20),
            createBlob(Color.web("#10b981", 0.2), 180, -350, 250, 18),
            createBlob(Color.web("#f59e0b", 0.15), 220, 350, -250, 22)
        );
    }
    private Circle createBlob(Color c, double r, double x, double y, double d) {
        Circle b = new Circle(r, c); b.setEffect(new GaussianBlur(80)); b.setTranslateX(x); b.setTranslateY(y);
        Timeline t = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(b.translateXProperty(), x), new KeyValue(b.translateYProperty(), y)), new KeyFrame(Duration.seconds(d/2), new KeyValue(b.translateXProperty(), x+40), new KeyValue(b.translateYProperty(), y-40)), new KeyFrame(Duration.seconds(d), new KeyValue(b.translateXProperty(), x), new KeyValue(b.translateYProperty(), y))); t.setAutoReverse(true); t.setCycleCount(Animation.INDEFINITE); t.play(); return b;
    }
    private StackPane createAuthView() {
        StackPane authPane = new StackPane(); authPane.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95);");
        loginCard = new VBox(20); styleAuthCard(loginCard);
        Text loginTitle = new Text("System Access"); loginTitle.setStyle("-fx-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        TextField txtUser = new TextField(); txtUser.setPromptText("Username");
        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Password"); styleAuthField(txtPass);
        GlassButton btnLogin = new GlassButton("Login", Color.web("#3b82f6")); btnLogin.setMaxWidth(Double.MAX_VALUE);
        Hyperlink linkSignUp = new Hyperlink("Create an Account"); linkSignUp.setStyle("-fx-text-fill: #3b82f6; -fx-border-color: transparent;"); linkSignUp.setOnAction(e -> toggleAuthMode(false));
        btnLogin.setOnAction(e -> { Optional<User> valid = userList.stream().filter(u -> u.username.equals(txtUser.getText()) && u.password.equals(txtPass.getText())).findFirst(); if (valid.isPresent() || (txtUser.getText().equals("admin") && txtPass.getText().equals("admin"))) { currentUser = valid.orElse(new User("Administrator", "admin", "System Admin")); unlockApp(authPane); txtPass.clear(); } else { shake(loginCard); txtPass.clear(); } });
        loginCard.getChildren().addAll(loginTitle, txtUser, txtPass, btnLogin, linkSignUp);
        signUpCard = new VBox(20); styleAuthCard(signUpCard); signUpCard.setVisible(false);
        Text signTitle = new Text("Register Admin"); signTitle.setStyle("-fx-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        TextField regUser = new TextField(); regUser.setPromptText("Choose Username");
        PasswordField regPass = new PasswordField(); regPass.setPromptText("Choose Password"); styleAuthField(regPass);
        GlassButton btnRegister = new GlassButton("Sign Up", Color.web("#10b981")); btnRegister.setMaxWidth(Double.MAX_VALUE);
        Hyperlink linkLogin = new Hyperlink("Back to Login"); linkLogin.setStyle("-fx-text-fill: #3b82f6; -fx-border-color: transparent;"); linkLogin.setOnAction(e -> toggleAuthMode(true));
        btnRegister.setOnAction(e -> { if(regUser.getText().isEmpty() || regPass.getText().isEmpty()) { shake(signUpCard); return; } userList.add(new User(regUser.getText(), regPass.getText(), "Evaluator")); saveUsers(); txtUser.setText(regUser.getText()); txtPass.clear(); toggleAuthMode(true); btnLogin.setText("Account Created! Login Now"); });
        signUpCard.getChildren().addAll(signTitle, regUser, regPass, btnRegister, linkLogin);
        authPane.getChildren().addAll(loginCard, signUpCard); return authPane;
    }
    private void styleAuthCard(VBox card) { card.setMaxSize(400, 400); card.setAlignment(Pos.CENTER); card.getStyleClass().add("glass-panel"); card.setPadding(new Insets(40)); }
    private void styleAuthField(PasswordField pf) { pf.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12;"); }
    private void toggleAuthMode(boolean showLogin) { VBox show = showLogin ? loginCard : signUpCard; VBox hide = showLogin ? signUpCard : loginCard; hide.setVisible(false); show.setVisible(true); FadeTransition ft = new FadeTransition(Duration.millis(300), show); ft.setFromValue(0); ft.setToValue(1); ft.play(); }
    private void unlockApp(Pane authPane) { updateProfileView(); FadeTransition ft = new FadeTransition(Duration.millis(500), authPane); ft.setFromValue(1.0); ft.setToValue(0.0); ft.setOnFinished(ev -> { authPane.setVisible(false); mainLayout.setVisible(true); playIntroAnimation(); refreshStats(); }); ft.play(); }
    private void logout() { FadeTransition ft = new FadeTransition(Duration.millis(300), mainLayout); ft.setFromValue(1.0); ft.setToValue(0.0); ft.setOnFinished(e -> { mainLayout.setVisible(false); mainLayout.setOpacity(1.0); authContainer.setVisible(true); authContainer.setOpacity(0.0); toggleAuthMode(true); FadeTransition ftAuth = new FadeTransition(Duration.millis(500), authContainer); ftAuth.setFromValue(0.0); ftAuth.setToValue(1.0); ftAuth.play(); currentUser = null; }); ft.play(); }

    // ==========================================
    // ADD VIEW - Complete Reset Logic
    // ==========================================
    private VBox createAddCandidateView() {
        VBox container = new VBox(20); container.setPadding(new Insets(40)); container.setAlignment(Pos.TOP_LEFT);
        Text header = new Text("New Candidate Evaluation"); header.getStyleClass().add("header-text");

        // Photo Upload
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

        HBox emailGroup = new HBox(); emailGroup.setAlignment(Pos.CENTER_LEFT); emailGroup.getStyleClass().add("input-group");
        TextField txtEmailHandle = new TextField(); txtEmailHandle.setPromptText("username"); txtEmailHandle.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-padding: 10 5 10 10;"); HBox.setHgrow(txtEmailHandle, Priority.ALWAYS);
        Label lblSuffix = new Label("@gmail.com"); lblSuffix.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14px; -fx-padding: 0 15 0 0;");
        emailGroup.getChildren().addAll(txtEmailHandle, lblSuffix);

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
                String m = txtEmailHandle.getText().trim(); if(!m.isEmpty()) m += "@gmail.com";
                String r = cmbRole.getValue();
                if(n.isEmpty() || m.isEmpty() || r == null) throw new Exception("Empty fields");

                int tech = Integer.parseInt(t1.getText());
                int comm = Integer.parseInt(t2.getText());
                int exp = Integer.parseInt(t3.getText());
                double avg = (tech + comm) / 2.0;
                String status = (avg >= 85 && exp >= 2) ? "HIRED" : (avg >= 70 ? "SHORTLISTED" : (avg >= 50 && exp > 5 ? "ON HOLD" : "REJECTED"));
                
                candidateList.add(new Candidate(n, m, r, tech, comm, exp, status, avg, droppedImgPath[0]));
                refreshStats();
                
                // --- COMPLETE RESET ---
                txtName.clear(); txtEmailHandle.clear(); 
                t1.clear(); t2.clear(); t3.clear();
                cmbRole.getSelectionModel().clearSelection(); // Reset Combo
                cmbRole.setPromptText("Select Job Role"); // Ensure prompt returns
                imgPreview.setImage(null); lblDrop.setVisible(true); // Reset Image
                droppedImgPath[0] = null; // Reset Path
                
                btnSave.setText("Result: " + status + " (Saved)");
                PauseTransition pt = new PauseTransition(Duration.seconds(2));
                pt.setOnFinished(ev -> btnSave.setText("Calculate & Save Result")); pt.play();
            } catch(Exception ex) { shake(btnSave); }
        });

        container.getChildren().addAll(header, dropZone, txtName, emailGroup, cmbRole, scores, btnSave);
        return container;
    }

    // ==========================================
    // DATABASE VIEW - Interactive & Detailed
    // ==========================================
    private HBox createDatabaseView() {
        HBox splitView = new HBox(20); splitView.setPadding(new Insets(30));
        
        // Left: List
        VBox leftPane = new VBox(15); HBox.setHgrow(leftPane, Priority.ALWAYS); leftPane.setMaxWidth(400);
        Text header = new Text("Database"); header.getStyleClass().add("header-text");
        TextField search = new TextField(); search.setPromptText("Search...");
        ListView<Candidate> list = new ListView<>(); list.setStyle("-fx-background-color: transparent;");
        list.setCellFactory(p -> new CandidateCell());
        FilteredList<Candidate> filtered = new FilteredList<>(candidateList, p->true);
        search.textProperty().addListener((o, old, val) -> filtered.setPredicate(c -> val.isEmpty() || c.name.toLowerCase().contains(val.toLowerCase())));
        list.setItems(filtered); VBox.setVgrow(list, Priority.ALWAYS);
        leftPane.getChildren().addAll(header, search, list);

        // Right: Detailed Analytics
        VBox detailPane = new VBox(20); detailPane.getStyleClass().add("glass-panel");
        detailPane.setPadding(new Insets(30)); detailPane.setAlignment(Pos.CENTER);
        HBox.setHgrow(detailPane, Priority.ALWAYS);
        
        Text emptyText = new Text("Select a candidate to view analytics");
        emptyText.setStyle("-fx-fill: rgba(255,255,255,0.3); -fx-font-size: 16px;");
        detailPane.getChildren().add(emptyText);

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal != null) updateDetailPane(detailPane, newVal);
        });

        splitView.getChildren().addAll(leftPane, detailPane);
        return splitView;
    }

    private void updateDetailPane(VBox pane, Candidate c) {
        pane.getChildren().clear();
        
        // Header Section
        HBox headerBox = new HBox(20); headerBox.setAlignment(Pos.CENTER_LEFT);
        ImageView iv = new ImageView();
        if(c.imgPath != null) try { iv.setImage(new Image(c.imgPath)); } catch(Exception e) {}
        iv.setFitWidth(80); iv.setFitHeight(80); Circle clip = new Circle(40, 40, 40); iv.setClip(clip);
        
        VBox info = new VBox(5);
        Text name = new Text(c.name); name.setStyle("-fx-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Text role = new Text(c.role); role.setStyle("-fx-fill: #3b82f6; -fx-font-size: 16px;");
        String color = c.status.equals("HIRED") ? "#10b981" : (c.status.equals("REJECTED") ? "#ef4444" : "#f59e0b");
        Label badge = new Label(c.status); badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 15; -fx-font-weight: bold;");
        info.getChildren().addAll(name, role, badge);
        headerBox.getChildren().addAll(iv, info);

        // Advanced Stats
        GridPane stats = new GridPane(); stats.setHgap(15); stats.setVgap(10);
        stats.add(createStatBar("Technical", c.tech, "#3b82f6"), 0, 0);
        stats.add(createStatBar("Communication", c.comm, "#8b5cf6"), 0, 1);
        
        // Percentile Calculation
        long betterThan = candidateList.stream().filter(cand -> cand.avg < c.avg).count();
        int percentile = (int) ((double)betterThan / candidateList.size() * 100);
        VBox pBox = new VBox(5); pBox.setAlignment(Pos.CENTER);
        Text pVal = new Text("Top " + (100-percentile) + "%"); pVal.setStyle("-fx-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Text pLbl = new Text("Global Ranking"); pLbl.setStyle("-fx-fill: grey; -fx-font-size: 12px;");
        pBox.getChildren().addAll(pVal, pLbl);
        stats.add(pBox, 1, 0, 1, 2);

        Canvas radar = new RadarChartCanvas(c.tech, c.comm, c.exp * 10);
        
        HBox actions = new HBox(10); actions.setAlignment(Pos.CENTER);
        if(c.status.equals("HIRED")) {
            GlassButton btnOffer = new GlassButton("Generate Offer", Color.web("#10b981"));
            btnOffer.setOnAction(e -> generateHTMLReport(c)); actions.getChildren().add(btnOffer);
        }
        
        pane.getChildren().addAll(headerBox, new Separator(), stats, radar, actions);
        FadeTransition ft = new FadeTransition(Duration.millis(400), pane); ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private VBox createStatBar(String label, int val, String color) {
        VBox box = new VBox(5);
        HBox top = new HBox(); top.setAlignment(Pos.CENTER_LEFT);
        Text l = new Text(label); l.setStyle("-fx-fill: white; -fx-font-size: 12px;");
        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Text v = new Text(val + "/100"); v.setStyle("-fx-fill: " + color + "; -fx-font-weight: bold;");
        top.getChildren().addAll(l, r, v);
        
        ProgressBar pb = new ProgressBar(val / 100.0);
        pb.setStyle("-fx-accent: " + color + ";");
        pb.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(top, pb);
        return box;
    }

    // ==========================================
    // DASHBOARD - Watson Studio Style
    // ==========================================
    private ScrollPane createDashboard() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox content = new VBox(25); content.setPadding(new Insets(30));
        Text header = new Text("Executive Overview"); header.getStyleClass().add("header-text");

        // 1. KPI Row
        HBox kpiRow = new HBox(20); kpiRow.setAlignment(Pos.CENTER);
        lblTotal = new Label("0"); lblHired = new Label("0"); lblRate = new Label("0%");
        kpiRow.getChildren().addAll(
            createWatsonCard("Total Candidates", lblTotal, "#3b82f6"),
            createWatsonCard("Positions Filled", lblHired, "#10b981"),
            createWatsonCard("Success Rate", lblRate, "#f59e0b")
        );

        // 2. Analytics Grid
        GridPane analyticsGrid = new GridPane(); analyticsGrid.setHgap(20); analyticsGrid.setVgap(20);
        
        // Pie
        chartStatus = new PieChart(); chartStatus.setTitle("Pipeline Status"); chartStatus.setLegendSide(Side.RIGHT);
        analyticsGrid.add(wrapWatsonCard("Status Distribution", chartStatus), 0, 0);
        
        // Bar
        CategoryAxis xRole = new CategoryAxis(); NumberAxis yRole = new NumberAxis();
        chartRoles = new BarChart<>(xRole, yRole); chartRoles.setLegendVisible(false);
        analyticsGrid.add(wrapWatsonCard("Role Demographics", chartRoles), 1, 0);
        
        ColumnConstraints chartCol = new ColumnConstraints(); chartCol.setPercentWidth(50);
        analyticsGrid.getColumnConstraints().addAll(chartCol, chartCol);

        // 3. Top Performers Table (New)
        TableColumn<Candidate, String> colName = new TableColumn<>("Name"); colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Candidate, String> colRole = new TableColumn<>("Role"); colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        TableColumn<Candidate, Double> colScore = new TableColumn<>("Avg Score"); colScore.setCellValueFactory(new PropertyValueFactory<>("avg"));
        
        tableTopPerformers = new TableView<>();
        tableTopPerformers.getColumns().addAll(colName, colRole, colScore);
        tableTopPerformers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTopPerformers.setMaxHeight(200);
        
        content.getChildren().addAll(header, kpiRow, analyticsGrid, wrapWatsonCard("Top 5 Performers", tableTopPerformers));
        scroll.setContent(content);
        return scroll;
    }
    
    private VBox createWatsonCard(String title, Label value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setMinWidth(250);
        card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.7); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 16;");
        
        Text t = new Text(title.toUpperCase()); t.setStyle("-fx-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
        value.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 36px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(t, value);
        return card;
    }
    
    private VBox wrapWatsonCard(String title, Node content) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.7); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 16;");
        
        Text t = new Text(title); t.setStyle("-fx-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        card.getChildren().addAll(t, content);
        return card;
    }

    // --- SIDEBAR, SETTINGS, HELPERS (Standard) ---
    private VBox createSidebar() {
        VBox sidebar = new VBox(20); sidebar.setPadding(new Insets(30)); sidebar.setPrefWidth(260); sidebar.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 1 0 0;");
        Text title = new Text("Candidate Evaluation\nSystem"); title.getStyleClass().add("header-text"); title.setStyle("-fx-font-size: 20px; -fx-text-alignment: left; -fx-fill: white; -fx-font-weight: bold;");
        Text subTitle = new Text("Enterprise Analytics"); subTitle.getStyleClass().add("sub-header");
        GlassButton btnDash = new GlassButton("Dashboard", true); btnDash.setOnAction(e -> { setActive(btnDash); switchView(dashboardScroll); });
        GlassButton btnAdd = new GlassButton("New Candidate", false); btnAdd.setOnAction(e -> { setActive(btnAdd); switchView(addCandidateView); });
        GlassButton btnDb = new GlassButton("Database", false); btnDb.setOnAction(e -> { setActive(btnDb); switchView(databaseView); });
        GlassButton btnSet = new GlassButton("Settings", false); btnSet.setOnAction(e -> { setActive(btnSet); switchView(settingsView); });
        GlassButton btnProfile = new GlassButton("My Profile", false); btnProfile.setOnAction(e -> { setActive(btnProfile); switchView(profileView); });
        VBox nav = new VBox(15); nav.getChildren().addAll(btnDash, btnAdd, btnDb, btnSet);
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(title, subTitle, nav, spacer, btnProfile); return sidebar;
    }
    private VBox createProfileView() { VBox container = new VBox(25); container.setPadding(new Insets(40)); container.setAlignment(Pos.CENTER); return container; }
    private void updateProfileView() { profileView.getChildren().clear(); Circle avatar = new Circle(60); avatar.setFill(Color.web("#3b82f6")); avatar.setEffect(new DropShadow(20, Color.web("#3b82f6", 0.4))); Text initials = new Text(currentUser!=null ? currentUser.username.substring(0,1).toUpperCase() : "?"); initials.setStyle("-fx-font-size: 40px; -fx-fill: white; -fx-font-weight: bold;"); StackPane img = new StackPane(avatar, initials); Text name = new Text(currentUser!=null?currentUser.username:"Guest"); name.setStyle("-fx-font-size: 32px; -fx-fill: white; -fx-font-weight: bold;"); Text role = new Text(currentUser!=null?currentUser.role:"Viewer"); role.setStyle("-fx-font-size: 18px; -fx-fill: #94a3b8;"); GlassButton btnLogout = new GlassButton("Logout", Color.web("#ef4444")); btnLogout.setMaxWidth(200); btnLogout.setOnAction(e -> logout()); profileView.getChildren().addAll(img, name, role, new Separator(), btnLogout); }
    private VBox createSettingsView() { VBox container = new VBox(20); container.setAlignment(Pos.CENTER); Text header = new Text("System Settings"); header.getStyleClass().add("header-text"); GlassButton btnTheme = new GlassButton("Toggle Light/Dark Mode", false); btnTheme.setOnAction(e -> { isLightMode = !isLightMode; if(isLightMode) rootStack.getStyleClass().add("light-mode"); else rootStack.getStyleClass().remove("light-mode"); }); GlassButton btnExport = new GlassButton("Export Database (CSV)", Color.web("#2563eb")); btnExport.setOnAction(e -> saveData()); container.getChildren().addAll(header, btnTheme, btnExport); return container; }
    private void switchView(Node newView) { if (centerContainer.getChildren().isEmpty()) { centerContainer.getChildren().add(newView); return; } Node current = centerContainer.getChildren().get(centerContainer.getChildren().size() - 1); if (current == newView) return; newView.setOpacity(0); centerContainer.getChildren().add(newView); FadeTransition in = new FadeTransition(Duration.millis(300), newView); in.setToValue(1); FadeTransition out = new FadeTransition(Duration.millis(300), current); out.setToValue(0); out.setOnFinished(e -> centerContainer.getChildren().remove(current)); new ParallelTransition(in, out).play(); refreshStats(); }
    private void setActive(GlassButton b) { ((VBox)b.getParent()).getChildren().forEach(n -> { if(n instanceof GlassButton) ((GlassButton)n).setActive(false); }); b.setActive(true); }
    private void generateHTMLReport(Candidate c) { try { File f = new File("Offer_" + c.name.replaceAll(" ", "_") + ".html"); PrintWriter pw = new PrintWriter(f); pw.println("<html><body style='font-family: sans-serif; padding: 40px;'>"); pw.println("<h1 style='color: #2563eb;'>OFFICIAL JOB OFFER</h1><hr>"); pw.println("<h3>Dear " + c.name + ",</h3>"); pw.println("<p>We are pleased to offer you the position of <b>" + c.role + "</b>.</p>"); pw.println("<p>Based on your Technical Score (" + c.tech + "), we believe you are a great fit.</p>"); pw.println("<br><p>Sincerely,<br>HR Team</p></body></html>"); pw.close(); getHostServices().showDocument(f.toURI().toString()); } catch(Exception e) { e.printStackTrace(); } }
    private void shake(Node node) { TranslateTransition tt = new TranslateTransition(Duration.millis(50), node); tt.setByX(10); tt.setAutoReverse(true); tt.setCycleCount(4); tt.play(); }
    private void playIntroAnimation() { mainLayout.setOpacity(0); mainLayout.setTranslateY(30); FadeTransition ft=new FadeTransition(Duration.millis(1000), mainLayout); ft.setToValue(1); TranslateTransition tt=new TranslateTransition(Duration.millis(1000), mainLayout); tt.setToY(0); new ParallelTransition(ft, tt).play(); }
    private void refreshStats() { 
        int h=0, r=0, o=0; Map<String, Integer> roles = new HashMap<>(); 
        for(Candidate c:candidateList) { if(c.status.equals("HIRED")) h++; else if(c.status.equals("REJECTED")) r++; else o++; roles.put(c.role, roles.getOrDefault(c.role,0)+1); } 
        lblTotal.setText(candidateList.size()+""); lblHired.setText(h+""); lblRate.setText((candidateList.isEmpty() ? 0 : (h*100/candidateList.size())) + "%");
        chartStatus.getData().clear(); chartStatus.getData().addAll(new PieChart.Data("Hired", h), new PieChart.Data("Rejected", r), new PieChart.Data("On Hold", o));
        chartRoles.getData().clear(); XYChart.Series<String, Number> s = new XYChart.Series<>(); roles.forEach((k,v) -> s.getData().add(new XYChart.Data<>(k, v))); chartRoles.getData().add(s);
        
        // Update Top Performers
        ObservableList<Candidate> sorted = FXCollections.observableArrayList(candidateList);
        sorted.sort(Comparator.comparingDouble(Candidate::getAvg).reversed());
        tableTopPerformers.setItems(FXCollections.observableArrayList(sorted.stream().limit(5).collect(Collectors.toList())));
    }
    private void saveData() { try(PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) { for(Candidate c : candidateList) pw.println(c.toCSV()); } catch(Exception e) {} }
    private void loadData() { File f = new File(DATA_FILE); if(!f.exists()) return; try(BufferedReader br = new BufferedReader(new FileReader(f))) { String l; while((l=br.readLine())!=null) candidateList.add(Candidate.fromCSV(l)); } catch(Exception e) {} }
    private void saveUsers() { try(PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) { for(User u : userList) pw.println(u.username + "," + u.password + "," + u.role); } catch(Exception e) {} }
    private void loadUsers() { File f = new File(USERS_FILE); if(!f.exists()) return; try(BufferedReader br = new BufferedReader(new FileReader(f))) { String l; while((l=br.readLine())!=null) { String[] p = l.split(","); if(p.length >= 3) userList.add(new User(p[0], p[1], p[2])); } } catch(Exception e) {} }

    class RadarChartCanvas extends Canvas {
        public RadarChartCanvas(double t, double c, double e) { super(300, 300); GraphicsContext gc = getGraphicsContext2D(); double cx=150, cy=150, r=100; gc.setStroke(Color.GRAY); gc.setLineWidth(1); for(int i=0; i<3; i++) { double ang = Math.toRadians(i * 120 - 90); gc.strokeLine(cx, cy, cx + Math.cos(ang)*r, cy + Math.sin(ang)*r); } gc.strokeOval(cx-r, cy-r, r*2, r*2); double[] xPoints = { cx + Math.cos(Math.toRadians(-90)) * (t/100.0*r), cx + Math.cos(Math.toRadians(30)) * (c/100.0*r), cx + Math.cos(Math.toRadians(150)) * (e/100.0*r) }; double[] yPoints = { cy + Math.sin(Math.toRadians(-90)) * (t/100.0*r), cy + Math.sin(Math.toRadians(30)) * (c/100.0*r), cy + Math.sin(Math.toRadians(150)) * (e/100.0*r) }; gc.setFill(Color.web("#3b82f6", 0.5)); gc.fillPolygon(xPoints, yPoints, 3); gc.setFill(Color.GRAY); gc.fillText("Tech", cx, cy-r-10); gc.fillText("Comm", cx+r-20, cy+r/2); gc.fillText("Exp", cx-r, cy+r/2); }
    }
    static class GlassButton extends Button { private boolean isActive=false; private Color customColor=null; public GlassButton(String t, boolean a) { super(t); isActive=a; init(); } public GlassButton(String t, Color c) { super(t); customColor=c; init(); } private void init() { getStyleClass().add("glass-button"); updateStyle(); setPrefWidth(200); setOnMouseEntered(e -> { if (customColor != null) setStyle("-fx-background-color: " + toHex(customColor.deriveColor(0, 1, 1.2, 1)) + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"); else if (!isActive) setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-background-radius: 12; -fx-alignment: CENTER_LEFT;"); setScaleX(1.02); setScaleY(1.02); }); setOnMouseExited(e -> { updateStyle(); setScaleX(1.0); setScaleY(1.0); }); setOnMousePressed(e -> { setScaleX(0.97); setScaleY(0.97); }); setOnMouseReleased(e -> { setScaleX(1.02); setScaleY(1.02); }); } public void setActive(boolean b) { isActive = b; updateStyle(); } private void updateStyle() { if (customColor != null) { String hex = toHex(customColor); setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold; -fx-alignment: CENTER;"); } else { String fill = isActive ? "rgba(59, 130, 246, 0.4)" : "rgba(255,255,255,0.05)"; String border = isActive ? "-fx-border-color: #3b82f6; -fx-border-width: 0 0 0 3;" : ""; setStyle("-fx-background-color: " + fill + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-alignment: CENTER_LEFT; " + border); } } private String toHex(Color c) { return String.format("#%02X%02X%02X", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255)); } }
    static class CandidateCell extends ListCell<Candidate> { @Override protected void updateItem(Candidate c, boolean e) { super.updateItem(c, e); if(e || c==null) { setGraphic(null); setText(null); return; } HBox card = new HBox(15); card.setAlignment(Pos.CENTER_LEFT); ImageView iv = new ImageView(); if(c.imgPath != null) try { iv.setImage(new Image(c.imgPath)); } catch(Exception ex) {} iv.setFitWidth(40); iv.setFitHeight(40); Circle clip = new Circle(20, 20, 20); iv.setClip(clip); VBox info = new VBox(2); Text n = new Text(c.name); n.getStyleClass().add("text-primary"); n.setStyle("-fx-font-weight: bold; -fx-fill: -text-primary;"); Text r = new Text(c.role); r.getStyleClass().add("text-secondary"); r.setStyle("-fx-fill: -text-secondary; -fx-font-size: 12px;"); info.getChildren().addAll(n, r); card.getChildren().addAll(iv, info); setGraphic(card); } }
    public static class Candidate { String name, email, role, status, imgPath; int tech, comm, exp; double avg; public Candidate(String n, String e, String rl, int t, int c, int x, String s, double a, String i) { name=n; email=e; role=rl; tech=t; comm=c; exp=x; status=s; avg=a; imgPath=i; } public String getName(){return name;} public String getRole(){return role;} public double getAvg(){return avg;} String toCSV() { return name+","+email+","+role+","+tech+","+comm+","+exp+","+status+","+avg+","+(imgPath==null?"":imgPath); } static Candidate fromCSV(String l) { String[] p = l.split(","); String img = p.length > 8 ? p[8] : null; return new Candidate(p[0], p[1], p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5]), p[6], Double.parseDouble(p[7]), img); } }
    static class User { String username, password, role; public User(String u, String p, String r) { username=u; password=p; role=r; } }

    public static void main(String[] args) { launch(args); }
}