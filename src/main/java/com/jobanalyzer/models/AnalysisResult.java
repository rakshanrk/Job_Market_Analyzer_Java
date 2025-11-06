package com.jobanalyzer.models;

import java.util.ArrayList;
import java.util.List;

/**
 * AnalysisResult Model Class
 *
 * Contains the complete analysis results after comparing
 * user's resume skills with job market requirements
 */
public class AnalysisResult {

    private Resume resume;                    // User's resume
    private List<Job> analyzedJobs;           // Jobs that were analyzed
    private List<Skill> matchingSkills;       // Skills user has that match jobs
    private List<Skill> missingSkills;        // Skills user needs to learn
    private double matchPercentage;           // Overall skill match (0-100)
    private int totalJobsAnalyzed;            // Number of jobs analyzed
    private List<LearningResource> recommendedResources;  // Suggested learning resources
    private String learningPath;              // 4-week learning plan (formatted text)

    /**
     * Default constructor
     */
    public AnalysisResult() {
        this.analyzedJobs = new ArrayList<>();
        this.matchingSkills = new ArrayList<>();
        this.missingSkills = new ArrayList<>();
        this.recommendedResources = new ArrayList<>();
    }

    /**
     * Constructor with resume
     * @param resume User's resume
     */
    public AnalysisResult(Resume resume) {
        this();
        this.resume = resume;
    }

    /**
     * Calculate match percentage based on matching and missing skills
     */
    public void calculateMatchPercentage() {
        int totalSkills = matchingSkills.size() + missingSkills.size();
        if (totalSkills > 0) {
            this.matchPercentage = (matchingSkills.size() * 100.0) / totalSkills;
        } else {
            this.matchPercentage = 0.0;
        }
    }

    /**
     * Add a matching skill
     * @param skill Skill to add
     */
    public void addMatchingSkill(Skill skill) {
        if (skill != null && !matchingSkills.contains(skill)) {
            matchingSkills.add(skill);
        }
    }

    /**
     * Add a missing skill
     * @param skill Skill to add
     */
    public void addMissingSkill(Skill skill) {
        if (skill != null && !missingSkills.contains(skill)) {
            missingSkills.add(skill);
        }
    }

    /**
     * Add a recommended learning resource
     * @param resource Resource to add
     */
    public void addRecommendedResource(LearningResource resource) {
        if (resource != null) {
            recommendedResources.add(resource);
        }
    }

    /**
     * Get matching skills as comma-separated string
     * @return String of matching skills
     */
    public String getMatchingSkillsAsString() {
        if (matchingSkills.isEmpty()) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matchingSkills.size(); i++) {
            sb.append(matchingSkills.get(i).getName());
            if (i < matchingSkills.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Get missing skills as comma-separated string
     * @return String of missing skills
     */
    public String getMissingSkillsAsString() {
        if (missingSkills.isEmpty()) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < missingSkills.size(); i++) {
            sb.append(missingSkills.get(i).getName());
            if (i < missingSkills.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Get number of matching skills
     * @return Count of matching skills
     */
    public int getMatchingSkillCount() {
        return matchingSkills.size();
    }

    /**
     * Get number of missing skills
     * @return Count of missing skills
     */
    public int getMissingSkillCount() {
        return missingSkills.size();
    }

    /**
     * Check if analysis is successful (has data)
     * @return true if analysis has results
     */
    public boolean hasResults() {
        return totalJobsAnalyzed > 0 &&
                (matchingSkills.size() > 0 || missingSkills.size() > 0);
    }

    /**
     * Get a summary string of the analysis
     * @return Summary text
     */
    public String getSummary() {
        return String.format(
                "Analyzed %d jobs | Match: %.1f%% | Matching Skills: %d | Skills to Learn: %d",
                totalJobsAnalyzed, matchPercentage, matchingSkills.size(), missingSkills.size()
        );
    }

    // Getters and Setters

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public List<Job> getAnalyzedJobs() {
        return analyzedJobs;
    }

    public void setAnalyzedJobs(List<Job> analyzedJobs) {
        this.analyzedJobs = analyzedJobs;
    }

    public List<Skill> getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(List<Skill> matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public List<Skill> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<Skill> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public int getTotalJobsAnalyzed() {
        return totalJobsAnalyzed;
    }

    public void setTotalJobsAnalyzed(int totalJobsAnalyzed) {
        this.totalJobsAnalyzed = totalJobsAnalyzed;
    }

    public List<LearningResource> getRecommendedResources() {
        return recommendedResources;
    }

    public void setRecommendedResources(List<LearningResource> recommendedResources) {
        this.recommendedResources = recommendedResources;
    }

    public String getLearningPath() {
        return learningPath;
    }

    public void setLearningPath(String learningPath) {
        this.learningPath = learningPath;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "matchPercentage=" + matchPercentage +
                ", totalJobsAnalyzed=" + totalJobsAnalyzed +
                ", matchingSkills=" + matchingSkills.size() +
                ", missingSkills=" + missingSkills.size() +
                '}';
    }
}