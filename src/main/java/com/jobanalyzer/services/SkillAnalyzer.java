package com.jobanalyzer.services;

import com.jobanalyzer.models.AnalysisResult;
import com.jobanalyzer.models.Job;
import com.jobanalyzer.models.Resume;
import com.jobanalyzer.models.Skill;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * SkillAnalyzer Service Class
 *
 * Uses Weka K-Means clustering to analyze skill gaps
 * Compares user's resume skills with job market requirements
 */
public class SkillAnalyzer {

    private DatabaseManager databaseManager;

    /**
     * Constructor
     */
    public SkillAnalyzer() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    /**
     * Analyze resume against job market
     * @param resume User's resume with extracted skills
     * @param jobs List of jobs from the market
     * @return Complete analysis result
     */
    public AnalysisResult analyzeSkills(Resume resume, List<Job> jobs) {
        System.out.println("\n🔬 Starting skill analysis...");
        System.out.println("   Resume skills: " + resume.getSkillCount());
        System.out.println("   Jobs to analyze: " + jobs.size());

        // Create result object
        AnalysisResult result = new AnalysisResult(resume);
        result.setAnalyzedJobs(jobs);
        result.setTotalJobsAnalyzed(jobs.size());

        // Step 1: Collect all unique skills from jobs
        Set<String> allJobSkills = collectAllJobSkills(jobs);
        System.out.println("   Unique skills in job market: " + allJobSkills.size());

        // Step 2: Identify matching and missing skills
        identifySkillGaps(resume, allJobSkills, result);

        // Calculate importance of missing skills
        calculateSkillImportance(result.getMissingSkills(), jobs);

        // Step 3: Calculate match percentage using K-Means clustering
        double matchPercentage = calculateMatchWithClustering(resume, jobs);
        result.setMatchPercentage(matchPercentage);

        System.out.println("✅ Analysis complete!");
        System.out.println("   Match percentage: " + String.format("%.1f%%", matchPercentage));
        System.out.println("   Matching skills: " + result.getMatchingSkillCount());
        System.out.println("   Skills to learn: " + result.getMissingSkillCount());

        return result;
    }

    /**
     * Collect all unique skills from job postings
     * @param jobs List of jobs
     * @return Set of unique skill names
     */
    private Set<String> collectAllJobSkills(List<Job> jobs) {
        Set<String> allSkills = new HashSet<>();

        for (Job job : jobs) {
            for (Skill skill : job.getRequiredSkills()) {
                allSkills.add(skill.getName().toLowerCase());
            }
        }

        return allSkills;
    }

    /**
     * Identify which skills the user has and which are missing
     * @param resume User's resume
     * @param jobSkills All skills required by jobs
     * @param result Result object to populate
     */
    private void identifySkillGaps(Resume resume, Set<String> jobSkills, AnalysisResult result) {
        // Create a set of user's skills (lowercase for comparison)
        Set<String> userSkillNames = new HashSet<>();
        for (Skill skill : resume.getSkills()) {
            userSkillNames.add(skill.getName().toLowerCase());
        }

        // Find matching skills (user has these)
        for (String jobSkill : jobSkills) {
            if (userSkillNames.contains(jobSkill)) {
                result.addMatchingSkill(new Skill(capitalize(jobSkill)));
            } else {
                result.addMissingSkill(new Skill(capitalize(jobSkill)));
            }
        }

        // Sort missing skills by importance (frequency in jobs)
        sortMissingSkillsByImportance(result, jobSkills);
    }

    /**
     * Sort missing skills by how often they appear in job postings
     * @param result Analysis result
     * @param jobSkills All job skills
     */
    private void sortMissingSkillsByImportance(AnalysisResult result, Set<String> jobSkills) {
        // For now, keep the order as is
        // In a real implementation, we would count frequency across all jobs
        // and sort by frequency
    }

    /**
     * Calculate importance scores for missing skills based on job frequency
     * @param missingSkills List of missing skills
     * @param jobs List of analyzed jobs
     */
    private void calculateSkillImportance(List<Skill> missingSkills, List<Job> jobs) {
        // Count how many jobs require each skill
        Map<String, Integer> skillJobCount = new HashMap<>();

        for (Job job : jobs) {
            for (Skill jobSkill : job.getRequiredSkills()) {
                String skillName = jobSkill.getName().toLowerCase();
                skillJobCount.put(skillName, skillJobCount.getOrDefault(skillName, 0) + 1);
            }
        }

        // Update frequency based on job count
        for (Skill skill : missingSkills) {
            String skillName = skill.getName().toLowerCase();
            int count = skillJobCount.getOrDefault(skillName, 1);
            skill.setFrequency(count);
        }

        // Sort by frequency (most important first)
        missingSkills.sort((s1, s2) -> Integer.compare(s2.getFrequency(), s1.getFrequency()));
    }

    /**
     * Calculate skill match percentage using K-Means clustering
     * This uses Weka's machine learning to group similar skill profiles
     * @param resume User's resume
     * @param jobs List of jobs
     * @return Match percentage (0-100)
     */
    private double calculateMatchWithClustering(Resume resume, List<Job> jobs) {
        try {
            System.out.println("\n🤖 Running K-Means clustering analysis...");

            // Step 1: Create a master skill list (all unique skills)
            Set<String> masterSkillSet = new HashSet<>();

            // Add skills from resume
            for (Skill skill : resume.getSkills()) {
                masterSkillSet.add(skill.getName().toLowerCase());
            }

            // Add skills from jobs
            for (Job job : jobs) {
                for (Skill skill : job.getRequiredSkills()) {
                    masterSkillSet.add(skill.getName().toLowerCase());
                }
            }

            List<String> masterSkillList = new ArrayList<>(masterSkillSet);
            System.out.println("   Total unique skills to analyze: " + masterSkillList.size());

            if (masterSkillList.isEmpty()) {
                System.out.println("   No skills found for clustering");
                return 0.0;
            }

            // Step 2: Create Weka dataset structure
            ArrayList<Attribute> attributes = new ArrayList<>();

            // Create binary attributes for each skill (has skill = 1, doesn't have = 0)
            for (String skillName : masterSkillList) {
                attributes.add(new Attribute(skillName.replaceAll("[^a-zA-Z0-9]", "_")));
            }

            // Create dataset
            Instances dataset = new Instances("SkillProfiles", attributes, jobs.size() + 1);

            // Step 3: Add resume as an instance (skill vector)
            Instance resumeInstance = createSkillVector(resume.getSkills(), masterSkillList, dataset);
            dataset.add(resumeInstance);

            // Step 4: Add each job as an instance
            for (Job job : jobs) {
                Instance jobInstance = createSkillVector(job.getRequiredSkills(), masterSkillList, dataset);
                dataset.add(jobInstance);
            }

            System.out.println("   Created dataset with " + dataset.numInstances() + " instances");
            System.out.println("   Each instance has " + dataset.numAttributes() + " attributes");

            // Step 5: Run K-Means clustering
            // We use 3 clusters: high-match, medium-match, low-match
            int numClusters = Math.min(3, dataset.numInstances());
            SimpleKMeans kmeans = new SimpleKMeans();
            kmeans.setNumClusters(numClusters);
            kmeans.setPreserveInstancesOrder(true);
            kmeans.buildClusterer(dataset);

            System.out.println("   K-Means clustering complete with " + numClusters + " clusters");

            // Step 6: Find which cluster the resume belongs to
            int resumeCluster = kmeans.clusterInstance(resumeInstance);

            // Step 7: Calculate match percentage based on clustering
            // Count how many jobs are in the same cluster as the resume
            int sameClusterCount = 0;
            for (int i = 1; i < dataset.numInstances(); i++) { // Skip index 0 (resume)
                int jobCluster = kmeans.clusterInstance(dataset.instance(i));
                if (jobCluster == resumeCluster) {
                    sameClusterCount++;
                }
            }

            double clusterMatchPercentage = (sameClusterCount * 100.0) / jobs.size();

            // Step 8: Also calculate simple overlap percentage
            double overlapPercentage = calculateSimpleOverlap(resume, jobs);

            // Step 9: Combine both methods (weighted average)
            // 60% from clustering, 40% from simple overlap
            double finalMatch = (clusterMatchPercentage * 0.6) + (overlapPercentage * 0.4);

            System.out.println("   Cluster-based match: " + String.format("%.1f%%", clusterMatchPercentage));
            System.out.println("   Overlap-based match: " + String.format("%.1f%%", overlapPercentage));
            System.out.println("   Final weighted match: " + String.format("%.1f%%", finalMatch));

            return finalMatch;

        } catch (Exception e) {
            System.err.println("❌ Error in K-Means clustering: " + e.getMessage());
            e.printStackTrace();

            // Fallback to simple overlap calculation
            return calculateSimpleOverlap(resume, jobs);
        }
    }

    /**
     * Create a skill vector (instance) for Weka
     * @param skills List of skills
     * @param masterSkillList Complete list of all skills
     * @param dataset Weka dataset
     * @return Weka instance
     */
    private Instance createSkillVector(List<Skill> skills, List<String> masterSkillList, Instances dataset) {
        double[] values = new double[masterSkillList.size()];

        // Create a set of skill names for quick lookup
        Set<String> skillNames = new HashSet<>();
        for (Skill skill : skills) {
            skillNames.add(skill.getName().toLowerCase());
        }

        // Fill the vector: 1 if skill present, 0 if not
        for (int i = 0; i < masterSkillList.size(); i++) {
            values[i] = skillNames.contains(masterSkillList.get(i)) ? 1.0 : 0.0;
        }

        return new DenseInstance(1.0, values);
    }

    /**
     * Simple overlap calculation (fallback method)
     * Calculate what percentage of required job skills the user has
     * @param resume User's resume
     * @param jobs List of jobs
     * @return Match percentage
     */
    private double calculateSimpleOverlap(Resume resume, List<Job> jobs) {
        Set<String> userSkills = new HashSet<>();
        for (Skill skill : resume.getSkills()) {
            userSkills.add(skill.getName().toLowerCase());
        }

        Set<String> requiredSkills = new HashSet<>();
        for (Job job : jobs) {
            for (Skill skill : job.getRequiredSkills()) {
                requiredSkills.add(skill.getName().toLowerCase());
            }
        }

        if (requiredSkills.isEmpty()) {
            return 0.0;
        }

        // Count matching skills
        int matchCount = 0;
        for (String requiredSkill : requiredSkills) {
            if (userSkills.contains(requiredSkill)) {
                matchCount++;
            }
        }

        return (matchCount * 100.0) / requiredSkills.size();
    }

    /**
     * Capitalize first letter of string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Test skill analyzer
     */
    public static void testAnalyzer() {
        System.out.println("\n=== Testing SkillAnalyzer ===");

        SkillAnalyzer analyzer = new SkillAnalyzer();

        // Create sample resume
        Resume resume = new Resume("test_resume.pdf");
        resume.addSkill(new Skill("Java"));
        resume.addSkill(new Skill("Python"));
        resume.addSkill(new Skill("SQL"));

        // Create sample jobs
        List<Job> jobs = new ArrayList<>();

        Job job1 = new Job("Java Developer", "Company A", "Java, Spring, SQL");
        job1.addRequiredSkill(new Skill("Java"));
        job1.addRequiredSkill(new Skill("Spring"));
        job1.addRequiredSkill(new Skill("SQL"));
        jobs.add(job1);

        Job job2 = new Job("Python Developer", "Company B", "Python, Django, SQL");
        job2.addRequiredSkill(new Skill("Python"));
        job2.addRequiredSkill(new Skill("Django"));
        job2.addRequiredSkill(new Skill("SQL"));
        jobs.add(job2);

        // Analyze
        AnalysisResult result = analyzer.analyzeSkills(resume, jobs);

        System.out.println("\nTest Results:");
        System.out.println("  Match: " + String.format("%.1f%%", result.getMatchPercentage()));
        System.out.println("  Matching skills: " + result.getMatchingSkillsAsString());
        System.out.println("  Missing skills: " + result.getMissingSkillsAsString());

        System.out.println("\n=== SkillAnalyzer Test Complete ===\n");
    }
}