package com.cirestechnologies.client.controller;

import com.cirestechnologies.client.model.AuthResponse;
import com.cirestechnologies.client.service.ApiService;
import com.cirestechnologies.client.service.SessionManager;
import com.cirestechnologies.client.util.AnimationUtils;
import com.cirestechnologies.client.util.SceneManager;
import com.cirestechnologies.client.util.UIComponents;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller for the Login screen
 */
public class LoginController {

    private final StackPane root;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private ProgressIndicator loadingSpinner;
    private VBox loginCard;

    private final ApiService apiService = ApiService.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public LoginController() {
        this.root = createView();
        setupAnimations();
    }

    public StackPane getView() {
        return root;
    }

    private StackPane createView() {
        StackPane stackPane = new StackPane();
        stackPane.getStyleClass().add("login-root");

        // Create animated background
        Pane backgroundPane = createAnimatedBackground();

        // Create the main content
        HBox mainContent = new HBox();
        mainContent.setAlignment(Pos.CENTER);

        // Left side - Branding
        VBox brandingPane = createBrandingPane();
        HBox.setHgrow(brandingPane, Priority.ALWAYS);

        // Right side - Login form
        VBox loginPane = createLoginPane();
        HBox.setHgrow(loginPane, Priority.ALWAYS);

        mainContent.getChildren().addAll(brandingPane, loginPane);

        stackPane.getChildren().addAll(backgroundPane, mainContent);

        return stackPane;
    }

    private Pane createAnimatedBackground() {
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #0F172A, #1E293B);");

        // Add floating circles for visual effect
        for (int i = 0; i < 6; i++) {
            Circle circle = new Circle(50 + Math.random() * 100);
            circle.setFill(Color.web("#6366F1", 0.1));
            circle.setLayoutX(Math.random() * 1200);
            circle.setLayoutY(Math.random() * 800);
            backgroundPane.getChildren().add(circle);
        }

        return backgroundPane;
    }

    private VBox createBrandingPane() {
        VBox brandingPane = new VBox(30);
        brandingPane.setAlignment(Pos.CENTER);
        brandingPane.setPadding(new Insets(60));
        brandingPane.setMinWidth(500);

        // Logo/Icon
        StackPane logoContainer = new StackPane();
        logoContainer.getStyleClass().add("logo-container");

        Circle logoBackground = new Circle(60);
        logoBackground.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6366F1")),
                new Stop(1, Color.web("#8B5CF6"))));

        DropShadow logoShadow = new DropShadow();
        logoShadow.setColor(Color.web("#6366F1", 0.5));
        logoShadow.setRadius(30);
        logoShadow.setSpread(0.2);
        logoBackground.setEffect(logoShadow);

        FontIcon logoIcon = new FontIcon("fas-shield-alt");
        logoIcon.setIconSize(50);
        logoIcon.setIconColor(Color.WHITE);

        logoContainer.getChildren().addAll(logoBackground, logoIcon);

        // Title
        Text titleText = new Text("JWT User Management");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 36));
        titleText.setFill(Color.WHITE);

        // Subtitle
        Text subtitleText = new Text("Secure Authentication & User Management");
        subtitleText.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#94A3B8"));

        // Features list
        VBox featuresList = new VBox(16);
        featuresList.setAlignment(Pos.CENTER_LEFT);
        featuresList.setMaxWidth(350);
        featuresList.setPadding(new Insets(30, 0, 0, 0));

        String[] features = {
            "JWT Token Authentication",
            "Role-Based Access Control",
            "Fake User Generation",
            "Batch User Import",
            "Profile Management"
        };
        String[] icons = {
            "fas-key",
            "fas-user-shield",
            "fas-users-cog",
            "fas-file-import",
            "fas-id-card"
        };

        for (int i = 0; i < features.length; i++) {
            HBox featureItem = new HBox(12);
            featureItem.setAlignment(Pos.CENTER_LEFT);

            StackPane iconBg = new StackPane();
            Circle iconCircle = new Circle(16);
            iconCircle.setFill(Color.web("#6366F1", 0.2));
            FontIcon icon = new FontIcon(icons[i]);
            icon.setIconSize(14);
            icon.setIconColor(Color.web("#818CF8"));
            iconBg.getChildren().addAll(iconCircle, icon);

            Label featureLabel = new Label(features[i]);
            featureLabel.setTextFill(Color.web("#CBD5E1"));
            featureLabel.setFont(Font.font("System", 14));

            featureItem.getChildren().addAll(iconBg, featureLabel);
            featuresList.getChildren().add(featureItem);
        }

        brandingPane.getChildren().addAll(logoContainer, titleText, subtitleText, featuresList);

        return brandingPane;
    }

    private VBox createLoginPane() {
        VBox loginPane = new VBox();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setPadding(new Insets(60));
        loginPane.setMinWidth(500);
        loginPane.setStyle("-fx-background-color: #1E293B;");

        // Login card
        loginCard = new VBox(24);
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(48));
        loginCard.setMaxWidth(400);
        loginCard.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 20;");

        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        cardShadow.setRadius(30);
        cardShadow.setOffsetY(10);
        loginCard.setEffect(cardShadow);

        // Welcome text
        Label welcomeLabel = new Label("Welcome Back");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.WHITE);

        Label signInLabel = new Label("Sign in to your account");
        signInLabel.setFont(Font.font("System", 14));
        signInLabel.setTextFill(Color.web("#94A3B8"));

        VBox welcomeBox = new VBox(8);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.getChildren().addAll(welcomeLabel, signInLabel);

        // Username field
        VBox usernameBox = createInputField("Username", "fas-user", false);
        usernameField = (TextField) usernameBox.lookup(".styled-text-field");

        // Password field
        VBox passwordBox = createInputField("Password", "fas-lock", true);
        passwordField = (PasswordField) passwordBox.lookup(".styled-text-field");

        // Error label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setTextFill(Color.web("#EF4444"));
        errorLabel.setFont(Font.font("System", 13));
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

        // Loading spinner
        loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(24, 24);
        loadingSpinner.setVisible(false);
        loadingSpinner.setManaged(false);

        // Login button
        loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(50);
        loginButton.setFont(Font.font("System", FontWeight.BOLD, 15));
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #8B5CF6); " +
                "-fx-text-fill: white; -fx-background-radius: 12; -fx-cursor: hand;");

        FontIcon loginIcon = new FontIcon("fas-sign-in-alt");
        loginIcon.setIconSize(16);
        loginIcon.setIconColor(Color.WHITE);
        loginButton.setGraphic(loginIcon);

        loginButton.setOnAction(e -> handleLogin());

        // Enter key to login
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // Footer
        Label footerLabel = new Label("Powered by Spring Security & JWT");
        footerLabel.setFont(Font.font("System", 11));
        footerLabel.setTextFill(Color.web("#64748B"));

        loginCard.getChildren().addAll(
            welcomeBox,
            usernameBox,
            passwordBox,
            errorLabel,
            loadingSpinner,
            loginButton,
            footerLabel
        );

        loginPane.getChildren().add(loginCard);

        return loginPane;
    }

    private VBox createInputField(String labelText, String iconCode, boolean isPassword) {
        VBox container = new VBox(8);

        Label label = new Label(labelText);
        label.setTextFill(Color.web("#94A3B8"));
        label.setFont(Font.font("System", FontWeight.MEDIUM, 13));

        HBox inputContainer = new HBox();
        inputContainer.setAlignment(Pos.CENTER_LEFT);
        inputContainer.getStyleClass().add("input-container");
        inputContainer.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 12; -fx-padding: 12 16;");

        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        icon.setIconColor(Color.web("#64748B"));

        Control field;
        if (isPassword) {
            field = new PasswordField();
            ((PasswordField) field).setPromptText("Enter your password");
        } else {
            field = new TextField();
            ((TextField) field).setPromptText("Enter your username");
        }
        field.getStyleClass().add("styled-text-field");
        field.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #64748B; -fx-padding: 0 0 0 12;");
        HBox.setHgrow(field, Priority.ALWAYS);

        inputContainer.getChildren().addAll(icon, field);
        container.getChildren().addAll(label, inputContainer);

        // Focus effect
        inputContainer.setOnMouseEntered(e ->
            inputContainer.setStyle("-fx-background-color: #334155; -fx-background-radius: 12; -fx-padding: 12 16;"));
        inputContainer.setOnMouseExited(e ->
            inputContainer.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 12; -fx-padding: 12 16;"));

        return container;
    }

    private void setupAnimations() {
        Platform.runLater(() -> {
            AnimationUtils.fadeIn(loginCard, 600);
            AnimationUtils.slideInFromBottom(loginCard, 600);
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs
        if (username.isEmpty()) {
            showError("Please enter your username");
            AnimationUtils.shake(usernameField.getParent());
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            AnimationUtils.shake(passwordField.getParent());
            passwordField.requestFocus();
            return;
        }

        // Show loading state
        setLoading(true);
        hideError();

        // Call API
        apiService.authenticate(username, password)
            .thenAccept(result -> Platform.runLater(() -> {
                setLoading(false);

                if (result.isSuccess()) {
                    AuthResponse authResponse = result.getData();
                    sessionManager.setAuthResponse(authResponse);

                    // Transition to dashboard
                    SceneManager.showDashboard();
                } else {
                    showError(result.getError());
                    AnimationUtils.shake(loginCard);
                }
            }));
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        loadingSpinner.setVisible(loading);
        loadingSpinner.setManaged(loading);

        if (loading) {
            loginButton.setText("Signing In...");
            loginButton.setGraphic(loadingSpinner);
        } else {
            loginButton.setText("Sign In");
            FontIcon loginIcon = new FontIcon("fas-sign-in-alt");
            loginIcon.setIconSize(16);
            loginIcon.setIconColor(Color.WHITE);
            loginButton.setGraphic(loginIcon);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        AnimationUtils.fadeIn(errorLabel, 200);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
