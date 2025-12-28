Here is the complete, ready-to-paste content for your README.md file. I have updated the GitHub links to match your username (rakshanrk) and repository name (Job_Market_Analyzer_Java) visible in your screenshot.

Click the "Copy" button on the top right of the code block below, paste it into the editor in your screenshot, and then click "Commit changes...".

Markdown

# 📊 Job Market Analyzer & AI Career Coach

![Java](https://img.shields.io/badge/Java-21%2B-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-GUI-blue) ![Maven](https://img.shields.io/badge/Build-Maven-C71A36) ![AI](https://img.shields.io/badge/AI-KMeans%20%26%20NLP-purple) ![License](https://img.shields.io/badge/License-MIT-green)

**Job Market Analyzer** is an AI-powered desktop application designed to bridge the gap between job seekers and market demands. It analyzes resumes against real-world job postings using **Machine Learning (K-Means Clustering)** and **Natural Language Processing (NLP)** to identify skill gaps and generate a personalized **4-week learning path**.

---

## 📑 Table of Contents
- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [How It Works (AI & Algorithms)](#-how-it-works-ai--algorithms)
- [Installation & Setup](#-installation--setup)
- [Usage](#-usage)
- [Database Schema](#-database-schema)
- [Future Enhancements](#-future-enhancements)

---

## 🎯 Project Overview

Finding a job isn't just about having skills; it's about having the *right* skills for the specific role. This tool parses a user's resume (PDF or Image), compares it against a dataset of 50+ job postings for a selected domain, and uses clustering algorithms to determine career fit. It then goes a step further by creating a week-by-week study plan with direct links to learning resources.

---

## 🚀 Key Features

* **📄 Universal Resume Parsing:** Extracts text from **PDFs** (using Apache PDFBox) and **Images/Scans** (using Tesseract OCR).
* **🤖 AI-Driven Analysis:** Uses **K-Means Clustering** (Weka) to group job profiles and calculate a sophisticated "Fit Percentage" rather than simple keyword matching.
* **🧠 NLP Skill Extraction:** Utilizes **Apache OpenNLP** for tokenization and POS tagging to identify technical skills contextually.
* **📚 Personalized Learning Path:** Automatically generates a structured **4-week study plan** targeting your specific missing skills.
* **🔗 Integrated Resources:** Maps missing skills to actual courses (Udemy, Coursera, YouTube) stored in the local database.
* **📊 Visual Analytics:** Interactive charts (JFreeChart) showing skill gaps, market demand, and match statistics.
* **💾 History & Export:** Saves analysis history to SQLite and supports exporting results to text files.

---

## 🛠 Technology Stack

| Category | Technology | Usage |
| :--- | :--- | :--- |
| **Language** | Java 21+ | Core Application Logic |
| **GUI** | JavaFX | User Interface |
| **Build Tool** | Maven | Dependency Management |
| **Database** | SQLite | Local Data Storage (Resources, History) |
| **Machine Learning** | Weka | K-Means Clustering Algorithm |
| **NLP** | Apache OpenNLP | Tokenization & POS Tagging |
| **OCR** | Tesseract | Text extraction from images |
| **PDF Processing** | Apache PDFBox | Text extraction from PDFs |
| **Visualization** | JFreeChart | Bar & Horizontal Charts |

---

## 🏗 System Architecture

The project follows an **MVC-inspired architecture**:

```text
JobMarketAnalyzer/
├── src/main/java/com/jobanalyzer/
│   ├── models/           # Data Objects (Resume, Skill, Job)
│   ├── services/         # Business Logic (OCR, AI, DB Ops)
│   ├── utils/            # Helpers (Validation, Chart Generation)
│   └── Main.java         # Controller + JavaFX View
├── data/
│   └── jobanalyzer.db    # SQLite Database
└── resources/            # FXML, CSS, Trained Models
🧠 How It Works (AI & Algorithms)
1. Skill Extraction (NLP)
We use a hybrid approach for high accuracy:

Dictionary Matching: Checks against a master list of 100+ technical skills.

NLP Pipeline: Uses OpenNLP to tokenize text and identify Nouns (Parts-of-Speech tagging), allowing the system to find skills contextually even if they are slightly misspelled or formatted differently.

2. K-Means Clustering (Matching Engine)
Instead of a simple boolean check, we use Unsupervised Learning:

Vectorization: The system converts the Resume and all Job Postings into binary vectors (0s and 1s) based on the total skill universe.

Clustering: Weka's SimpleKMeans groups jobs into clusters (e.g., "Frontend", "Backend", "Data Science").

Classification: The resume is plotted in this N-dimensional space. The match percentage is derived from the resume's proximity to the cluster centroid of the target job domain.

3. Learning Path Generator
The system prioritizes missing skills based on their frequency in the job market. It then schedules the top 8 missing skills into a 4-week plan (2 skills per week), assigning relevant course links from the database.

💻 Installation & Setup
Prerequisites
Java JDK 21+

Maven

Tesseract OCR (Required for image processing)

Windows: Download Installer

Mac: brew install tesseract

Linux: sudo apt-get install tesseract-ocr

Steps to Run
Clone the Repo:

Bash

git clone [https://github.com/rakshanrk/Job_Market_Analyzer_Java.git](https://github.com/rakshanrk/Job_Market_Analyzer_Java.git)
cd Job_Market_Analyzer_Java
Configure Tesseract: Ensure the tessdata path in ResumeParser.java points to your local installation (e.g., C:\\Program Files\\Tesseract-OCR\\tessdata).

Build the Project:

Bash

mvn clean install
Run the App:

Bash

mvn javafx:run
📲 Usage
Launch: Open the application.

Select Domain: Choose a target job role (e.g., "Java Developer", "Data Scientist").

Upload: Click "Upload Resume" and select a PDF or Image file.

Analyze: Watch the progress bar as the system extracts text, fetches jobs, and runs the AI model.

Review:

Check the Summary tab for your Match %.

Go to Job Postings to see which specific jobs you qualify for.

Open Learning Path to see your 4-week study plan.

Use Charts to visualize your skill gaps.

🗄 Database Schema
The SQLite database (jobanalyzer.db) contains 4 main tables:

learning_resources: Stores course URLs, platform (Udemy/Coursera), and difficulty.

skills_master: The dictionary of known technical skills.

analysis_history: Logs past scans and results.

learning_paths: Stores generated schedules for user retrieval.

🔮 Future Enhancements
[ ] LinkedIn Integration: Import profile directly via URL.

[ ] Salary Predictor: Use regression models to estimate salary based on skill vectors.

[ ] Cloud Sync: Move database to MySQL/PostgreSQL for multi-user support.

[ ] Interview Prep: Generate questions based on the identified missing skills.

📄 License
Distributed under the MIT License. See LICENSE for more information.

Developed by Rakshan
