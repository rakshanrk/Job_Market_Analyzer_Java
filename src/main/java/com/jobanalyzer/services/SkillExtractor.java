package com.jobanalyzer.services;

import com.jobanalyzer.models.Skill;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * SkillExtractor Service Class
 *
 * Uses Apache OpenNLP to extract skills from text
 * Combines NLP with a predefined skill dictionary
 */
public class SkillExtractor {

    // OpenNLP models
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;

    // Predefined skill dictionary (technical skills commonly found in job postings)
    private static final Set<String> SKILL_DICTIONARY = new HashSet<>(Arrays.asList(
            // Programming Languages
            "java", "python", "javascript", "c++", "c#", "csharp", "ruby", "php", "swift", "kotlin",
            "typescript", "go", "golang", "rust", "scala", "r", "matlab", "perl", "objective-c",

            // Web Technologies
            "html", "html5", "css", "css3", "react", "reactjs", "angular", "vue", "vuejs",
            "node.js", "nodejs", "express", "expressjs", "django", "flask", "spring", "spring boot",
            "hibernate", "jquery", "bootstrap", "sass", "less", "webpack", "redux", "nextjs",

            // Databases
            "sql", "mysql", "postgresql", "postgres", "mongodb", "oracle", "redis", "cassandra",
            "sqlite", "dynamodb", "firebase", "elasticsearch", "mariadb", "nosql",

            // Cloud & DevOps
            "aws", "amazon web services", "azure", "microsoft azure", "gcp", "google cloud",
            "docker", "kubernetes", "k8s", "jenkins", "terraform", "ansible", "git", "github",
            "gitlab", "bitbucket", "ci/cd", "linux", "unix", "bash", "shell scripting",

            // Data Science & ML
            "machine learning", "deep learning", "tensorflow", "pytorch", "scikit-learn",
            "pandas", "numpy", "data analysis", "data science", "statistics", "nlp",
            "natural language processing", "computer vision", "keras", "matplotlib",

            // Mobile Development
            "android", "ios", "react native", "flutter", "xamarin", "kotlin", "swift",

            // Backend/API
            "rest api", "restful", "graphql", "microservices", "api development", "soap",

            // Testing & QA
            "testing", "selenium", "junit", "pytest", "testng", "cucumber", "jest",
            "unit testing", "integration testing", "test automation",

            // Project Management & Methodologies
            "agile", "scrum", "kanban", "jira", "trello", "project management",

            // Other Technical Skills
            "maven", "gradle", "npm", "yarn", "hadoop", "spark", "kafka", "rabbitmq",
            "nginx", "apache", "tomcat", "networking", "security", "cybersecurity",
            "blockchain", "devops", "dataops", "mlops"
    ));

    // Words to EXCLUDE (common non-skill words)
    private static final Set<String> EXCLUDED_WORDS = new HashSet<>(Arrays.asList(
            // Common verbs
            "access", "build", "building", "create", "creating", "develop", "developing",
            "design", "designing", "implement", "implementing", "manage", "managing",
            "analyze", "analyzing", "work", "working", "support", "supporting",

            // Time/Date related
            "date", "time", "day", "week", "month", "year", "schedule", "calendar",

            // Generic business words
            "customer", "client", "business", "company", "team", "project", "product",
            "service", "services", "solution", "solutions", "system", "systems",
            "application", "applications", "platform", "platforms",

            // Academic/General
            "school", "university", "college", "education", "training", "learning",
            "course", "class", "student", "teacher", "intern", "internship",

            // Medical (unless targeting medical jobs)
            "patient", "medical", "health", "healthcare", "hospital", "clinic",
            "doctor", "nurse", "radiology", "neuron", "medicine",

            // Location words
            "place", "location", "nation", "country", "county", "city", "state",

            // Generic descriptors
            "model", "tool", "tools", "description", "knowledge", "interest",
            "expertise", "capability", "capabilities", "skill", "skills",

            // Random nouns that aren't skills
            "electronics", "people", "thing", "things", "way", "ways", "part", "parts"
    ));

    /**
     * Constructor - loads OpenNLP models
     */
    public SkillExtractor() {
        loadModels();
    }

    /**
     * Load OpenNLP models from resources
     */
    private void loadModels() {
        try {
            System.out.println("📚 Loading OpenNLP models...");

            // Load Tokenizer model
            InputStream tokenizerStream = getClass().getResourceAsStream("/models/en-token.bin");
            if (tokenizerStream == null) {
                // Try loading from file system
                tokenizerStream = new FileInputStream("src/main/resources/models/en-token.bin");
            }
            TokenizerModel tokenizerModel = new TokenizerModel(tokenizerStream);
            tokenizer = new TokenizerME(tokenizerModel);
            tokenizerStream.close();
            System.out.println("  ✅ Tokenizer model loaded");

            // Load POS Tagger model
            InputStream posStream = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
            if (posStream == null) {
                // Try loading from file system
                posStream = new FileInputStream("src/main/resources/models/en-pos-maxent.bin");
            }
            POSModel posModel = new POSModel(posStream);
            posTagger = new POSTaggerME(posModel);
            posStream.close();
            System.out.println("  ✅ POS Tagger model loaded");

            System.out.println("✅ OpenNLP models loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ Error loading OpenNLP models!");
            System.err.println("   Make sure model files are in: src/main/resources/models/");
            e.printStackTrace();
        }
    }

    /**
     * Extract skills from text
     * @param text Text to analyze (resume or job description)
     * @return List of found skills
     */
    public List<Skill> extractSkills(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        System.out.println("\n🔍 Extracting skills from text...");

        // Normalize text (lowercase for comparison)
        String normalizedText = text.toLowerCase();

        // Map to track found skills and their frequencies
        Map<String, Skill> skillMap = new HashMap<>();

        // Method 1: Dictionary-based extraction (simple but effective)
        for (String skillName : SKILL_DICTIONARY) {
            if (normalizedText.contains(skillName)) {
                // IMPORTANT: Skip if it's in excluded words
                if (!EXCLUDED_WORDS.contains(skillName)) {
                    Skill skill = new Skill(capitalizeFirst(skillName));
                    skill.setCategory("Technical");
                    skill.setTechnical(true);

                    // Count frequency
                    int count = countOccurrences(normalizedText, skillName);
                    skill.setFrequency(count);

                    skillMap.put(skillName, skill);
                }
            }
        }

        // Method 2: NLP-based extraction (finds nouns that might be skills)
        if (tokenizer != null && posTagger != null) {
            extractSkillsUsingNLP(normalizedText, skillMap);
        }

        // Convert map to list
        List<Skill> skills = new ArrayList<>(skillMap.values());

        // Sort by frequency (most common skills first)
        skills.sort((s1, s2) -> Integer.compare(s2.getFrequency(), s1.getFrequency()));

        System.out.println("✅ Found " + skills.size() + " unique skills");

        return skills;
    }

    /**
     * Extract skills using NLP (tokenization and POS tagging)
     * @param text Normalized text
     * @param skillMap Map to add found skills to
     */
    private void extractSkillsUsingNLP(String text, Map<String, Skill> skillMap) {
        try {
            // Tokenize text
            String[] tokens = tokenizer.tokenize(text);

            // Get POS tags
            String[] tags = posTagger.tag(tokens);

            // Look for nouns (potential skills)
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].toLowerCase();
                String tag = tags[i];

                // Check if it's a noun (NN, NNS, NNP, NNPS)
                if (tag.startsWith("NN")) {
                    // Check if it looks like a skill (length > 2, contains letters)
                    if (token.length() > 2 && token.matches("[a-z]+")) {
                        // IMPORTANT: Skip if in excluded words or common words
                        if (!skillMap.containsKey(token) &&
                                !isCommonWord(token) &&
                                !EXCLUDED_WORDS.contains(token)) {

                            // Only add if it's in the dictionary
                            if (SKILL_DICTIONARY.contains(token)) {
                                Skill skill = new Skill(capitalizeFirst(token));
                                skill.setCategory("Technical");
                                skill.setTechnical(true);
                                skill.setFrequency(1);
                                skillMap.put(token, skill);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error in NLP extraction: " + e.getMessage());
        }
    }

    /**
     * Count how many times a skill appears in text
     * @param text Text to search
     * @param skill Skill to count
     * @return Number of occurrences
     */
    private int countOccurrences(String text, String skill) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(skill, index)) != -1) {
            count++;
            index += skill.length();
        }

        return count;
    }

    /**
     * Capitalize first letter of a string
     * @param str String to capitalize
     * @return Capitalized string
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Check if word is a common English word (not a skill)
     * @param word Word to check
     * @return true if it's a common word
     */
    private boolean isCommonWord(String word) {
        Set<String> commonWords = new HashSet<>(Arrays.asList(
                "the", "and", "for", "with", "from", "this", "that", "have", "has",
                "been", "were", "will", "would", "should", "could", "may", "can",
                "work", "job", "company", "team", "project", "experience", "year",
                "years", "required", "preferred", "including", "responsibilities"
        ));
        return commonWords.contains(word);
    }

    /**
     * Get the skill dictionary size
     * @return Number of skills in dictionary
     */
    public int getDictionarySize() {
        return SKILL_DICTIONARY.size();
    }

    /**
     * Test skill extractor
     */
    public static void testExtractor() {
        System.out.println("\n=== Testing SkillExtractor ===");

        SkillExtractor extractor = new SkillExtractor();

        // Test with sample text
        String sampleText = "We are looking for a Java developer with experience in Spring, " +
                "SQL databases, and AWS cloud. Knowledge of Docker and React is a plus.";

        System.out.println("\nSample text: " + sampleText);
        System.out.println("\nExtracting skills...");

        List<Skill> skills = extractor.extractSkills(sampleText);

        System.out.println("\nFound skills:");
        for (Skill skill : skills) {
            System.out.println("  - " + skill.getName() + " (frequency: " + skill.getFrequency() + ")");
        }

        System.out.println("\nSkill dictionary contains " + extractor.getDictionarySize() + " predefined skills");

        System.out.println("\n=== SkillExtractor Test Complete ===\n");
    }
}