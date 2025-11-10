package com.jobanalyzer;

import java.sql.ResultSet;
import java.util.ArrayList;
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
     * Create the main scene (upload screen) - IMPROVED VERSION with vibrant styling
     */
    private void createMainScene() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");

        // Title
        Label titleLabel = new Label("📊 Job Market Analyzer");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");

        Label subtitleLabel = new Label("Analyze your skills and get personalized learning recommendations");
        subtitleLabel.setFont(Font.font("System", 16));
        subtitleLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");

        // Instructions
        VBox instructionsBox = new VBox(10);
        instructionsBox.setAlignment(Pos.CENTER);
        instructionsBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-padding: 25px; " +
                "-fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");

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
        domainBox.setMaxWidth(450);
        domainBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-padding: 20px; " +
                "-fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");

        Label domainLabel = new Label("🎯 Select Your Target Job Domain:");
        domainLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        jobDomainComboBox = new ComboBox<>();
        jobDomainComboBox.getItems().addAll(
                "Software Developer",
                "Data Scientist / ML Engineer",
                "Full Stack Web Developer",
                "Frontend Developer",
                "Backend Developer",
                "Mobile App Developer (iOS/Android)",
                "DevOps Engineer / SRE",
                "Cloud Engineer / Architect",
                "Data Engineer",
                "AI / Deep Learning Engineer",
                "Cybersecurity Engineer",
                "QA / Test Automation Engineer",
                "Product Manager (Technical)",
                "Business Analyst",
                "UI/UX Designer"
        );
        jobDomainComboBox.setValue("Software Developer");
        jobDomainComboBox.setStyle("-fx-font-size: 14px;");
        jobDomainComboBox.setPrefWidth(380);

        domainBox.getChildren().addAll(domainLabel, jobDomainComboBox);

        // Upload button with gradient
        Button uploadButton = new Button("📤 Upload Resume");
        uploadButton.setFont(Font.font("System", FontWeight.BOLD, 18));
        uploadButton.setStyle("-fx-background-color: linear-gradient(to right, #11998e, #38ef7d); " +
                "-fx-text-fill: white; -fx-padding: 15px 40px; -fx-background-radius: 25px; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
        uploadButton.setOnAction(e -> handleUploadResume());

        // Status label
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 14));
        statusLabel.setStyle("-fx-text-fill: white;");

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(50, 50);

        // Add hover effect
        uploadButton.setOnMouseEntered(e ->
                uploadButton.setStyle("-fx-background-color: linear-gradient(to right, #0d7a6e, #2dd170); " +
                        "-fx-text-fill: white; -fx-padding: 15px 40px; -fx-background-radius: 25px; " +
                        "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 4);"));
        uploadButton.setOnMouseExited(e ->
                uploadButton.setStyle("-fx-background-color: linear-gradient(to right, #11998e, #38ef7d); " +
                        "-fx-text-fill: white; -fx-padding: 15px 40px; -fx-background-radius: 25px; " +
                        "-fx-font-size: 16px; -fx-font-weight: bold; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"));

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
            progressIndicator.setVisible(true);
            statusLabel.setText("⏳ Processing your resume...");
            statusLabel.setStyle("-fx-text-fill: white;");

            new Thread(() -> processResume(selectedFile)).start();
        }
    }

    /**
     * Process the uploaded resume
     */
    private void processResume(File file) {
        try {
            updateStatus("📋 Validating file...");
            if (!FileValidator.isValidFile(file)) {
                showError("Invalid file: " + FileValidator.getValidationError(file));
                return;
            }

            updateStatus("📖 Extracting text from resume...");
            currentResume = resumeParser.parseResume(file);

            updateStatus("🔍 Identifying your skills...");
            List<Skill> skills = skillExtractor.extractSkills(currentResume.getExtractedText());
            currentResume.setSkills(skills);

            System.out.println("Found " + skills.size() + " skills in resume");

            updateStatus("🌐 Fetching job postings from market...");
            String selectedDomain = jobDomainComboBox.getValue();
            List<Job> jobs = jobFetcher.fetchJobs(selectedDomain, 50);

            updateStatus("🤖 Analyzing skill gaps with AI...");
            currentResult = skillAnalyzer.analyzeSkills(currentResume, jobs);

            updateStatus("📚 Creating your personalized learning path...");
            String learningPath = learningPathGenerator.generateLearningPath(currentResult);
            currentResult.setLearningPath(learningPath);

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
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");

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

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        VBox header = createResultsHeader();
        root.setTop(header);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab summaryTab = new Tab("📊 Summary");
        summaryTab.setContent(createSummaryView());

        Tab learningPathTab = new Tab("📚 Learning Path");
        learningPathTab.setContent(createLearningPathView());

        Tab chartsTab = new Tab("📈 Charts");
        chartsTab.setContent(createChartsView());

        Tab jobsTab = new Tab("💼 Job Postings");
        jobsTab.setContent(createJobPostingsView());

        tabPane.getTabs().addAll(summaryTab, learningPathTab, chartsTab, jobsTab);
        root.setCenter(tabPane);

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

        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox matchBox = createStatBox(
                String.format("%.1f%%", currentResult.getMatchPercentage()),
                "Skill Match",
                "#27ae60"
        );

        VBox matchingBox = createStatBox(
                String.valueOf(currentResult.getMatchingSkillCount()),
                "Skills You Have",
                "#3498db"
        );

        VBox missingBox = createStatBox(
                String.valueOf(currentResult.getMissingSkillCount()),
                "Skills to Learn",
                "#e74c3c"
        );

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
     * Create stat card for learning path
     */
    private VBox createStatCard(String value, String label, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 20px; -fx-background-radius: 10px;");
        card.setPrefWidth(150);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", 14));
        labelText.setStyle("-fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(valueLabel, labelText);
        return card;
    }

    /**
     * Create summary view
     */
    private ScrollPane createSummaryView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        VBox resumeInfo = new VBox(10);
        resumeInfo.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 8px;");

        Label resumeTitle = new Label("📄 Resume Information");
        resumeTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label filename = new Label("File: " + currentResume.getFilename());
        Label skillCount = new Label("Skills found: " + currentResume.getSkillCount());

        resumeInfo.getChildren().addAll(resumeTitle, filename, skillCount);

        VBox matchingSkills = createSkillSection(
                "✅ Skills You Have (Matching Job Requirements)",
                currentResult.getMatchingSkills(),
                "#27ae60"
        );

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
     * Create learning path view with clickable resource links
     */
    private ScrollPane createLearningPathView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("📚 Your Personalized 4-Week Learning Path");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 10px;");

        VBox stat1 = createStatCard(String.valueOf(currentResult.getMissingSkillCount()), "Skills to Learn", "#e74c3c");
        VBox stat2 = createStatCard("4", "Weeks", "#3498db");
        VBox stat3 = createStatCard(String.format("%.0f%%", currentResult.getMatchPercentage()), "Current Match", "#27ae60");

        statsBox.getChildren().addAll(stat1, stat2, stat3);
        content.getChildren().addAll(titleLabel, statsBox);

        List<Skill> missingSkills = currentResult.getMissingSkills();
        int skillsPerWeek = 2;

        for (int week = 1; week <= 4; week++) {
            VBox weekBox = createWeekBoxWithButtons(week, missingSkills, skillsPerWeek);
            content.getChildren().add(weekBox);
        }

        VBox tipsBox = new VBox(15);
        tipsBox.setStyle("-fx-background-color: white; -fx-padding: 25px; -fx-background-radius: 10px;");

        Label tipsTitle = new Label("💡 Learning Tips");
        tipsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label tip1 = new Label("✓ Dedicate 1-2 hours daily to focused learning");
        Label tip2 = new Label("✓ Build at least one project per week to practice");
        Label tip3 = new Label("✓ Join online communities (Reddit, Discord, Stack Overflow)");
        Label tip4 = new Label("✓ Document your learning journey on GitHub");

        tipsBox.getChildren().addAll(tipsTitle, tip1, tip2, tip3, tip4);
        content.getChildren().add(tipsBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Create a week box with clickable resource buttons
     */
    private VBox createWeekBoxWithButtons(int weekNumber, List<Skill> missingSkills, int skillsPerWeek) {
        VBox weekBox = new VBox(15);
        weekBox.setStyle("-fx-background-color: white; -fx-padding: 25px; -fx-background-radius: 10px; " +
                "-fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 10px;");

        Label weekLabel = new Label("📅 WEEK " + weekNumber);
        weekLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        weekLabel.setStyle("-fx-text-fill: #3498db;");

        int startIndex = (weekNumber - 1) * skillsPerWeek;
        int endIndex = Math.min(startIndex + skillsPerWeek, missingSkills.size());

        if (startIndex >= missingSkills.size()) {
            Label reviewLabel = new Label("🎉 Review and practice skills from previous weeks!");
            reviewLabel.setFont(Font.font("System", 16));
            weekBox.getChildren().addAll(weekLabel, reviewLabel);
            return weekBox;
        }

        List<Skill> weekSkills = missingSkills.subList(startIndex, endIndex);

        Label focusLabel = new Label("🎯 Focus Skills:");
        focusLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        FlowPane skillsPane = new FlowPane(10, 10);
        for (Skill skill : weekSkills) {
            Label skillPill = new Label(skill.getName());
            skillPill.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                    "-fx-padding: 8px 16px; -fx-background-radius: 15px; -fx-font-size: 14px;");
            skillsPane.getChildren().add(skillPill);
        }

        weekBox.getChildren().addAll(weekLabel, focusLabel, skillsPane);

        for (Skill skill : weekSkills) {
            VBox resourceBox = new VBox(10);
            resourceBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15px; -fx-background-radius: 8px;");

            Label resourceTitle = new Label("📖 " + skill.getName() + " Learning Resources:");
            resourceTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

            List<LearningResource> resources = getResourcesForSkill(skill.getName());

            if (resources.isEmpty()) {
                resources = getGenericResources(skill.getName());
            }

            VBox buttonBox = new VBox(8);
            for (int i = 0; i < Math.min(3, resources.size()); i++) {
                LearningResource resource = resources.get(i);
                Button resourceButton = new Button("🔗 " + resource.getResourceTitle() + " (" + resource.getPlatform() + ")");
                resourceButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-padding: 10px 20px; -fx-background-radius: 5px; " +
                        "-fx-cursor: hand; -fx-font-size: 13px;");
                resourceButton.setMaxWidth(Double.MAX_VALUE);

                final String url = resource.getResourceUrl();
                resourceButton.setOnAction(e -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception ex) {
                        System.err.println("Could not open URL: " + ex.getMessage());
                    }
                });

                resourceButton.setOnMouseEntered(e ->
                        resourceButton.setStyle("-fx-background-color: #229954; -fx-text-fill: white; " +
                                "-fx-padding: 10px 20px; -fx-background-radius: 5px; " +
                                "-fx-cursor: hand; -fx-font-size: 13px;"));
                resourceButton.setOnMouseExited(e ->
                        resourceButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                "-fx-padding: 10px 20px; -fx-background-radius: 5px; " +
                                "-fx-cursor: hand; -fx-font-size: 13px;"));

                buttonBox.getChildren().add(resourceButton);
            }

            resourceBox.getChildren().addAll(resourceTitle, buttonBox);
            weekBox.getChildren().add(resourceBox);
        }

        Label milestonesLabel = new Label("🏆 Week " + weekNumber + " Milestones:");
        milestonesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        VBox milestonesBox = new VBox(5);
        for (int i = 0; i < weekSkills.size(); i++) {
            Label milestone = new Label((i + 1) + ". Complete " + weekSkills.get(i).getName() + " tutorial and build one project");
            milestone.setStyle("-fx-font-size: 13px;");
            milestonesBox.getChildren().add(milestone);
        }
        Label finalMilestone = new Label((weekSkills.size() + 1) + ". Document your learning and upload to GitHub");
        finalMilestone.setStyle("-fx-font-size: 13px;");
        milestonesBox.getChildren().add(finalMilestone);

        weekBox.getChildren().addAll(milestonesLabel, milestonesBox);

        return weekBox;
    }

    /**
     * Get learning resources for a skill from database
     */
    private List<LearningResource> getResourcesForSkill(String skillName) {
        List<LearningResource> resources = new ArrayList<>();
        try {
            ResultSet rs = databaseManager.getLearningResourcesBySkill(skillName);
            if (rs != null) {
                while (rs.next()) {
                    LearningResource resource = new LearningResource();
                    resource.setResourceTitle(rs.getString("resource_title"));
                    resource.setResourceUrl(rs.getString("resource_url"));
                    resource.setPlatform(rs.getString("platform"));
                    resource.setDifficultyLevel(rs.getString("difficulty_level"));
                    resources.add(resource);
                }
                rs.close();
            }
        } catch (Exception e) {
            System.err.println("Error fetching resources: " + e.getMessage());
        }
        return resources;
    }

    /**
     * Get generic learning resources if not in database
     */
    private List<LearningResource> getGenericResources(String skillName) {
        List<LearningResource> resources = new ArrayList<>();

        LearningResource udemy = new LearningResource();
        udemy.setResourceTitle(skillName + " Complete Course");
        udemy.setResourceUrl("https://www.udemy.com/courses/search/?q=" + skillName.replace(" ", "+"));
        udemy.setPlatform("Udemy");
        resources.add(udemy);

        LearningResource coursera = new LearningResource();
        coursera.setResourceTitle(skillName + " Specialization");
        coursera.setResourceUrl("https://www.coursera.org/search?query=" + skillName.replace(" ", "%20"));
        coursera.setPlatform("Coursera");
        resources.add(coursera);

        LearningResource youtube = new LearningResource();
        youtube.setResourceTitle(skillName + " Tutorial");
        youtube.setResourceUrl("https://www.youtube.com/results?search_query=" + skillName.replace(" ", "+") + "+tutorial");
        youtube.setPlatform("YouTube");
        resources.add(youtube);

        return resources;
    }

    /**
     * Create charts view with spider chart
     */
    private ScrollPane createChartsView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        try {
            JFreeChart barChart = ChartGenerator.createSkillMatchBarChart(currentResult);
            Image barChartImage = ChartGenerator.convertChartToImage(barChart);
            ImageView barChartView = new ImageView(barChartImage);
            barChartView.setPreserveRatio(true);
            barChartView.setFitWidth(750);

            VBox barChartBox = new VBox(10);
            barChartBox.setAlignment(Pos.CENTER);
            barChartBox.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px;");
            barChartBox.getChildren().add(barChartView);

            JFreeChart spiderChart = ChartGenerator.createSkillDistributionSpiderChart(currentResult);
            Image spiderChartImage = ChartGenerator.convertChartToImage(spiderChart);
            ImageView spiderChartView = new ImageView(spiderChartImage);
            spiderChartView.setPreserveRatio(true);
            spiderChartView.setFitWidth(750);

            VBox spiderChartBox = new VBox(10);
            spiderChartBox.setAlignment(Pos.CENTER);
            spiderChartBox.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px;");
            spiderChartBox.getChildren().add(spiderChartView);

            JFreeChart topSkillsChart = ChartGenerator.createTopMissingSkillsChart(currentResult);
            Image topSkillsImage = ChartGenerator.convertChartToImage(topSkillsChart);
            ImageView topSkillsView = new ImageView(topSkillsImage);
            topSkillsView.setPreserveRatio(true);
            topSkillsView.setFitWidth(750);

            VBox topSkillsBox = new VBox(10);
            topSkillsBox.setAlignment(Pos.CENTER);
            topSkillsBox.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px;");
            topSkillsBox.getChildren().add(topSkillsView);

            content.getChildren().addAll(barChartBox, spiderChartBox, topSkillsBox);

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
     * Create job postings view - Shows analyzed jobs
     */
    private ScrollPane createJobPostingsView() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label headerLabel = new Label("💼 Analyzed Job Postings (" + currentResult.getTotalJobsAnalyzed() + " jobs)");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        headerLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label infoLabel = new Label("Showing top 35 most relevant jobs analyzed to determine your skill match:");
        infoLabel.setFont(Font.font("System", 14));
        infoLabel.setStyle("-fx-text-fill: #7f8c8d;");

        content.getChildren().addAll(headerLabel, infoLabel);

        List<Job> jobs = currentResult.getAnalyzedJobs();
        int displayCount = Math.min(35, jobs.size());

        for (int i = 0; i < displayCount; i++) {
            Job job = jobs.get(i);
            VBox jobCard = createJobCard(job, i + 1);
            content.getChildren().add(jobCard);
        }

        if (jobs.size() > 35) {
            Label moreLabel = new Label("... and " + (jobs.size() - 35) + " more jobs analyzed");
            moreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            moreLabel.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 10px;");
            content.getChildren().add(moreLabel);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * Create a job card for display
     */
    private VBox createJobCard(Job job, int index) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20px; " +
                "-fx-background-radius: 8px; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8px; -fx-border-width: 1px;");

        Label titleLabel = new Label(index + ". " + job.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label companyLabel = new Label("🏢 " + job.getCompany());
        companyLabel.setFont(Font.font("System", 14));
        companyLabel.setStyle("-fx-text-fill: #3498db;");

        Label locationLabel = new Label("📍 " + job.getLocation());
        locationLabel.setFont(Font.font("System", 13));
        locationLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Label skillsHeaderLabel = new Label("Required Skills:");
        skillsHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        FlowPane skillsFlow = new FlowPane(8, 8);
        List<Skill> requiredSkills = job.getRequiredSkills();

        for (int i = 0; i < Math.min(8, requiredSkills.size()); i++) {
            Skill skill = requiredSkills.get(i);

            boolean userHasSkill = currentResume.hasSkill(skill.getName());
            String skillColor = userHasSkill ? "#27ae60" : "#e74c3c";
            String skillIcon = userHasSkill ? "✓ " : "✗ ";

            Label skillLabel = new Label(skillIcon + skill.getName());
            skillLabel.setStyle("-fx-background-color: " + skillColor + "; -fx-text-fill: white; " +
                    "-fx-padding: 5px 12px; -fx-background-radius: 12px; -fx-font-size: 12px;");
            skillsFlow.getChildren().add(skillLabel);
        }

        if (requiredSkills.size() > 8) {
            Label moreSkills = new Label("+" + (requiredSkills.size() - 8) + " more");
            moreSkills.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
            skillsFlow.getChildren().add(moreSkills);
        }

        int matchCount = 0;
        for (Skill skill : requiredSkills) {
            if (currentResume.hasSkill(skill.getName())) {
                matchCount++;
            }
        }
        double jobMatchPercentage = requiredSkills.isEmpty() ? 0 :
                (matchCount * 100.0 / requiredSkills.size());

        Label matchLabel = new Label(String.format("Your Match: %.0f%%", jobMatchPercentage));
        matchLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        matchLabel.setStyle("-fx-text-fill: " + (jobMatchPercentage >= 70 ? "#27ae60" :
                jobMatchPercentage >= 40 ? "#f39c12" : "#e74c3c") + ";");

        Button applyButton = new Button("🔗 View Job");
        applyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 8px 16px; -fx-background-radius: 5px;");
        applyButton.setOnAction(e -> {
            if (job.getUrl() != null && !job.getUrl().isEmpty()) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(job.getUrl()));
                } catch (Exception ex) {
                    System.err.println("Could not open URL: " + ex.getMessage());
                }
            }
        });

        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.getChildren().addAll(matchLabel, applyButton);

        card.getChildren().addAll(titleLabel, companyLabel, locationLabel,
                skillsHeaderLabel, skillsFlow, bottomBox);

        return card;
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