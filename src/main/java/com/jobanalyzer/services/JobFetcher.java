package com.jobanalyzer.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jobanalyzer.models.Job;
import com.jobanalyzer.models.Skill;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JobFetcher Service Class
 *
 * Fetches real job postings from Adzuna API
 * Extracts required skills from job descriptions
 */
public class JobFetcher {

    // Adzuna API credentials
    // TODO: Replace with your actual App ID and API Key from https://developer.adzuna.com
    private static final String APP_ID = "82733db2";
    private static final String APP_KEY = "60fec3f6e28841599715813d9cd0453a";

    // API endpoints
    private static final String API_BASE_URL = "https://api.adzuna.com/v1/api/jobs";
    private static final String COUNTRY_CODE = "us";  // United States

    // Skill extractor for analyzing job descriptions
    private SkillExtractor skillExtractor;

    /**
     * Constructor
     */
    public JobFetcher() {
        this.skillExtractor = new SkillExtractor();
    }

    /**
     * Fetch jobs based on search query
     * @param searchQuery Job title or keywords (e.g., "Java Developer", "Data Scientist")
     * @param maxResults Maximum number of jobs to fetch (default: 50)
     * @return List of Job objects
     */
    public List<Job> fetchJobs(String searchQuery, int maxResults) {
        System.out.println("\n🔍 Fetching jobs from Adzuna API...");
        System.out.println("   Search query: " + searchQuery);
        System.out.println("   Max results: " + maxResults);

        List<Job> jobs = new ArrayList<>();

        // Check if API credentials are set
        if (APP_ID.equals("YOUR_APP_ID") || APP_KEY.equals("YOUR_API_KEY")) {
            System.err.println("⚠️ WARNING: Adzuna API credentials not configured!");
            System.err.println("   Please add your App ID and API Key in JobFetcher.java");
            System.err.println("   Get free credentials at: https://developer.adzuna.com/signup");
            System.err.println("   Using sample data instead...");
            return createSampleJobs(searchQuery);
        }

        try {
            // Calculate number of pages needed (Adzuna returns 10 results per page)
            int resultsPerPage = 10;
            int pages = (int) Math.ceil(maxResults / (double) resultsPerPage);
            pages = Math.min(pages, 5); // Limit to 5 pages max to save API calls

            // Fetch jobs from multiple pages
            for (int page = 1; page <= pages && jobs.size() < maxResults; page++) {
                String url = buildApiUrl(searchQuery, page);
                List<Job> pageJobs = fetchJobsFromUrl(url);
                jobs.addAll(pageJobs);

                System.out.println("   ✅ Fetched page " + page + ": " + pageJobs.size() + " jobs");

                // Stop if we got fewer results than expected (no more pages)
                if (pageJobs.size() < resultsPerPage) {
                    break;
                }
            }

            // Trim to max results
            if (jobs.size() > maxResults) {
                jobs = jobs.subList(0, maxResults);
            }

            System.out.println("✅ Total jobs fetched: " + jobs.size());

        } catch (Exception e) {
            System.err.println("❌ Error fetching jobs from API: " + e.getMessage());
            e.printStackTrace();

            // Fallback to sample data
            System.out.println("   Using sample data instead...");
            jobs = createSampleJobs(searchQuery);
        }

        return jobs;
    }

    /**
     * Fetch jobs with default max results (50)
     * @param searchQuery Job search query
     * @return List of jobs
     */
    public List<Job> fetchJobs(String searchQuery) {
        return fetchJobs(searchQuery, 50);
    }

    /**
     * Build Adzuna API URL
     * @param searchQuery Search query
     * @param page Page number
     * @return Complete API URL
     */
    private String buildApiUrl(String searchQuery, int page) {
        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            return String.format("%s/%s/search/%d?app_id=%s&app_key=%s&results_per_page=10&what=%s",
                    API_BASE_URL, COUNTRY_CODE, page, APP_ID, APP_KEY, encodedQuery);
        } catch (Exception e) {
            System.err.println("❌ Error building API URL: " + e.getMessage());
            return "";
        }
    }

    /**
     * Fetch jobs from a specific URL
     * @param url API URL
     * @return List of jobs from this page
     */
    private List<Job> fetchJobsFromUrl(String url) {
        List<Job> jobs = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());

                // Parse JSON response
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                JsonArray results = jsonObject.getAsJsonArray("results");

                if (results != null) {
                    for (JsonElement element : results) {
                        Job job = parseJob(element.getAsJsonObject());
                        if (job != null) {
                            jobs.add(job);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error fetching jobs from URL: " + e.getMessage());
        }

        return jobs;
    }

    /**
     * Parse a single job from JSON
     * @param jsonJob JSON object representing a job
     * @return Job object
     */
    private Job parseJob(JsonObject jsonJob) {
        try {
            Job job = new Job();

            // Extract basic info
            job.setId(getJsonString(jsonJob, "id"));
            job.setTitle(getJsonString(jsonJob, "title"));
            job.setCompany(getJsonString(jsonJob, "company", "display_name"));
            job.setDescription(getJsonString(jsonJob, "description"));
            job.setUrl(getJsonString(jsonJob, "redirect_url"));

            // Extract location
            JsonObject location = jsonJob.getAsJsonObject("location");
            if (location != null) {
                String city = getJsonString(location, "display_name");
                job.setLocation(city);
            }

            // Extract salary (if available)
            if (jsonJob.has("salary_min") && !jsonJob.get("salary_min").isJsonNull()) {
                double salaryMin = jsonJob.get("salary_min").getAsDouble();
                job.setSalary(salaryMin);
            }

            // Extract skills from description
            String description = job.getDescription();
            if (description != null && !description.isEmpty()) {
                List<Skill> skills = skillExtractor.extractSkills(description);
                job.setRequiredSkills(skills);
            }

            return job;

        } catch (Exception e) {
            System.err.println("⚠️ Error parsing job: " + e.getMessage());
            return null;
        }
    }

    /**
     * Safely get string from JSON object
     */
    private String getJsonString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }

    /**
     * Safely get nested string from JSON object
     */
    private String getJsonString(JsonObject obj, String key, String nestedKey) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            JsonObject nested = obj.getAsJsonObject(key);
            if (nested.has(nestedKey) && !nested.get(nestedKey).isJsonNull()) {
                return nested.get(nestedKey).getAsString();
            }
        }
        return "";
    }

    /**
     * Create sample jobs for testing (when API is not available)
     * @param searchQuery Search query to base sample data on
     * @return List of sample jobs
     */
    private List<Job> createSampleJobs(String searchQuery) {
        List<Job> sampleJobs = new ArrayList<>();

        // Determine job type and create relevant jobs
        String lowerQuery = searchQuery.toLowerCase();

        if (lowerQuery.contains("data scient")) {
            return createDataScienceJobs();
        } else if (lowerQuery.contains("web dev")) {
            return createWebDeveloperJobs();
        } else if (lowerQuery.contains("mobile")) {
            return createMobileDeveloperJobs();
        } else if (lowerQuery.contains("devops")) {
            return createDevOpsJobs();
        } else if (lowerQuery.contains("machine learning")) {
            return createMLEngineerJobs();
        } else {
            // Default: Software Developer jobs
            return createSoftwareDeveloperJobs();
        }
    }

    private List<Job> createSoftwareDeveloperJobs() {
        List<Job> jobs = new ArrayList<>();

        String[] companies = {"Google", "Microsoft", "Amazon", "Meta", "Apple", "Netflix", "Uber", "Airbnb", "Spotify", "Tesla"};
        String[] cities = {"San Francisco, CA", "Seattle, WA", "New York, NY", "Austin, TX", "Boston, MA"};

        for (int i = 0; i < 50; i++) {
            Job job = new Job(
                    "Software Engineer " + (i % 3 == 0 ? "II" : i % 3 == 1 ? "III" : ""),
                    companies[i % companies.length],
                    "Looking for software engineer with strong programming skills. " +
                            "Experience with Java, Python, algorithms, and system design required. " +
                            "Knowledge of databases, REST APIs, and Agile methodologies preferred."
            );
            job.setId("sw-" + i);
            job.setLocation(cities[i % cities.length]);
            job.setSalary(100000 + (i * 2000));
            job.setUrl("https://www.linkedin.com/jobs/");

            // Add relevant skills
            job.addRequiredSkill(new Skill("Java"));
            job.addRequiredSkill(new Skill("Python"));
            job.addRequiredSkill(new Skill("SQL"));
            job.addRequiredSkill(new Skill("Git"));
            job.addRequiredSkill(new Skill("REST API"));
            if (i % 2 == 0) job.addRequiredSkill(new Skill("Docker"));
            if (i % 3 == 0) job.addRequiredSkill(new Skill("AWS"));
            if (i % 4 == 0) job.addRequiredSkill(new Skill("Kubernetes"));

            jobs.add(job);
        }
        return jobs;
    }

    private List<Job> createDataScienceJobs() {
        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Job job = new Job(
                    "Data Scientist",
                    "Company " + i,
                    "Seeking data scientist with Python, machine learning, and statistics expertise. " +
                            "Experience with TensorFlow, PyTorch, pandas, and data visualization required."
            );
            job.setLocation("Remote");
            job.addRequiredSkill(new Skill("Python"));
            job.addRequiredSkill(new Skill("Machine Learning"));
            job.addRequiredSkill(new Skill("Statistics"));
            job.addRequiredSkill(new Skill("Pandas"));
            job.addRequiredSkill(new Skill("NumPy"));
            job.addRequiredSkill(new Skill("TensorFlow"));
            job.addRequiredSkill(new Skill("SQL"));
            jobs.add(job);
        }
        return jobs;
    }

    private List<Job> createWebDeveloperJobs() {
        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Job job = new Job(
                    "Web Developer",
                    "WebCo " + i,
                    "Web developer needed. HTML, CSS, JavaScript, React, Node.js experience required."
            );
            job.addRequiredSkill(new Skill("HTML"));
            job.addRequiredSkill(new Skill("CSS"));
            job.addRequiredSkill(new Skill("JavaScript"));
            job.addRequiredSkill(new Skill("React"));
            job.addRequiredSkill(new Skill("Node.js"));
            job.addRequiredSkill(new Skill("Git"));
            jobs.add(job);
        }
        return jobs;
    }

    private List<Job> createMobileDeveloperJobs() {
        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Job job = new Job(
                    "Mobile Developer",
                    "MobileApp " + i,
                    "Mobile developer for iOS and Android. Swift, Kotlin, React Native experience."
            );
            if (i % 2 == 0) {
                job.addRequiredSkill(new Skill("iOS"));
                job.addRequiredSkill(new Skill("Swift"));
            } else {
                job.addRequiredSkill(new Skill("Android"));
                job.addRequiredSkill(new Skill("Kotlin"));
            }
            job.addRequiredSkill(new Skill("React Native"));
            job.addRequiredSkill(new Skill("Git"));
            jobs.add(job);
        }
        return jobs;
    }

    private List<Job> createDevOpsJobs() {
        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Job job = new Job(
                    "DevOps Engineer",
                    "CloudOps " + i,
                    "DevOps engineer needed. Docker, Kubernetes, AWS, CI/CD, Linux required."
            );
            job.addRequiredSkill(new Skill("Docker"));
            job.addRequiredSkill(new Skill("Kubernetes"));
            job.addRequiredSkill(new Skill("AWS"));
            job.addRequiredSkill(new Skill("Linux"));
            job.addRequiredSkill(new Skill("Git"));
            job.addRequiredSkill(new Skill("Jenkins"));
            jobs.add(job);
        }
        return jobs;
    }

    private List<Job> createMLEngineerJobs() {
        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Job job = new Job(
                    "Machine Learning Engineer",
                    "AI Corp " + i,
                    "ML engineer for deep learning projects. Python, TensorFlow, PyTorch required."
            );
            job.addRequiredSkill(new Skill("Python"));
            job.addRequiredSkill(new Skill("Machine Learning"));
            job.addRequiredSkill(new Skill("Deep Learning"));
            job.addRequiredSkill(new Skill("TensorFlow"));
            job.addRequiredSkill(new Skill("PyTorch"));
            job.addRequiredSkill(new Skill("Docker"));
            jobs.add(job);
        }
        return jobs;
    }

    /**
     * Test job fetcher
     */
    public static void testFetcher() {
        System.out.println("\n=== Testing JobFetcher ===");

        JobFetcher fetcher = new JobFetcher();

        // Test fetching jobs
        List<Job> jobs = fetcher.fetchJobs("Software Developer", 10);

        System.out.println("\nFetched " + jobs.size() + " jobs:");
        for (int i = 0; i < Math.min(3, jobs.size()); i++) {
            Job job = jobs.get(i);
            System.out.println("\n" + (i + 1) + ". " + job.getTitle() + " at " + job.getCompany());
            System.out.println("   Location: " + job.getLocation());
            System.out.println("   Required skills: " + job.getRequiredSkillsAsString());
        }

        System.out.println("\n=== JobFetcher Test Complete ===\n");
    }
}