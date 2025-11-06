package com.jobanalyzer.models;

/**
 * Skill Model Class
 *
 * Represents a skill (technical or soft skill)
 * Used throughout the application to track user skills and job requirements
 */
public class Skill {

    private String name;           // Skill name (e.g., "Java", "Python")
    private String category;       // Category (e.g., "Programming", "Database")
    private boolean isTechnical;   // True for technical skills, false for soft skills
    private int frequency;         // How many times this skill appears (used in analysis)

    /**
     * Default constructor
     */
    public Skill() {
        this.isTechnical = true;  // Default to technical skill
        this.frequency = 0;
    }

    /**
     * Constructor with name only
     * @param name Skill name
     */
    public Skill(String name) {
        this.name = name;
        this.isTechnical = true;
        this.frequency = 1;
    }

    /**
     * Full constructor
     * @param name Skill name
     * @param category Skill category
     * @param isTechnical Whether this is a technical skill
     */
    public Skill(String name, String category, boolean isTechnical) {
        this.name = name;
        this.category = category;
        this.isTechnical = isTechnical;
        this.frequency = 1;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isTechnical() {
        return isTechnical;
    }

    public void setTechnical(boolean technical) {
        isTechnical = technical;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Increment frequency counter
     * Useful when counting skill occurrences
     */
    public void incrementFrequency() {
        this.frequency++;
    }

    /**
     * Check if this skill matches another skill (case-insensitive)
     * @param otherSkill Skill to compare with
     * @return true if skills match
     */
    public boolean matches(Skill otherSkill) {
        if (otherSkill == null || otherSkill.getName() == null) {
            return false;
        }
        return this.name.equalsIgnoreCase(otherSkill.getName());
    }

    /**
     * Override equals for proper comparison
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Skill skill = (Skill) obj;
        return name != null && name.equalsIgnoreCase(skill.name);
    }

    /**
     * Override hashCode (required when overriding equals)
     */
    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }

    /**
     * String representation
     */
    @Override
    public String toString() {
        return "Skill{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", isTechnical=" + isTechnical +
                ", frequency=" + frequency +
                '}';
    }
}