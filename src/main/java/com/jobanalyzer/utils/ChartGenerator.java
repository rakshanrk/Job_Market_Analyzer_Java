package com.jobanalyzer.utils;

import com.jobanalyzer.models.AnalysisResult;
import com.jobanalyzer.models.Skill;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * ChartGenerator Utility Class
 *
 * Creates visualizations using JFreeChart:
 * - Bar chart for skill match percentage
 * - Radar/Spider chart for skill distribution
 */
public class ChartGenerator {

    // Chart dimensions
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 600;

    /**
     * Generate a bar chart showing skill match percentage
     * @param result Analysis result
     * @return JFreeChart bar chart
     */
    public static JFreeChart createSkillMatchBarChart(AnalysisResult result) {
        System.out.println("\n📊 Creating skill match bar chart...");

        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        double matchPercentage = result.getMatchPercentage();
        double gapPercentage = 100.0 - matchPercentage;

        dataset.addValue(matchPercentage, "Percentage", "Skills You Have");
        dataset.addValue(gapPercentage, "Percentage", "Skills to Learn");

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Skill Match Analysis",           // Chart title
                "Category",                        // X-axis label
                "Percentage (%)",                  // Y-axis label
                dataset,                           // Dataset
                PlotOrientation.VERTICAL,          // Orientation
                true,                              // Include legend
                true,                              // Tooltips
                false                              // URLs
        );

        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 20));

        // Add subtitle with actual numbers
        String subtitle = String.format("Match: %.1f%% | %d skills matched, %d skills to learn",
                matchPercentage, result.getMatchingSkillCount(), result.getMissingSkillCount());
        chart.addSubtitle(new TextTitle(subtitle, new Font("SansSerif", Font.PLAIN, 14)));

        // Customize plot
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(240, 248, 255));
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);

        // Set bar colors
        plot.getRenderer().setSeriesPaint(0, new Color(46, 204, 113));  // Green for matched

        System.out.println("✅ Bar chart created");
        return chart;
    }

    /**
     * Generate a spider/radar chart for skill distribution
     * Shows top skills across different categories
     * @param result Analysis result
     * @return JFreeChart spider chart
     */
    public static JFreeChart createSkillDistributionSpiderChart(AnalysisResult result) {
        System.out.println("\n🕸️ Creating skill distribution spider chart...");

        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Skill> matchingSkills = result.getMatchingSkills();
        List<Skill> missingSkills = result.getMissingSkills();

        // If we have skills, add top 8 for visualization
        int maxSkills = 8;

        // Add matching skills (up to maxSkills/2)
        int count = 0;
        for (Skill skill : matchingSkills) {
            if (count >= maxSkills / 2) break;
            dataset.addValue(5.0, "Your Skills", skill.getName());
            count++;
        }

        // Add missing skills (up to maxSkills/2)
        count = 0;
        for (Skill skill : missingSkills) {
            if (count >= maxSkills / 2) break;
            dataset.addValue(2.0, "Skills to Learn", skill.getName());
            count++;
        }

        // If no data, add placeholder
        if (dataset.getRowCount() == 0) {
            dataset.addValue(0, "No Data", "Skill 1");
            dataset.addValue(0, "No Data", "Skill 2");
        }

        // Create spider plot
        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setSeriesPaint(0, new Color(46, 204, 113));   // Green for your skills
        plot.setSeriesPaint(1, new Color(231, 76, 60));    // Red for skills to learn

        // Create chart
        JFreeChart chart = new JFreeChart(
                "Skill Distribution Overview",
                new Font("SansSerif", Font.BOLD, 18),
                plot,
                true  // Include legend
        );

        chart.setBackgroundPaint(Color.WHITE);

        System.out.println("✅ Spider chart created");
        return chart;
    }

    /**
     * Generate a bar chart showing top missing skills
     * @param result Analysis result
     * @return JFreeChart bar chart
     */
    public static JFreeChart createTopMissingSkillsChart(AnalysisResult result) {
        System.out.println("\n📈 Creating top missing skills chart...");

        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Skill> missingSkills = result.getMissingSkills();

        // Show top 10 missing skills
        int maxSkills = Math.min(10, missingSkills.size());

        for (int i = 0; i < maxSkills; i++) {
            Skill skill = missingSkills.get(i);
            // Use frequency or default value
            int importance = skill.getFrequency() > 0 ? skill.getFrequency() : (maxSkills - i);
            dataset.addValue(importance, "Importance", skill.getName());
        }

        // If no missing skills, show message
        if (dataset.getColumnCount() == 0) {
            dataset.addValue(0, "No Data", "All skills matched!");
        }

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Top Skills You Need to Learn",
                "Skill",
                "Importance",
                dataset,
                PlotOrientation.HORIZONTAL,
                false,  // No legend needed
                true,
                false
        );

        // Customize
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(255, 250, 240));
        plot.getRenderer().setSeriesPaint(0, new Color(52, 152, 219));  // Blue

        System.out.println("✅ Top missing skills chart created");
        return chart;
    }

    /**
     * Save chart as PNG file
     * @param chart JFreeChart to save
     * @param filename Output filename
     * @param width Image width
     * @param height Image height
     */
    public static void saveChartAsPNG(JFreeChart chart, String filename, int width, int height) {
        try {
            File outputFile = new File(filename);
            outputFile.getParentFile().mkdirs();  // Create directories if needed
            ChartUtils.saveChartAsPNG(outputFile, chart, width, height);
            System.out.println("✅ Chart saved: " + filename);
        } catch (IOException e) {
            System.err.println("❌ Error saving chart: " + e.getMessage());
        }
    }

    /**
     * Convert JFreeChart to JavaFX Image for display in GUI
     * @param chart JFreeChart to convert
     * @return JavaFX Image
     */
    public static Image convertChartToImage(JFreeChart chart) {
        BufferedImage bufferedImage = chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Test chart generator
     */
    public static void testChartGenerator() {
        System.out.println("\n=== Testing ChartGenerator ===");

        // Create sample analysis result
        AnalysisResult result = new AnalysisResult();
        result.setMatchPercentage(72.5);
        result.addMatchingSkill(new Skill("Java"));
        result.addMatchingSkill(new Skill("Python"));
        result.addMatchingSkill(new Skill("SQL"));
        result.addMissingSkill(new Skill("React"));
        result.addMissingSkill(new Skill("Docker"));
        result.addMissingSkill(new Skill("AWS"));

        // Create charts
        JFreeChart barChart = createSkillMatchBarChart(result);
        JFreeChart spiderChart = createSkillDistributionSpiderChart(result);
        JFreeChart topSkillsChart = createTopMissingSkillsChart(result);

        // Save charts to test
        saveChartAsPNG(barChart, "test_bar_chart.png", CHART_WIDTH, CHART_HEIGHT);
        saveChartAsPNG(spiderChart, "test_spider_chart.png", CHART_WIDTH, CHART_HEIGHT);
        saveChartAsPNG(topSkillsChart, "test_top_skills.png", CHART_WIDTH, CHART_HEIGHT);

        System.out.println("\n✅ Charts created and saved successfully!");
        System.out.println("   Check project root for test chart PNG files");

        System.out.println("\n=== ChartGenerator Test Complete ===\n");
    }
}