package com.jobanalyzer.services;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DatabaseManager - Handles all SQLite database operations
 *
 * This class manages:
 * - Database connection
 * - Table creation
 * - CRUD operations for learning resources, skills, and analysis history
 */
public class DatabaseManager {

    // Database file path - stored in the 'data' folder
    private static final String DB_URL = "jdbc:sqlite:data/jobanalyzer.db";

    // Singleton instance
    private static DatabaseManager instance;
    private Connection connection;

    /**
     * Private constructor (Singleton pattern)
     * Initializes database connection and creates tables if they don't exist
     */
    private DatabaseManager() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("✅ Database connection established: " + DB_URL);

            // Create tables
            createTables();

        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database!");
            e.printStackTrace();
        }
    }

    /**
     * Get singleton instance of DatabaseManager
     * @return DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Create all required database tables
     */
    private void createTables() {
        try {
            Statement stmt = connection.createStatement();

            // Table 1: Learning Resources
            // Stores available courses/resources mapped to skills
            String createLearningResourcesTable = """
                CREATE TABLE IF NOT EXISTS learning_resources (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    skill_name TEXT NOT NULL,
                    resource_title TEXT NOT NULL,
                    resource_type TEXT NOT NULL,
                    resource_url TEXT NOT NULL,
                    platform TEXT NOT NULL,
                    duration_weeks INTEGER,
                    difficulty_level TEXT,
                    description TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createLearningResourcesTable);

            // Table 2: Skills Master List
            // Stores all known skills in the system
            String createSkillsTable = """
                CREATE TABLE IF NOT EXISTS skills_master (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    skill_name TEXT UNIQUE NOT NULL,
                    category TEXT,
                    is_technical BOOLEAN DEFAULT 1,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createSkillsTable);

            // Table 3: Analysis History
            // Stores past resume analyses and results
            String createAnalysisHistoryTable = """
                CREATE TABLE IF NOT EXISTS analysis_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_name TEXT,
                    resume_filename TEXT NOT NULL,
                    extracted_skills TEXT,
                    missing_skills TEXT,
                    match_percentage REAL,
                    jobs_analyzed INTEGER,
                    analysis_date TEXT NOT NULL,
                    learning_path_generated BOOLEAN DEFAULT 0
                )
            """;
            stmt.execute(createAnalysisHistoryTable);

            // Table 4: Learning Path Details
            // Stores generated 4-week learning plans
            String createLearningPathTable = """
                CREATE TABLE IF NOT EXISTS learning_paths (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    analysis_id INTEGER,
                    week_number INTEGER,
                    skill_focus TEXT,
                    resources TEXT,
                    milestones TEXT,
                    FOREIGN KEY (analysis_id) REFERENCES analysis_history(id)
                )
            """;
            stmt.execute(createLearningPathTable);

            stmt.close();
            System.out.println("✅ Database tables created successfully!");

            // Insert sample learning resources
            insertSampleResources();

        } catch (SQLException e) {
            System.err.println("❌ Error creating tables!");
            e.printStackTrace();
        }
    }

    /**
     * Insert sample learning resources into database
     * These map common skills to real learning resources
     */
    /**
     * Insert sample learning resources into database with REAL course links
     */
    private void insertSampleResources() {
        try {
            // Check if resources already exist
            String checkQuery = "SELECT COUNT(*) FROM learning_resources";
            Statement checkStmt = connection.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkQuery);

            if (rs.next() && rs.getInt(1) > 0) {
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();

            String insertSQL = """
            INSERT INTO learning_resources 
            (skill_name, resource_title, resource_type, resource_url, platform, duration_weeks, difficulty_level, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

            PreparedStatement pstmt = connection.prepareStatement(insertSQL);

            // REAL resources with actual working links
            Object[][] resources = {
                    // Java
                    {"Java", "Java Programming Masterclass", "Course", "https://www.udemy.com/course/java-the-complete-java-developer-course/", "Udemy", 12, "Beginner", "Complete Java from zero to hero"},
                    {"Java", "Java Programming and Software Engineering", "Course", "https://www.coursera.org/specializations/java-programming", "Coursera", 10, "Beginner", "Duke University Java course"},

                    // Python
                    {"Python", "Python for Everybody", "Course", "https://www.coursera.org/specializations/python", "Coursera", 8, "Beginner", "University of Michigan Python course"},
                    {"Python", "Complete Python Bootcamp", "Course", "https://www.udemy.com/course/complete-python-bootcamp/", "Udemy", 10, "Beginner", "Go from zero to hero in Python"},

                    // JavaScript
                    {"JavaScript", "The Complete JavaScript Course", "Course", "https://www.udemy.com/course/the-complete-javascript-course/", "Udemy", 12, "Beginner", "Modern JavaScript from scratch"},
                    {"JavaScript", "JavaScript Algorithms and Data Structures", "Course", "https://www.freecodecamp.org/learn/javascript-algorithms-and-data-structures/", "freeCodeCamp", 6, "Intermediate", "Free JavaScript certification"},

                    // React
                    {"React", "React - The Complete Guide", "Course", "https://www.udemy.com/course/react-the-complete-guide-incl-redux/", "Udemy", 10, "Intermediate", "React, Hooks, Redux, React Router"},
                    {"React", "Full-Stack Web Development with React", "Course", "https://www.coursera.org/specializations/full-stack-react", "Coursera", 12, "Intermediate", "Hong Kong University course"},

                    // Node.js
                    {"Node.js", "Node.js - The Complete Guide", "Course", "https://www.udemy.com/course/nodejs-the-complete-guide/", "Udemy", 10, "Intermediate", "Build REST APIs with Node.js"},
                    {"Node.js", "Server-side Development with NodeJS", "Course", "https://www.coursera.org/learn/server-side-nodejs", "Coursera", 4, "Intermediate", "Express framework and MongoDB"},

                    // Angular
                    {"Angular", "Angular - The Complete Guide", "Course", "https://www.udemy.com/course/the-complete-guide-to-angular-2/", "Udemy", 12, "Intermediate", "Master Angular and build apps"},

                    // Machine Learning
                    {"Machine Learning", "Machine Learning Specialization", "Course", "https://www.coursera.org/specializations/machine-learning-introduction", "Coursera", 12, "Intermediate", "Andrew Ng's famous ML course"},
                    {"Machine Learning", "Machine Learning A-Z", "Course", "https://www.udemy.com/course/machinelearning/", "Udemy", 10, "Beginner", "Hands-on Python & R in ML"},

                    // Data Science
                    {"Data Science", "IBM Data Science Professional Certificate", "Course", "https://www.coursera.org/professional-certificates/ibm-data-science", "Coursera", 16, "Beginner", "Complete data science program"},
                    {"Data Science", "Data Science Bootcamp", "Course", "https://www.udemy.com/course/the-data-science-course-complete-data-science-bootcamp/", "Udemy", 14, "Beginner", "Statistics, Python, ML, Deep Learning"},

                    // SQL
                    {"SQL", "The Complete SQL Bootcamp", "Course", "https://www.udemy.com/course/the-complete-sql-bootcamp/", "Udemy", 4, "Beginner", "Master SQL with PostgreSQL"},
                    {"SQL", "SQL for Data Science", "Course", "https://www.coursera.org/learn/sql-for-data-science", "Coursera", 4, "Beginner", "UC Davis SQL course"},

                    // Docker
                    {"Docker", "Docker Mastery", "Course", "https://www.udemy.com/course/docker-mastery/", "Udemy", 6, "Intermediate", "Docker, Compose, and Swarm"},
                    {"Docker", "Docker Tutorial for Beginners", "Video", "https://www.youtube.com/watch?v=fqMOX6JJhGo", "YouTube", 1, "Beginner", "Free Docker crash course"},

                    // Kubernetes
                    {"Kubernetes", "Kubernetes for Beginners", "Course", "https://www.udemy.com/course/learn-kubernetes/", "Udemy", 6, "Intermediate", "Deploy applications with K8s"},

                    // AWS
                    {"AWS", "AWS Certified Solutions Architect", "Course", "https://www.udemy.com/course/aws-certified-solutions-architect-associate-saa-c03/", "Udemy", 12, "Intermediate", "Prepare for AWS certification"},
                    {"AWS", "AWS Fundamentals", "Course", "https://www.coursera.org/specializations/aws-fundamentals", "Coursera", 8, "Beginner", "Amazon Web Services basics"},

                    // Git
                    {"Git", "Git Complete: The Definitive Guide", "Course", "https://www.udemy.com/course/git-complete/", "Udemy", 3, "Beginner", "Master Git and GitHub"},
                    {"Git", "Version Control with Git", "Course", "https://www.coursera.org/learn/version-control-with-git", "Coursera", 3, "Beginner", "Atlassian Git course"},

                    // Spring Boot
                    {"Spring Boot", "Spring Boot Masterclass", "Course", "https://www.udemy.com/course/spring-hibernate-tutorial/", "Udemy", 10, "Intermediate", "Spring Boot, Spring MVC, Hibernate"},

                    // MongoDB
                    {"MongoDB", "MongoDB - The Complete Developer's Guide", "Course", "https://www.udemy.com/course/mongodb-the-complete-developers-guide/", "Udemy", 6, "Intermediate", "NoSQL with MongoDB"},

                    // TypeScript
                    {"TypeScript", "Understanding TypeScript", "Course", "https://www.udemy.com/course/understanding-typescript/", "Udemy", 8, "Intermediate", "Master TypeScript"},

                    // Django
                    {"Django", "Python Django - The Practical Guide", "Course", "https://www.udemy.com/course/python-django-the-practical-guide/", "Udemy", 10, "Intermediate", "Build web apps with Django"},

                    // Flask
                    {"Flask", "REST APIs with Flask and Python", "Course", "https://www.udemy.com/course/rest-api-flask-and-python/", "Udemy", 6, "Intermediate", "Build REST APIs with Flask"},

                    // Android
                    {"Android", "The Complete Android Development Course", "Course", "https://www.udemy.com/course/complete-android-n-developer-course/", "Udemy", 12, "Beginner", "Build Android apps from scratch"},

                    // iOS
                    {"iOS", "iOS & Swift - The Complete iOS App Development", "Course", "https://www.udemy.com/course/ios-13-app-development-bootcamp/", "Udemy", 12, "Beginner", "Build iOS apps with Swift"},

                    // Flutter
                    {"Flutter", "Flutter & Dart - The Complete Guide", "Course", "https://www.udemy.com/course/learn-flutter-dart-to-build-ios-android-apps/", "Udemy", 10, "Intermediate", "Cross-platform mobile development"},

                    // React Native
                    {"React Native", "The Complete React Native + Hooks Course", "Course", "https://www.udemy.com/course/the-complete-react-native-and-redux-course/", "Udemy", 10, "Intermediate", "Build iOS and Android apps"},

                    // TensorFlow
                    {"TensorFlow", "TensorFlow Developer Certificate", "Course", "https://www.coursera.org/professional-certificates/tensorflow-in-practice", "Coursera", 12, "Intermediate", "Official TensorFlow certification"},

                    // PyTorch
                    {"PyTorch", "PyTorch for Deep Learning", "Course", "https://www.udemy.com/course/pytorch-for-deep-learning-with-python-bootcamp/", "Udemy", 10, "Intermediate", "Deep learning with PyTorch"},

                    // Statistics
                    {"Statistics", "Statistics with Python", "Course", "https://www.coursera.org/specializations/statistics-with-python", "Coursera", 12, "Intermediate", "University of Michigan course"},

                    // Linux
                    {"Linux", "Linux Mastery", "Course", "https://www.udemy.com/course/linux-mastery/", "Udemy", 6, "Beginner", "Master the Linux command line"},

                    // C++
                    {"C++", "Beginning C++ Programming", "Course", "https://www.udemy.com/course/beginning-c-plus-plus-programming/", "Udemy", 10, "Beginner", "Modern C++ from scratch"},

                    // Go
                    {"Go", "Learn Go Programming", "Course", "https://www.udemy.com/course/learn-how-to-code/", "Udemy", 8, "Beginner", "Google's Go language"},

                    // Rust
                    {"Rust", "The Rust Programming Language", "Book", "https://doc.rust-lang.org/book/", "Rust Official", 8, "Intermediate", "Official Rust book - Free"}
            };

            for (Object[] resource : resources) {
                pstmt.setString(1, (String) resource[0]);
                pstmt.setString(2, (String) resource[1]);
                pstmt.setString(3, (String) resource[2]);
                pstmt.setString(4, (String) resource[3]);
                pstmt.setString(5, (String) resource[4]);
                pstmt.setInt(6, (Integer) resource[5]);
                pstmt.setString(7, (String) resource[6]);
                pstmt.setString(8, (String) resource[7]);
                pstmt.executeUpdate();
            }

            pstmt.close();
            System.out.println("✅ Sample learning resources with real links inserted successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error inserting sample resources!");
            e.printStackTrace();
        }
    }

    /**
     * Get learning resources for a specific skill
     * @param skillName The skill to search for
     * @return ResultSet containing matching resources
     */
    public ResultSet getLearningResourcesBySkill(String skillName) {
        try {
            String query = "SELECT * FROM learning_resources WHERE LOWER(skill_name) = LOWER(?) ORDER BY difficulty_level";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, skillName);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("❌ Error fetching learning resources!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save analysis result to history
     * @param userName User's name (optional)
     * @param resumeFilename Name of uploaded resume file
     * @param extractedSkills Comma-separated list of found skills
     * @param missingSkills Comma-separated list of missing skills
     * @param matchPercentage Skill match percentage
     * @param jobsAnalyzed Number of jobs analyzed
     * @return Generated analysis ID
     */
    public int saveAnalysisHistory(String userName, String resumeFilename,
                                   String extractedSkills, String missingSkills,
                                   double matchPercentage, int jobsAnalyzed) {
        try {
            String insertSQL = """
                INSERT INTO analysis_history 
                (user_name, resume_filename, extracted_skills, missing_skills, 
                 match_percentage, jobs_analyzed, analysis_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement pstmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, userName);
            pstmt.setString(2, resumeFilename);
            pstmt.setString(3, extractedSkills);
            pstmt.setString(4, missingSkills);
            pstmt.setDouble(5, matchPercentage);
            pstmt.setInt(6, jobsAnalyzed);
            pstmt.setString(7, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            int analysisId = -1;
            if (rs.next()) {
                analysisId = rs.getInt(1);
            }

            rs.close();
            pstmt.close();

            System.out.println("✅ Analysis history saved with ID: " + analysisId);
            return analysisId;

        } catch (SQLException e) {
            System.err.println("❌ Error saving analysis history!");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Save learning path details
     * @param analysisId ID of the analysis
     * @param weekNumber Week number (1-4)
     * @param skillFocus Skills to focus on this week
     * @param resources Resources for this week
     * @param milestones Goals to achieve this week
     */
    public void saveLearningPath(int analysisId, int weekNumber,
                                 String skillFocus, String resources, String milestones) {
        try {
            String insertSQL = """
                INSERT INTO learning_paths 
                (analysis_id, week_number, skill_focus, resources, milestones)
                VALUES (?, ?, ?, ?, ?)
            """;

            PreparedStatement pstmt = connection.prepareStatement(insertSQL);
            pstmt.setInt(1, analysisId);
            pstmt.setInt(2, weekNumber);
            pstmt.setString(3, skillFocus);
            pstmt.setString(4, resources);
            pstmt.setString(5, milestones);

            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Error saving learning path!");
            e.printStackTrace();
        }
    }

    /**
     * Get all analysis history
     * @return ResultSet containing all past analyses
     */
    public ResultSet getAllAnalysisHistory() {
        try {
            String query = "SELECT * FROM analysis_history ORDER BY analysis_date DESC";
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("❌ Error fetching analysis history!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection!");
            e.printStackTrace();
        }
    }

    /**
     * Test database operations
     */
    public static void testDatabase() {
        System.out.println("\n=== Testing Database Operations ===");

        DatabaseManager db = DatabaseManager.getInstance();

        // Test 1: Get learning resources for Java
        System.out.println("\n📚 Learning resources for 'Java':");
        ResultSet rs = db.getLearningResourcesBySkill("Java");
        try {
            while (rs != null && rs.next()) {
                System.out.println("  - " + rs.getString("resource_title") +
                        " (" + rs.getString("platform") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Test 2: Save sample analysis
        int analysisId = db.saveAnalysisHistory(
                "Test User",
                "test_resume.pdf",
                "Java, Python, SQL",
                "React, Docker, AWS",
                65.5,
                50
        );
        System.out.println("\n💾 Sample analysis saved with ID: " + analysisId);

        // Test 3: Save learning path
        db.saveLearningPath(analysisId, 1, "React Basics",
                "Coursera React Course", "Complete React fundamentals");
        System.out.println("📖 Sample learning path saved");

        System.out.println("\n=== Database Test Complete ===\n");
    }
}