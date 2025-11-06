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
    private void insertSampleResources() {
        try {
            // Check if resources already exist
            String checkQuery = "SELECT COUNT(*) FROM learning_resources";
            Statement checkStmt = connection.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkQuery);

            if (rs.next() && rs.getInt(1) > 0) {
                // Resources already exist, skip insertion
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();

            // Prepare insert statement
            String insertSQL = """
                INSERT INTO learning_resources 
                (skill_name, resource_title, resource_type, resource_url, platform, duration_weeks, difficulty_level, description)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement pstmt = connection.prepareStatement(insertSQL);

            // Sample resources for common skills
            Object[][] resources = {
                    // Java
                    {"Java", "Java Programming Masterclass", "Course", "https://www.coursera.org/learn/java-programming", "Coursera", 8, "Beginner", "Complete Java programming from basics to advanced"},
                    {"Java", "Effective Java Practice", "GitHub Project", "https://github.com/topics/java-practice", "GitHub", 4, "Intermediate", "Hands-on Java coding exercises"},

                    // Python
                    {"Python", "Python for Everybody", "Course", "https://www.coursera.org/specializations/python", "Coursera", 8, "Beginner", "Learn Python programming fundamentals"},
                    {"Python", "Python Projects for Beginners", "GitHub Project", "https://github.com/topics/python-projects", "GitHub", 4, "Beginner", "Practice Python with real projects"},

                    // JavaScript
                    {"JavaScript", "The Complete JavaScript Course", "Course", "https://www.udemy.com/course/the-complete-javascript-course/", "Udemy", 12, "Beginner", "Modern JavaScript from beginner to advanced"},
                    {"JavaScript", "JavaScript30", "GitHub Project", "https://github.com/wesbos/JavaScript30", "GitHub", 4, "Intermediate", "30 day vanilla JS coding challenge"},

                    // React
                    {"React", "React - The Complete Guide", "Course", "https://www.udemy.com/course/react-the-complete-guide/", "Udemy", 10, "Intermediate", "Master React with hooks, Redux, and more"},
                    {"React", "React Projects Collection", "GitHub Project", "https://github.com/topics/react-projects", "GitHub", 6, "Intermediate", "Build real-world React applications"},

                    // Machine Learning
                    {"Machine Learning", "Machine Learning Specialization", "Course", "https://www.coursera.org/specializations/machine-learning-introduction", "Coursera", 12, "Intermediate", "Learn ML fundamentals from Andrew Ng"},
                    {"Machine Learning", "ML Projects Repository", "GitHub Project", "https://github.com/topics/machine-learning-projects", "GitHub", 8, "Advanced", "Implement ML algorithms from scratch"},

                    // SQL
                    {"SQL", "SQL for Data Science", "Course", "https://www.coursera.org/learn/sql-for-data-science", "Coursera", 4, "Beginner", "Master SQL queries and database design"},
                    {"SQL", "SQL Practice Problems", "GitHub Project", "https://github.com/topics/sql-practice", "GitHub", 2, "Beginner", "Practice SQL with real datasets"},

                    // Docker
                    {"Docker", "Docker Mastery", "Course", "https://www.udemy.com/course/docker-mastery/", "Udemy", 6, "Intermediate", "Learn Docker, Compose, and Swarm"},
                    {"Docker", "Docker Examples", "GitHub Project", "https://github.com/docker/awesome-compose", "GitHub", 3, "Intermediate", "Docker Compose examples"},

                    // AWS
                    {"AWS", "AWS Certified Solutions Architect", "Course", "https://www.coursera.org/learn/aws-certified-solutions-architect-associate", "Coursera", 10, "Intermediate", "Prepare for AWS certification"},
                    {"AWS", "AWS Projects", "GitHub Project", "https://github.com/topics/aws-projects", "GitHub", 8, "Advanced", "Deploy applications on AWS"},

                    // Git
                    {"Git", "Git & GitHub Complete Guide", "Course", "https://www.udemy.com/course/git-and-github-complete-guide/", "Udemy", 3, "Beginner", "Master version control with Git"},
                    {"Git", "Git Exercises", "GitHub Project", "https://github.com/topics/git-exercises", "GitHub", 2, "Beginner", "Practice Git commands"},

                    // Data Structures
                    {"Data Structures", "Data Structures and Algorithms", "Course", "https://www.coursera.org/specializations/data-structures-algorithms", "Coursera", 16, "Intermediate", "Master DSA fundamentals"},
                    {"Data Structures", "DSA Practice", "GitHub Project", "https://github.com/TheAlgorithms/Java", "GitHub", 12, "Intermediate", "Implement data structures"}
            };

            // Insert all resources
            for (Object[] resource : resources) {
                pstmt.setString(1, (String) resource[0]);  // skill_name
                pstmt.setString(2, (String) resource[1]);  // resource_title
                pstmt.setString(3, (String) resource[2]);  // resource_type
                pstmt.setString(4, (String) resource[3]);  // resource_url
                pstmt.setString(5, (String) resource[4]);  // platform
                pstmt.setInt(6, (Integer) resource[5]);     // duration_weeks
                pstmt.setString(7, (String) resource[6]);  // difficulty_level
                pstmt.setString(8, (String) resource[7]);  // description
                pstmt.executeUpdate();
            }

            pstmt.close();
            System.out.println("✅ Sample learning resources inserted successfully!");

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