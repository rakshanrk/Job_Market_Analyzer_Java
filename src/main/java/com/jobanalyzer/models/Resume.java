package com.jobanalyzer.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Resume Model Class
 *
 * Represents a user's resume with extracted information
 */
public class Resume {

    private String filename;              // Original filename
    private String fileType;              // "PDF" or "IMAGE"
    private String extractedText;         // Full text extracted from resume
    private List<Skill> skills;           // Skills found in resume
    private String userName;              // User's name (optional)
    private String email;                 // User's email (optional)
    private String phone;                 // User's phone (optional)

    /**
     * Default constructor
     */
    public Resume() {
        this.skills = new ArrayList<>();
    }

    /**
     * Constructor with filename
     * @param filename Name of the resume file
     */
    public Resume(String filename) {
        this.filename = filename;
        this.skills = new ArrayList<>();

        // Determine file type from extension
        if (filename.toLowerCase().endsWith(".pdf")) {
            this.fileType = "PDF";
        } else {
            this.fileType = "IMAGE";
        }
    }

    /**
     * Add a skill to the resume
     * @param skill Skill to add
     */
    public void addSkill(Skill skill) {
        if (skill != null && !skills.contains(skill)) {
            skills.add(skill);
        }
    }

    /**
     * Add multiple skills at once
     * @param skillList List of skills to add
     */
    public void addSkills(List<Skill> skillList) {
        if (skillList != null) {
            for (Skill skill : skillList) {
                addSkill(skill);
            }
        }
    }

    /**
     * Check if resume contains a specific skill
     * @param skillName Name of the skill to check
     * @return true if skill exists
     */
    public boolean hasSkill(String skillName) {
        if (skillName == null) return false;

        for (Skill skill : skills) {
            if (skill.getName().equalsIgnoreCase(skillName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get total number of skills
     * @return Number of skills found
     */
    public int getSkillCount() {
        return skills.size();
    }

    /**
     * Get comma-separated list of skill names
     * @return String of skills (e.g., "Java, Python, SQL")
     */
    public String getSkillsAsString() {
        if (skills.isEmpty()) {
            return "No skills found";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < skills.size(); i++) {
            sb.append(skills.get(i).getName());
            if (i < skills.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    // Getters and Setters

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Resume{" +
                "filename='" + filename + '\'' +
                ", fileType='" + fileType + '\'' +
                ", skillCount=" + skills.size() +
                ", userName='" + userName + '\'' +
                '}';
    }
}