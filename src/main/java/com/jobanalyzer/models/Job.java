package com.jobanalyzer.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Job Model Class
 *
 * Represents a job posting fetched from Adzuna API
 */
public class Job {

    private String id;                    // Job ID from API
    private String title;                 // Job title
    private String company;               // Company name
    private String location;              // Job location
    private String description;           // Job description
    private String url;                   // Job posting URL
    private double salary;                // Salary (if available)
    private List<Skill> requiredSkills;   // Skills required for this job

    /**
     * Default constructor
     */
    public Job() {
        this.requiredSkills = new ArrayList<>();
    }

    /**
     * Constructor with basic info
     * @param title Job title
     * @param company Company name
     * @param description Job description
     */
    public Job(String title, String company, String description) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.requiredSkills = new ArrayList<>();
    }

    /**
     * Add a required skill to this job
     * @param skill Skill to add
     */
    public void addRequiredSkill(Skill skill) {
        if (skill != null && !requiredSkills.contains(skill)) {
            requiredSkills.add(skill);
        }
    }

    /**
     * Add multiple required skills
     * @param skills List of skills
     */
    public void addRequiredSkills(List<Skill> skills) {
        if (skills != null) {
            for (Skill skill : skills) {
                addRequiredSkill(skill);
            }
        }
    }

    /**
     * Check if this job requires a specific skill
     * @param skillName Name of the skill
     * @return true if skill is required
     */
    public boolean requiresSkill(String skillName) {
        if (skillName == null) return false;

        for (Skill skill : requiredSkills) {
            if (skill.getName().equalsIgnoreCase(skillName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get total number of required skills
     * @return Number of skills
     */
    public int getRequiredSkillCount() {
        return requiredSkills.size();
    }

    /**
     * Get comma-separated list of required skills
     * @return String of skills
     */
    public String getRequiredSkillsAsString() {
        if (requiredSkills.isEmpty()) {
            return "No specific skills listed";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < requiredSkills.size(); i++) {
            sb.append(requiredSkills.get(i).getName());
            if (i < requiredSkills.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public List<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<Skill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    @Override
    public String toString() {
        return "Job{" +
                "title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", requiredSkills=" + requiredSkills.size() +
                '}';
    }
}