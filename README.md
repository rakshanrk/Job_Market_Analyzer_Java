# 📊 Job Market Analyzer

<div align="center">

![Java](https://img.shields.io/badge/Java-21%2B-007396?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-23-FF6C37?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**AI-Powered Career Analysis Tool with Personalized Learning Paths**

[Features](#-features) • [Demo](#-demo) • [Installation](#-installation) • [Usage](#-usage) • [Architecture](#-architecture) • [Technologies](#-technologies)

</div>

---

## 🎯 Overview

**Job Market Analyzer** is an intelligent desktop application that helps job seekers identify skill gaps and provides personalized learning recommendations. Upload your resume, and within 60 seconds, receive:

- ✅ Detailed skill gap analysis against 50+ job postings
- ✅ AI-powered career path matching using K-Means clustering
- ✅ Personalized 4-week learning plan with real course links
- ✅ Interactive visualizations of your skill profile
- ✅ Job-specific matching percentages

---

## ✨ Features

### 🤖 AI-Powered Analysis
- **K-Means Clustering**: Groups similar career paths to find your best fit
- **NLP Skill Extraction**: Identifies 100+ technical skills using OpenNLP
- **Smart Matching**: Goes beyond simple keyword matching

### 📄 Multi-Format Resume Support
- PDF files (text-based and scanned)
- Image formats (PNG, JPG, JPEG, BMP, TIFF)
- Advanced OCR with Tesseract (85-95% accuracy)

### 📚 Personalized Learning Paths
- 4-week structured learning plans
- 40+ curated courses from Udemy, Coursera, YouTube
- Domain-specific recommendations (Web Dev, Data Science, DevOps, etc.)
- Progress milestones and project suggestions

### 📊 Visual Analytics
- Skill match percentage charts
- Top missing skills visualization
- Job-by-job analysis with color-coded results
- Export results to text files

### 🌐 Real Job Market Data
- Fetches 50+ job postings via Adzuna API
- Domain-specific job filtering
- Intelligent fallback with sample jobs

---

## 🎥 Demo

### Main Upload Screen
Upload your resume and select your target job domain.

### Analysis Results
View comprehensive skill analysis with interactive tabs:
- **Summary**: Matched vs. missing skills
- **Job Postings**: 50+ analyzed jobs with match percentages
- **Learning Path**: Your personalized 4-week plan
- **Charts**: Visual skill gap analysis

---

## 🚀 Installation

### Prerequisites

- **Java JDK 21 or higher**
- **Maven 3.x**
- **Tesseract OCR** (for image processing)

### Step 1: Clone the Repository

```bash
git clone https://github.com/rakshanrk/Job_Market_Analyzer_Java.git
cd Job_Market_Analyzer_Java
```

### Step 2: Install Tesseract OCR

**Windows:**
```bash
# Download installer from: https://github.com/UB-Mannheim/tesseract/wiki
# Install to: C:\Program Files\Tesseract-OCR
# Download tessdata: https://github.com/tesseract-ocr/tessdata
# Place eng.traineddata in C:\tessdata
```

**macOS:**
```bash
brew install tesseract
```

**Linux:**
```bash
sudo apt-get install tesseract-ocr
sudo apt-get install tesseract-ocr-eng
```

### Step 3: Build with Maven

```bash
mvn clean install
```

### Step 4: Run the Application

```bash
mvn javafx:run
```

**Or run the JAR:**
```bash
java -jar target/JobMarketAnalyzer-1.0-SNAPSHOT.jar
```

---

## 📖 Usage

### Basic Workflow

1. **Launch Application**
   - Run the application using Maven or the JAR file

2. **Select Job Domain**
   - Choose from: Software Developer, Data Scientist, Web Developer, etc.

3. **Upload Resume**
   - Click "Upload Resume" and select your PDF or image file
   - Maximum file size: 10MB

4. **Wait for Analysis** (30-60 seconds)
   - Text extraction
   - Skill identification using NLP
   - Job market analysis
   - AI-powered matching
   - Learning path generation

5. **View Results**
   - Explore 4 interactive tabs
   - View matched and missing skills
   - Check job-specific match percentages
   - Get your personalized 4-week learning plan

6. **Export Results**
   - Save analysis to text file for future reference

---

## 🏗️ Architecture

### Design Pattern: MVC-Inspired

```
├── models/              # Data structures (Skill, Job, Resume, etc.)
├── services/            # Business logic
│   ├── ResumeParser         # PDF & OCR processing
│   ├── SkillExtractor       # NLP-based skill identification
│   ├── JobFetcher           # API integration
│   ├── SkillAnalyzer        # K-Means clustering
│   └── LearningPathGenerator # Personalized recommendations
├── utils/               # Helper utilities
│   ├── FileValidator        # Input validation
│   └── ChartGenerator       # JFreeChart visualization
└── Main.java           # JavaFX GUI + Controller
```

### Key Components

#### 1. Resume Parser
- Extracts text from PDFs using Apache PDFBox
- OCR processing for images using Tesseract
- Text normalization and cleanup

#### 2. Skill Extractor
- Dictionary-based matching (100+ technical skills)
- NLP tokenization and POS tagging with OpenNLP
- Filters out common words to prevent false positives

#### 3. Job Fetcher
- Integrates with Adzuna Job Search API
- Intelligent fallback to domain-specific sample jobs
- Parses job descriptions to extract required skills

#### 4. Skill Analyzer (AI Core)
- **K-Means Clustering**: Groups similar skill profiles
- Creates n-dimensional feature vectors for resumes and jobs
- Calculates match percentage based on cluster similarity
- Identifies skill gaps with priority ranking

#### 5. Learning Path Generator
- Prioritizes missing skills by job market demand
- Queries SQLite database for relevant courses
- Creates structured 4-week learning plan
- Includes milestones and project suggestions

---

## 🛠️ Technologies

### Core Technologies
- **Java 21+**: Core programming language
- **JavaFX 23**: Modern GUI framework
- **Maven**: Dependency management and build tool

### AI & Machine Learning
- **Weka 3.8+**: K-Means clustering algorithm
- **Apache OpenNLP 2.3+**: NLP processing (tokenization, POS tagging)

### Data Processing
- **Apache PDFBox 3.0+**: PDF text extraction
- **Tesseract 5.x**: OCR engine for images
- **SQLite 3.46+**: Embedded database

### External Integrations
- **Adzuna API**: Real-time job market data
- **Apache HttpClient 5.x**: HTTP requests
- **Gson 2.11+**: JSON parsing

### Visualization
- **JFreeChart 1.5+**: Chart generation

---

## 📊 Algorithm: K-Means Clustering

### How It Works

1. **Feature Space Creation**
   - Collects all unique skills from resume and jobs
   - Example: [Java, Python, SQL, Docker, AWS, React]

2. **Vector Representation**
   - Resume: `[1, 1, 1, 0, 0, 0]` (has Java, Python, SQL)
   - Job 1: `[1, 0, 1, 1, 0, 0]` (needs Java, SQL, Docker)
   - Job 2: `[0, 1, 0, 0, 1, 1]` (needs Python, AWS, React)

3. **Clustering**
   - Groups similar skill profiles into 3 clusters
   - Cluster 0: Backend Developers
   - Cluster 1: Data Scientists
   - Cluster 2: DevOps Engineers

4. **Match Calculation**
   - Identifies resume's cluster
   - Counts jobs in same cluster
   - Match % = (same cluster jobs / total jobs) × 100

**Advantage**: Considers overall skill profile, not just individual skill overlap

---

## 📁 Database Schema

### Tables

#### 1. learning_resources
Stores 40+ curated courses mapped to skills
```sql
- skill_name (Java, Python, React, etc.)
- resource_title (Course name)
- resource_url (Link to Udemy, Coursera, YouTube)
- platform (Udemy, Coursera, YouTube)
- duration_weeks (Time to complete)
- difficulty_level (Beginner, Intermediate, Advanced)
```

#### 2. analysis_history
Tracks past analyses for progress monitoring
```sql
- resume_filename
- extracted_skills
- missing_skills
- match_percentage
- jobs_analyzed
- analysis_date
```

#### 3. learning_paths
Stores generated 4-week plans
```sql
- analysis_id (Foreign key)
- week_number (1-4)
- skill_focus (Skills for the week)
- resources (Course links)
```

---

## 🔧 Configuration

### API Setup (Optional)

To use real job data from Adzuna:

1. Sign up at [Adzuna Developer Portal](https://developer.adzuna.com/)
2. Get your API credentials (App ID and App Key)
3. Update `JobFetcher.java`:

```java
private static final String APP_ID = "your_app_id";
private static final String APP_KEY = "your_app_key";
```

**Note**: Application works perfectly with sample jobs if API is not configured.

### Tesseract Path Configuration

Update `ResumeParser.java` if Tesseract is installed in a custom location:

```java
tesseract.setDatapath("path/to/your/tessdata");
```

---

## 📈 Performance

- **Processing Time**: 30-60 seconds per resume
  - Text extraction: 5-20 seconds (PDF) or 10-30 seconds (Image OCR)
  - Skill extraction: 2-5 seconds
  - Job analysis: 5-10 seconds
  - AI clustering: 5-10 seconds
  - Path generation: 1-2 seconds

- **Accuracy**:
  - PDF text extraction: ~99%
  - OCR (images): 85-95% (depends on image quality)
  - Skill detection: 85-95%

- **Supported Files**: PDF, PNG, JPG, JPEG, BMP, TIFF (max 10MB)

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Areas for Contribution
- Add more learning resources to database
- Improve OCR accuracy
- Support additional file formats
- Enhance UI/UX
- Add more job domains
- Implement additional ML algorithms

---

## 🛣️ Roadmap

- [ ] Resume builder module
- [ ] LinkedIn profile import
- [ ] Job application tracker
- [ ] Skill verification quizzes
- [ ] Interview preparation questions
- [ ] Salary estimator based on skills
- [ ] Multiple career path suggestions
- [ ] Skill trend analysis dashboard
- [ ] Peer comparison feature
- [ ] Mobile app (iOS/Android)

---

## 👨‍💻 Author

**Rakshan RK**

- GitHub: [@rakshanrk](https://github.com/rakshanrk)
- LinkedIn: [https://www.linkedin.com/in/rakshanrk/]

---

## 🙏 Acknowledgments

- **Apache Software Foundation** for OpenNLP, PDFBox, and HttpClient
- **Weka Team** for the machine learning library
- **Tesseract Team** for OCR engine
- **Adzuna** for job market API
- **JFreeChart Team** for visualization library
- Course providers: Udemy, Coursera, YouTube

---

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/rakshanrk/Job_Market_Analyzer_Java/issues) page
2. Create a new issue with detailed description
3. Reach out via email

---

<div align="center">

[Back to Top](#-job-market-analyzer)

</div>
