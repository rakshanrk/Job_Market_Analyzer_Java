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

        // Sample Job 1: Java Developer
        Job job1 = new Job("Senior Java Developer", "Tech Corp",
                "Looking for experienced Java developer with Spring Boot, Microservices, AWS, and SQL skills. " +
                        "5+ years experience required. Docker and Kubernetes knowledge is a plus.");
        job1.setId("sample-1");
        job1.setLocation("San Francisco, CA");
        job1.setSalary(120000);
        job1.setUrl("https://example.com/job1");
        job1.setRequiredSkills(skillExtractor.extractSkills(job1.getDescription()));
        sampleJobs.add(job1);

        // Sample Job 2: Full Stack Developer
        Job job2 = new Job("Full Stack Developer", "StartUp Inc",
                "Full stack position requiring React, Node.js, MongoDB, and REST API experience. " +
                        "JavaScript, HTML, CSS, Git. Agile environment.");
        job2.setId("sample-2");
        job2.setLocation("New York, NY");
        job2.setSalary(110000);
        job2.setUrl("https://example.com/job2");
        job2.setRequiredSkills(skillExtractor.extractSkills(job2.getDescription()));
        sampleJobs.add(job2);

        // Sample Job 3: Data Scientist
        Job job3 = new Job("Data Scientist", "Analytics Co",
                "Seeking data scientist with Python, Machine Learning, TensorFlow, and SQL expertise. " +
                        "Statistics, Data Analysis, Pandas, NumPy required.");
        job3.setId("sample-3");
        job3.setLocation("Austin, TX");
        job3.setSalary(130000);
        job3.setUrl("https://example.com/job3");
        job3.setRequiredSkills(skillExtractor.extractSkills(job3.getDescription()));
        sampleJobs.add(job3);

        // Add more sample jobs...
        for (int i = 4; i <= 50; i++) {
            Job job = new Job(
                    "Software Engineer " + i,
                    "Company " + i,
                    "Looking for developer with Java, Python, SQL, Git, and Agile experience. " +
                            "Additional skills: Docker, AWS, React, Node.js."
            );
            job.setId("sample-" + i);
            job.setLocation("Various Locations");
            job.setSalary(100000 + (i * 1000));
            job.setUrl("https://example.com/job" + i);
            job.setRequiredSkills(skillExtractor.extractSkills(job.getDescription()));
            sampleJobs.add(job);
        }

        System.out.println("✅ Created " + sampleJobs.size() + " sample jobs for testing");
        return sampleJobs;
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