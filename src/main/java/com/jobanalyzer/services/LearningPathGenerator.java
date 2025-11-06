package com.jobanalyzer.services;

import com.jobanalyzer.models.AnalysisResult;
import com.jobanalyzer.models.LearningResource;
import com.jobanalyzer.models.Skill;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * LearningPathGenerator Service Class
 *
 * Generates personalized 4-week learning paths based on skill gaps
 * Retrieves learning resources from database and organizes them into a schedule
 */
public class LearningPathGenerator {

    private DatabaseManager databaseManager;

    /**
     * Constructor
     */
    public LearningPathGenerator() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    /**
     * Generate a 4-week learning path based on analysis results
     * @param result Analysis result with missing skills
     * @return Formatted learning path as text
     */
    public String generateLearningPath(AnalysisResult result) {
        System.out.println("\n📚 Generating 4-week learning path...");

        List<Skill> missingSkills = result.getMissingSkills();

        if (missingSkills.isEmpty()) {
            return "🎉 Congratulations! You have all the required skills!\n\n" +
                    "Consider exploring advanced topics or specializations in your field.";
        }

        System.out.println("   Skills to learn: " + missingSkills.size());

        // Limit to top 8 skills (2 per week)
        int maxSkills = Math.min(8, missingSkills.size());
        List<Skill> prioritySkills = missingSkills.subList(0, maxSkills);

        // Get learning resources for each skill
        List<LearningResource> allResources = new ArrayList<>();
        for (Skill skill : prioritySkills) {
            List<LearningResource> resources = getResourcesForSkill(skill.getName());
            allResources.addAll(resources);
        }

        System.out.println("   Found " + allResources.size() + " learning resources");

        // Build the 4-week plan
        StringBuilder plan = new StringBuilder();
        plan.append("═══════════════════════════════════════════════════════════\n");
        plan.append("        YOUR PERSONALIZED 4-WEEK LEARNING PATH\n");
        plan.append("═══════════════════════════════════════════════════════════\n\n");

        plan.append("📊 Skills Gap Analysis:\n");
        plan.append("   • Total skills to learn: ").append(missingSkills.size()).append("\n");
        plan.append("   • Priority skills (4 weeks): ").append(prioritySkills.size()).append("\n");
        plan.append("   • Current match rate: ").append(String.format("%.1f%%", result.getMatchPercentage())).append("\n\n");

        // Divide skills across 4 weeks (2 skills per week)
        int skillsPerWeek = 2;

        for (int week = 1; week <= 4; week++) {
            plan.append(generateWeekPlan(week, prioritySkills, allResources, skillsPerWeek, result));
        }

        // Add final recommendations
        plan.append("\n═══════════════════════════════════════════════════════════\n");
        plan.append("📝 ADDITIONAL RECOMMENDATIONS\n");
        plan.append("═══════════════════════════════════════════════════════════\n\n");

        plan.append("✅ Practice Tips:\n");
        plan.append("   • Build real projects for each skill you learn\n");
        plan.append("   • Contribute to open-source projects on GitHub\n");
        plan.append("   • Join online communities and forums\n");
        plan.append("   • Document your learning journey on a blog\n\n");

        plan.append("🎯 Goal Setting:\n");
        plan.append("   • Dedicate 1-2 hours daily to learning\n");
        plan.append("   • Complete at least one project per week\n");
        plan.append("   • Review and revise concepts regularly\n");
        plan.append("   • Track your progress and adjust as needed\n\n");

        if (missingSkills.size() > maxSkills) {
            plan.append("📌 Remaining Skills: After completing this 4-week plan,\n");
            plan.append("   focus on: ");
            for (int i = maxSkills; i < Math.min(maxSkills + 5, missingSkills.size()); i++) {
                plan.append(missingSkills.get(i).getName());
                if (i < Math.min(maxSkills + 5, missingSkills.size()) - 1) {
                    plan.append(", ");
                }
            }
            plan.append("\n");
        }

        System.out.println("✅ Learning path generated successfully!");

        return plan.toString();
    }

    /**
     * Generate plan for a specific week
     * @param weekNumber Week number (1-4)
     * @param skills All priority skills
     * @param resources All available resources
     * @param skillsPerWeek Number of skills to cover per week
     * @param result Analysis result (for saving to DB)
     * @return Formatted week plan
     */
    private String generateWeekPlan(int weekNumber, List<Skill> skills,
                                    List<LearningResource> resources,
                                    int skillsPerWeek, AnalysisResult result) {
        StringBuilder weekPlan = new StringBuilder();

        weekPlan.append("───────────────────────────────────────────────────────────\n");
        weekPlan.append("📅 WEEK ").append(weekNumber).append("\n");
        weekPlan.append("───────────────────────────────────────────────────────────\n\n");

        // Determine which skills to focus on this week
        int startIndex = (weekNumber - 1) * skillsPerWeek;
        int endIndex = Math.min(startIndex + skillsPerWeek, skills.size());

        if (startIndex >= skills.size()) {
            weekPlan.append("🎉 Review and practice skills from previous weeks!\n\n");
            return weekPlan.toString();
        }

        List<Skill> weekSkills = skills.subList(startIndex, endIndex);

        weekPlan.append("🎯 Focus Skills:\n");
        for (Skill skill : weekSkills) {
            weekPlan.append("   • ").append(skill.getName()).append("\n");
        }
        weekPlan.append("\n");

        // Add resources for each skill
        for (Skill skill : weekSkills) {
            weekPlan.append("📖 ").append(skill.getName()).append(" Learning Resources:\n");

            List<LearningResource> skillResources = getResourcesForSkill(skill.getName());

            if (skillResources.isEmpty()) {
                weekPlan.append("   • Search for \"").append(skill.getName()).append(" tutorial\" on Coursera, Udemy, or YouTube\n");
                weekPlan.append("   • Practice on GitHub: https://github.com/topics/").append(skill.getName().toLowerCase()).append("\n");
            } else {
                int count = 0;
                for (LearningResource resource : skillResources) {
                    weekPlan.append("   • ").append(resource.getResourceTitle())
                            .append(" (").append(resource.getPlatform()).append(")")
                            .append(" - ").append(resource.getDifficultyLevel()).append("\n");
                    weekPlan.append("     ↳ ").append(resource.getResourceUrl()).append("\n");

                    count++;
                    if (count >= 2) break; // Show max 2 resources per skill
                }
            }
            weekPlan.append("\n");
        }

        // Add weekly milestones
        weekPlan.append("🏆 Week ").append(weekNumber).append(" Milestones:\n");
        for (int i = 0; i < weekSkills.size(); i++) {
            Skill skill = weekSkills.get(i);
            weekPlan.append("   ").append(i + 1).append(". Complete basic ").append(skill.getName())
                    .append(" tutorial and build one small project\n");
        }
        weekPlan.append("   ").append(weekSkills.size() + 1).append(". Document your learning and upload projects to GitHub\n");
        weekPlan.append("\n");

        return weekPlan.toString();
    }

    /**
     * Get learning resources for a specific skill from database
     * @param skillName Name of the skill
     * @return List of learning resources
     */
    private List<LearningResource> getResourcesForSkill(String skillName) {
        List<LearningResource> resources = new ArrayList<>();

        try {
            ResultSet rs = databaseManager.getLearningResourcesBySkill(skillName);

            if (rs != null) {
                while (rs.next()) {
                    LearningResource resource = new LearningResource();
                    resource.setId(rs.getInt("id"));
                    resource.setSkillName(rs.getString("skill_name"));
                    resource.setResourceTitle(rs.getString("resource_title"));
                    resource.setResourceType(rs.getString("resource_type"));
                    resource.setResourceUrl(rs.getString("resource_url"));
                    resource.setPlatform(rs.getString("platform"));
                    resource.setDurationWeeks(rs.getInt("duration_weeks"));
                    resource.setDifficultyLevel(rs.getString("difficulty_level"));
                    resource.setDescription(rs.getString("description"));

                    resources.add(resource);
                }
                rs.close();
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error fetching resources for " + skillName + ": " + e.getMessage());
        }

        return resources;
    }

    /**
     * Save learning path to database
     * @param analysisId Analysis ID from database
     * @param learningPath Generated learning path
     */
    public void saveLearningPath(int analysisId, String learningPath, AnalysisResult result) {
        System.out.println("\n💾 Saving learning path to database...");

        List<Skill> missingSkills = result.getMissingSkills();
        int skillsPerWeek = 2;

        for (int week = 1; week <= 4; week++) {
            int startIndex = (week - 1) * skillsPerWeek;
            int endIndex = Math.min(startIndex + skillsPerWeek, missingSkills.size());

            if (startIndex >= missingSkills.size()) {
                break;
            }

            List<Skill> weekSkills = missingSkills.subList(startIndex, endIndex);

            // Prepare week data
            StringBuilder skillFocus = new StringBuilder();
            StringBuilder resources = new StringBuilder();
            StringBuilder milestones = new StringBuilder();

            for (Skill skill : weekSkills) {
                skillFocus.append(skill.getName()).append(", ");

                List<LearningResource> skillResources = getResourcesForSkill(skill.getName());
                for (LearningResource resource : skillResources) {
                    resources.append(resource.getResourceTitle()).append(" (")
                            .append(resource.getPlatform()).append("), ");
                    if (resources.length() > 200) break; // Limit length
                }

                milestones.append("Complete ").append(skill.getName()).append(" basics; ");
            }

            // Remove trailing commas
            if (skillFocus.length() > 0) {
                skillFocus.setLength(skillFocus.length() - 2);
            }
            if (resources.length() > 0) {
                resources.setLength(resources.length() - 2);
            }

            // Save to database
            databaseManager.saveLearningPath(
                    analysisId,
                    week,
                    skillFocus.toString(),
                    resources.toString(),
                    milestones.toString()
            );
        }

        System.out.println("✅ Learning path saved to database");
    }

    /**
     * Test learning path generator
     */
    public static void testGenerator() {
        System.out.println("\n=== Testing LearningPathGenerator ===");

        LearningPathGenerator generator = new LearningPathGenerator();

        // Create sample analysis result
        AnalysisResult result = new AnalysisResult();
        result.setMatchPercentage(65.5);

        // Add some missing skills
        result.addMissingSkill(new Skill("React"));
        result.addMissingSkill(new Skill("Docker"));
        result.addMissingSkill(new Skill("AWS"));
        result.addMissingSkill(new Skill("Spring"));

        // Generate path
        String learningPath = generator.generateLearningPath(result);

        System.out.println("\nGenerated Learning Path:");
        System.out.println(learningPath);

        System.out.println("=== LearningPathGenerator Test Complete ===\n");
    }
}