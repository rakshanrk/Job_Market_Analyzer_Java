package com.jobanalyzer.models;

/**
 * LearningResource Model Class
 *
 * Represents a learning resource (course, tutorial, project)
 * mapped to a specific skill
 */
public class LearningResource {

    private int id;                       // Database ID
    private String skillName;             // Skill this resource teaches
    private String resourceTitle;         // Title of the resource
    private String resourceType;          // "Course", "Tutorial", "GitHub Project", etc.
    private String resourceUrl;           // URL to access the resource
    private String platform;              // Platform (Coursera, Udemy, GitHub, etc.)
    private int durationWeeks;            // Estimated duration in weeks
    private String difficultyLevel;       // "Beginner", "Intermediate", "Advanced"
    private String description;           // Resource description

    /**
     * Default constructor
     */
    public LearningResource() {
    }

    /**
     * Full constructor
     */
    public LearningResource(String skillName, String resourceTitle, String resourceType,
                            String resourceUrl, String platform, int durationWeeks,
                            String difficultyLevel, String description) {
        this.skillName = skillName;
        this.resourceTitle = resourceTitle;
        this.resourceType = resourceType;
        this.resourceUrl = resourceUrl;
        this.platform = platform;
        this.durationWeeks = durationWeeks;
        this.difficultyLevel = difficultyLevel;
        this.description = description;
    }

    /**
     * Check if this resource is suitable for a beginner
     * @return true if beginner level
     */
    public boolean isForBeginners() {
        return difficultyLevel != null && difficultyLevel.equalsIgnoreCase("Beginner");
    }

    /**
     * Check if resource duration fits within given weeks
     * @param maxWeeks Maximum weeks available
     * @return true if it fits
     */
    public boolean fitsInTimeframe(int maxWeeks) {
        return durationWeeks <= maxWeeks;
    }

    /**
     * Get a formatted display string for UI
     * @return Formatted string
     */
    public String getDisplayString() {
        return String.format("%s (%s) - %d weeks - %s",
                resourceTitle, platform, durationWeeks, difficultyLevel);
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getResourceTitle() {
        return resourceTitle;
    }

    public void setResourceTitle(String resourceTitle) {
        this.resourceTitle = resourceTitle;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "LearningResource{" +
                "skillName='" + skillName + '\'' +
                ", resourceTitle='" + resourceTitle + '\'' +
                ", platform='" + platform + '\'' +
                ", durationWeeks=" + durationWeeks +
                ", difficultyLevel='" + difficultyLevel + '\'' +
                '}';
    }
}