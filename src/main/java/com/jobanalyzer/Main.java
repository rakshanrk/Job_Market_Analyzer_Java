package com.jobanalyzer;

import com.jobanalyzer.models.*;
import com.jobanalyzer.services.*;
import com.jobanalyzer.utils.ChartGenerator;
import com.jobanalyzer.utils.FileValidator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jfree.chart.JFreeChart;
import javafx.scene.control.ComboBox;

import java.io.File;
import java.util.List;

/**
 * Main Application Class
 * Job Market Analyzer with Personalized Learning Paths
 */
public class Main extends Application {

    // Services
    private ResumeParser resumeParser;
    private SkillExtractor skillExtractor;
    private JobFetcher jobFetcher;
    private SkillAnalyzer skillAnalyzer;
    private LearningPathGenerator learningPathGenerator;
    private DatabaseManager databaseManager;

    // UI Components
    private Stage primaryStage;
    private Scene mainScene, resultsScene;
    private TextArea resultsTextArea;
    private ImageView chartImageView;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    private ComboBox<String> jobDomainComboBox;

    // Data
    private Resume currentResume;
    private AnalysisResult currentResult;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Initialize services
        initializeServices();

        // Create UI
        createMainScene();

        // Set up stage
        primaryStage.setTitle("Job Market Analyzer - Your Career Path Assistant");
        primaryStage.setScene(mainScene);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(true);
        primaryStage.show();

        System.out.println("✅ Application started successfully!");
    }

    /**
     * Initialize all services
     */
    private void initializeServices() {
        System.out.println("🔧 Initializing services...");

        resumeParser = new ResumeParser();
        skillExtractor = new SkillExtractor();
        jobFetcher = new JobFetcher();
        skillAnalyzer = new SkillAnalyzer();
        learningPathGenerator = new LearningPathGenerator();
        databaseManager = DatabaseManager.getInstance();

        System.out.println("✅ All services initialized");
    }

    /**
     * Create the main scene (upload screen)
     */
    private void createMainScene() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root");

        // Title
        Label titleLabel = new Label("📊 Job Market Analyzer");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Analyze your skills and get personalized learning recommendations");
        subtitleLabel.setFont(Font.font("System", 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Instructions
        VBox instructionsBox = new VBox(10);
        instructionsBox.setAlignment(Pos.CENTER);
        instructionsBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 20px; -fx-background-radius: 10px;");

        Label instructionLabel = new Label("📝 How it works:");
        instructionLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label step1 = new Label("1. Select your target job domain");
        Label step2 = new Label("2. Upload your resume (PDF or Image)");
        Label step3 = new Label("3. We'll extract your skills using AI");
        Label step4 = new Label("4. Get your personalized 4-week learning path");

        instructionsBox.getChildren().addAll(instructionLabel, step1, step2, step3, step4);

        // Job Domain Selection
        VBox domainBox = new VBox(10);
        domainBox.setAlignment(Pos.CENTER);
        domainBox.setMaxWidth(400);

        Label domainLabel = new Label("🎯 Select Your Target Job Domain:");
        domainLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        jobDomainComboBox = new ComboBox<>();
        jobDomainComboBox.getItems().addAll(
                "Software Developer",
                "Data Scientist",
                "Web Developer",
                "Mobile Developer",
                "DevOps Engineer",
                "Machine Learning Engineer",
                "Full Stack Developer",
                "Backend Developer",
                "Frontend Developer",
                "Cloud Engineer",
                "Database Administrator",
                "System Administrator"
        );
        jobDomainComboBox.setValue("Software Developer");
        jobDomainComboBox.setStyle("-fx-font-size: 14px;");
        jobDomainComboBox.setPrefWidth(350);

        domainBox.getChildren().addAll(domainLabel, jobDomainComboBox);

        // Upload button
        Button uploadButton = new Button("📤 Upload Resume");
        uploadButton.setFont(Font.font("System", FontWeight.BOLD, 18));
        uploadButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-padding: 15px 40px; -fx-background-radius: 8px;");
        uploadButton.setOnAction(e -> handleUploadResume());

        // Status label
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 14));

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(50, 50);

        // Add hover effect
        uploadButton.setOnMouseEntered(e ->
                uploadButton.setStyle("-fx-background-color: #229954; -fx-text-fill: white; " +
                        "-fx-padding: 15px 40px; -fx-background-radius: 8px; -fx-cursor: hand;"));
        uploadButton.setOnMouseExited(e ->
                uploadButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-padding: 15px 40px; -fx-background-radius: 8px;"));

        root.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                instructionsBox,
                domainBox,
                uploadButton,
                progressIndicator,
                statusLabel
        );

        mainScene = new Scene(root);
    }

    /**
     * Handle resume upload
     */
    private void handleUploadResume() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Resume File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Supported Files", "*.pdf", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            // Show loading
            progressIndicator.setVisible(true);
            statusLabel.setText("⏳ Processing your resume...");
            statusLabel.setStyle("-fx-text-fill: #3498db;");

            // Process in background thread
            new Thread(() -> processResume(selectedFile)).start();
        }
    }

    /**
     * Process the uploaded resume
     */
    private void processResume(File file) {
        try {
            // Get selected job domain
            String selectedDomain = jobDomainComboBox.getValue();

            // Step 1: Validate file
            updateStatus("📋 Validating file...");
            if (!FileValidator.isValidFile(file)) {
                showError("Invalid file: " + FileValidator.getValidationError(file));
                return;
            }

            // Step 2: Parse resume (extract text)
            updateStatus("📖 Extracting text from resume...");
            currentResume = resumeParser.parseResume(file);

            // Step 3: Extract skills
            updateStatus("🔍 Identifying your skills...");
            List<Skill> skills = skillExtractor.extractSkills(currentResume.getExtractedText());
            currentResume.setSkills(skills);

            System.out.println("Found " + skills.size() + " skills in resume");

            // Step 4: Fetch jobs (using selected domain)
            updateStatus("🌐 Fetching " + selectedDomain + " job postings...");
            List<Job> jobs = jobFetcher.fetchJobs(selectedDomain, 50);

            // Step 5: Analyze skills
            updateStatus("🤖 Analyzing skill gaps with AI...");
            currentResult = skillAnalyzer.analyzeSkills(currentResume, jobs);

            // Step 6: Generate learning path
            updateStatus("📚 Creating your personalized learning path...");
            String learningPath = learningPathGenerator.generateLearningPath(currentResult);
            currentResult.setLearningPath(learningPath);

            // Step 7: Save to database
            updateStatus("💾 Saving results...");
            int analysisId = databaseManager.saveAnalysisHistory(
                    currentResume.getUserName(),
                    currentResume.getFilename(),
                    currentResult.getMatchingSkillsAsString(),
                    currentResult.getMissingSkillsAsString(),
                    currentResult.getMatchPercentage(),
                    currentResult.getTotalJobsAnalyzed()
            );

            learningPathGenerator.saveLearningPath(analysisId, learningPath, currentResult);

            // Show results
            javafx.application.Platform.runLater(() -> showResults());

        } catch (Exception e) {
            System.err.println("❌ Error processing resume: " + e.getMessage());
            e.printStackTrace();
            showError("Error processing resume: " + e.getMessage());
        }
    }

    /**
     * Update status label (thread-safe)
     */
    private void updateStatus(String message) {
        javafx.application.Platform.runLater(() -> {
            statusLabel.setText(message);
            System.out.println(message);
        });
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            statusLabel.setText("❌ " + message);
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Processing Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Show results screen
     */
    private void showResults() {
        progressIndicator.setVisible(false);

        // Create results scene
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Top: Header with stats
        VBox header = createResultsHeader();
        root.setTop(header);

        // Center: Tabs with different views
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Summary
        Tab summaryTab = new Tab("📊 Summary");
        summaryTab.setContent(createSummaryView());

        // Tab 2: Learning Path
        Tab learningPathTab = new Tab("📚 Learning Path");
        learningPathTab.setContent(createLearningPathView());

        // Tab 3: Visualizations
        Tab chartsTab = new Tab("📈 Charts");
        chartsTab.setContent(createChartsView());

        tabPane.getTabs().addAll(summaryTab, learningPathTab, chartsTab);
        root.setCenter(tabPane);

        // Bottom: Actions
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(20, 0, 0, 0));

        Button analyzeNewButton = new Button("📤 Analyze Another Resume");
        analyzeNewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 5px;");
        analyzeNewButton.setOnAction(e -> primaryStage.setScene(mainScene));

        Button exportButton = new Button("💾 Export Results");
        exportButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 5px;");
        exportButton.setOnAction(e -> exportResults());

        actions.getChildren().addAll(analyzeNewButton, exportButton);
        root.setBottom(actions);

        resultsScene = new Scene(root, 1000, 700);
        primaryStage.setScene(resultsScene);
    }

    /**
     * Create results header with stats
     */
    private VBox createResultsHeader() {
        VBox header = new VBox(15);
        header.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 10px;");

        Label titleLabel = new Label("🎯 Analysis Results");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Stats boxes
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        // Match percentage
        VBox matchBox = createStatBox(
                String.format("%.1f%%", currentResult.getMatchPercentage()),
                "Skill Match",
                "#27ae60"
        );

        // Matching skills
        VBox matchingBox = createStatBox(
                String.valueOf(currentResult.getMatchingSkillCount()),
                "Skills You Have",
                "#3498db"
        );

        // Missing skills
        VBox missingBox = createStatBox(
                String.valueOf(currentResult.getMissingSkillCount()),
                "Skills to Learn",
                "#e74c3c"
        );

        // Jobs analyzed
        VBox jobsBox = createStatBox(
                String.valueOf(currentResult.getTotalJobsAnalyzed()),
                "Jobs Analyzed",
                "#9b59b6"
        );

        statsBox.getChildren().addAll(matchBox, matchingBox, missingBox, jobsBox);

        header.getChildren().addAll(titleLabel, statsBox);
        return header;
    }

    /**
     * Create a stat box
     */
    private VBox createStatBox(String value, String label, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15px; -fx-background-radius: 8px;");
        box.setPrefWidth(200);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label textLabel = new Label(label);
        textLabel.setFont(Font.font("System", 14));
        textLabel.setStyle("-fx-text-fill: #7f8c8d;");

        box.getChildren().addAll(valueLabel, textLabel);
        return box;
    }

    /**
     * Create summary view
     */
    private ScrollPane createSummaryView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Resume info
        VBox resumeInfo = new VBox(10);
        resumeInfo.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 8px;");

        Label resumeTitle = new Label("📄 Resume Information");
        resumeTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label filename = new Label("File: " + currentResume.getFilename());
        Label skillCount = new Label("Skills found: " + currentResume.getSkillCount());

        resumeInfo.getChildren().addAll(resumeTitle, filename, skillCount);

        // Matching skills
        VBox matchingSkills = createSkillSection(
                "✅ Skills You Have (Matching Job Requirements)",
                currentResult.getMatchingSkills(),
                "#27ae60"
        );

        // Missing skills
        VBox missingSkills = createSkillSection(
                "📚 Skills You Need to Learn",
                currentResult.getMissingSkills(),
                "#e74c3c"
        );

        content.getChildren().addAll(resumeInfo, matchingSkills, missingSkills);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Create skill section
     */
    private VBox createSkillSection(String title, List<Skill> skills, String color) {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 8px;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        FlowPane skillsFlow = new FlowPane(10, 10);

        for (Skill skill : skills) {
            Label skillLabel = new Label(skill.getName());
            skillLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-padding: 8px 15px; -fx-background-radius: 15px;");
            skillsFlow.getChildren().add(skillLabel);
        }

        if (skills.isEmpty()) {
            Label emptyLabel = new Label("None");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6;");
            skillsFlow.getChildren().add(emptyLabel);
        }

        section.getChildren().addAll(titleLabel, skillsFlow);
        return section;
    }

    /**
     * Create learning path view
     */
    private ScrollPane createLearningPathView() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextArea pathTextArea = new TextArea(currentResult.getLearningPath());
        pathTextArea.setEditable(false);
        pathTextArea.setWrapText(true);
        pathTextArea.setFont(Font.font("Consolas", 13));
        pathTextArea.setStyle("-fx-control-inner-background: #f9f9f9;");
        pathTextArea.setPrefHeight(600);

        content.getChildren().add(pathTextArea);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Create charts view
     */
    private ScrollPane createChartsView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        try {
            // Create bar chart
            JFreeChart barChart = ChartGenerator.createSkillMatchBarChart(currentResult);
            Image barChartImage = ChartGenerator.convertChartToImage(barChart);
            ImageView barChartView = new ImageView(barChartImage);
            barChartView.setPreserveRatio(true);
            barChartView.setFitWidth(750);

            VBox barChartBox = new VBox(10);
            barChartBox.setAlignment(Pos.CENTER);
            barChartBox.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px;");
            barChartBox.getChildren().add(barChartView);

            // Create top missing skills chart
            JFreeChart topSkillsChart = ChartGenerator.createTopMissingSkillsChart(currentResult);
            Image topSkillsImage = ChartGenerator.convertChartToImage(topSkillsChart);
            ImageView topSkillsView = new ImageView(topSkillsImage);
            topSkillsView.setPreserveRatio(true);
            topSkillsView.setFitWidth(750);

            VBox topSkillsBox = new VBox(10);
            topSkillsBox.setAlignment(Pos.CENTER);
            topSkillsBox.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px;");
            topSkillsBox.getChildren().add(topSkillsView);

            content.getChildren().addAll(barChartBox, topSkillsBox);

        } catch (Exception e) {
            Label errorLabel = new Label("Error generating charts: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            content.getChildren().add(errorLabel);
            e.printStackTrace();
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");
        return scrollPane;
    }

    /**
     * Export results to file
     */
    private void exportResults() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results");
        fileChooser.setInitialFileName("job_analysis_results.txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                StringBuilder export = new StringBuilder();
                export.append("=".repeat(60)).append("\n");
                export.append("JOB MARKET ANALYSIS RESULTS\n");
                export.append("=".repeat(60)).append("\n\n");

                export.append("Resume: ").append(currentResume.getFilename()).append("\n");
                export.append("Analysis Date: ").append(java.time.LocalDateTime.now()).append("\n\n");

                export.append("SUMMARY\n");
                export.append("-".repeat(60)).append("\n");
                export.append("Match Percentage: ").append(String.format("%.1f%%", currentResult.getMatchPercentage())).append("\n");
                export.append("Skills You Have: ").append(currentResult.getMatchingSkillCount()).append("\n");
                export.append("Skills to Learn: ").append(currentResult.getMissingSkillCount()).append("\n");
                export.append("Jobs Analyzed: ").append(currentResult.getTotalJobsAnalyzed()).append("\n\n");

                export.append("MATCHING SKILLS\n");
                export.append("-".repeat(60)).append("\n");
                export.append(currentResult.getMatchingSkillsAsString()).append("\n\n");

                export.append("SKILLS TO LEARN\n");
                export.append("-".repeat(60)).append("\n");
                export.append(currentResult.getMissingSkillsAsString()).append("\n\n");

                export.append(currentResult.getLearningPath());

                java.nio.file.Files.writeString(file.toPath(), export.toString());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("Results Exported");
                alert.setContentText("Results saved to: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                showError("Error exporting results: " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        // Clean up resources
        if (databaseManager != null) {
            databaseManager.close();
        }
        System.out.println("👋 Application closed");
    }

    public static void main(String[] args) {
        System.out.println("🚀 Starting Job Market Analyzer...");
        launch(args);
    }
}