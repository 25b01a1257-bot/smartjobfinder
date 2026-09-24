package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InterviewQuestionSeedData {

    public static class QuestionDef {
        public String companyName;
        public String role;
        public String category; // Technical, HR, Behavioral, Role Specific
        public String question;
        public String answer;
        public String difficulty; // Beginner, Intermediate, Advanced
        public String skills;

        public QuestionDef(String companyName, String role, String category, String question, String answer, String difficulty, String skills) {
            this.companyName = companyName;
            this.role = role;
            this.category = category;
            this.question = question;
            this.answer = answer;
            this.difficulty = difficulty;
            this.skills = skills;
        }
    }

    public static void seedQuestionsIfEmpty(Connection conn) {
        if (conn == null) return;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM interview_questions");
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();

            if (count >= 50) {
                return; // Already populated
            }

            System.out.println("[InterviewQuestionSeedData] Seeding database with curated company & role interview questions...");
            List<QuestionDef> seedList = getCuratedSeedQuestions();

            String insertSql = "INSERT INTO interview_questions (company_name, role, category, question, answer, difficulty, skills) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (QuestionDef q : seedList) {
                    ps.setString(1, q.companyName);
                    ps.setString(2, q.role);
                    ps.setString(3, q.category);
                    ps.setString(4, q.question);
                    ps.setString(5, q.answer);
                    ps.setString(6, q.difficulty);
                    ps.setString(7, q.skills);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("[InterviewQuestionSeedData] ✅ Successfully seeded " + seedList.size() + " interview questions.");

        } catch (Exception e) {
            System.err.println("[InterviewQuestionSeedData] Seeding notice: " + e.getMessage());
        }
    }

    public static List<QuestionDef> getCuratedSeedQuestions() {
        List<QuestionDef> list = new ArrayList<>();

        // ==========================================
        // 1. GOOGLE - Specific Pack (DSA & System Design)
        // ==========================================
        list.add(new QuestionDef("Google", "Software Engineer", "Technical",
                "How does HashMap resolve collisions in Java 8+, and what is the time complexity when a bucket treeifies?",
                "Java 8+ HashMap resolves collisions using a linked list until the bucket reaches TREEIFY_THRESHOLD (8 items) and min table capacity >= 64, transforming it into a balanced Red-Black Tree. This improves worst-case lookup from O(n) to O(log n).",
                "Intermediate", "Java, Data Structures"));

        list.add(new QuestionDef("Google", "Software Engineer", "Technical",
                "Design an efficient algorithm to find the Median of Two Sorted Arrays in logarithmic time O(log(min(m, n))).",
                "Use binary search partitioning on the smaller array. Partition both arrays such that the left half elements <= right half elements. Verify maxLeftA <= minRightB and maxLeftB <= minRightA to calculate median in O(log(min(m,n))) time and O(1) space.",
                "Advanced", "DSA, Algorithms, Binary Search"));

        list.add(new QuestionDef("Google", "Software Engineer", "Role Specific",
                "How would you design Google's URL Shortener (like goo.gl) to handle 100M new URLs per day and 10B reads per day?",
                "Use Base62 encoding on a unique distributed 64-bit ID generator (Twitter Snowflake). Store URL mappings in distributed NoSQL (Bigtable/Spanner) with Redis caching for top 20% hot links. Employ multi-region load balancers with Anycast routing to achieve <10ms read latency.",
                "Advanced", "System Design, Scalability, Distributed Systems"));

        list.add(new QuestionDef("Google", "Software Engineer", "Role Specific",
                "Explain the CAP theorem and describe how Google Spanner provides external consistency despite network partitions.",
                "CAP states a distributed system can only guarantee two out of Consistency, Availability, and Partition Tolerance. Spanner uses TrueTime API with GPS receivers and atomic clocks to bound uncertainty (epsilon <= 7ms), enabling lock-free read transactions at global scale without breaking consistency.",
                "Advanced", "Distributed Systems, Consistency, Cloud"));

        list.add(new QuestionDef("Google", "Software Engineer", "Behavioral",
                "Describe a time when you discovered a subtle, critical bug before code deployment. How did you handle it?",
                "Situation: During final testing of an e-commerce microservice, I noticed sporadic transaction rollbacks under load. Task: Root-cause the issue without delaying launch. Action: Analyzed database deadlock logs, identified missing composite indexes on concurrent foreign key updates, and added isolated optimistic locking. Result: Eradicated deadlocks and kept release on schedule.",
                "Intermediate", "STAR, Problem Solving, Ownership"));

        list.add(new QuestionDef("Google", "Software Engineer", "HR",
                "Why Google, and how do you embrace 'Googleyness' in collaborative, ambiguous engineering environments?",
                "I thrive in environments where engineering excellence directly impacts billions. To me, Googleyness means intellectual humility, continuous curiosity, proactively helping teammates without ego, and doing what is right for the user even when difficult.",
                "Intermediate", "Cultural Fit, Communication"));

        // ==========================================
        // 2. AMAZON - Specific Pack (Leadership Principles & LLD)
        // ==========================================
        list.add(new QuestionDef("Amazon", "Software Engineer", "Technical",
                "Explain how you would design an In-Memory LRU Cache with O(1) get and put operations.",
                "Combine a Doubly Linked List with a HashMap. The HashMap stores keys mapped to Node references for O(1) lookup. The Doubly Linked List maintains access order: on access, move node to head; on capacity breach, remove tail node in O(1) time.",
                "Intermediate", "DSA, Java, Data Structures"));

        list.add(new QuestionDef("Amazon", "Software Engineer", "Role Specific",
                "How would you architect Amazon's Flash Sale / Lightning Deal checkout service to handle sudden traffic spikes without overselling?",
                "Use Redis distributed atomic counters with Lua scripts or DECR operations to deduct inventory atomically in-memory. Push verified purchase requests to an Amazon SQS queue. Async worker pools process queue orders into relational DB with optimistic locking.",
                "Advanced", "System Design, Microservices, AWS"));

        list.add(new QuestionDef("Amazon", "Software Engineer", "Behavioral",
                "Tell me about a time you demonstrated 'Customer Obsession' by pushing back on a feature or deadline.",
                "Situation: Our product roadmap planned to launch a checkout flow that skipped address validation to hit a Q3 target. Task: Advocate for buyer experience. Action: I collected delivery failure metrics showing 8% misdeliveries without validation, built a rapid POC integrating Google Places API in 2 days. Result: Avoided thousands of lost parcels and improved satisfaction by 18%.",
                "Intermediate", "STAR, Leadership Principles, Customer Obsession"));

        list.add(new QuestionDef("Amazon", "Software Engineer", "Behavioral",
                "Describe a scenario where you exercised 'Ownership' and solved a problem beyond your immediate job scope.",
                "Situation: Our team lacked an automated rollback mechanism during canary deployments. Task: Mitigate potential outage risks. Action: Over a weekend, I configured health-check alerting in CloudWatch and scripted automated traffic redirection in AWS Route53. Result: Prevented an outage when a bad deployment occurred two weeks later.",
                "Intermediate", "STAR, Ownership, Bias for Action"));

        list.add(new QuestionDef("Amazon", "Software Engineer", "HR",
                "How do you prioritize between speed of delivery (Bias for Action) and technical excellence (High Standards)?",
                "I classify decisions as Type 1 (irreversible, high impact) or Type 2 (two-way door, reversible). For Type 2 decisions, speed and iterative learning come first. For Type 1 core architectural choices (data consistency, security), I uphold rigorous high standards before writing production code.",
                "Intermediate", "Leadership Principles, Critical Thinking"));

        // ==========================================
        // 3. MICROSOFT - Specific Pack (OOP Design & Cloud)
        // ==========================================
        list.add(new QuestionDef("Microsoft", "Software Engineer", "Technical",
                "Explain the SOLID principles with concrete examples in Object-Oriented Programming.",
                "Single Responsibility (a class has one reason to change), Open/Closed (open for extension via interfaces, closed for modification), Liskov Substitution (subtypes must be substitutable for base types), Interface Segregation (small client-specific interfaces), and Dependency Inversion (depend upon abstractions, not concretions).",
                "Intermediate", "OOP, Design Patterns, Java/C#"));

        list.add(new QuestionDef("Microsoft", "Software Engineer", "Role Specific",
                "Explain how you would implement asynchronous distributed messaging using Azure Service Bus or Kafka.",
                "Producers publish event messages to topic exchanges with correlation IDs. Subscribers read partitions using consumer groups with dead-letter queue (DLQ) handlers for malformed payloads. At-least-once delivery with idempotent consumers prevents duplicate side-effects.",
                "Advanced", "Cloud, Azure, Microservices"));

        list.add(new QuestionDef("Microsoft", "Software Engineer", "Behavioral",
                "Tell me about a time you had a strong technical disagreement with a team member. How did you resolve it?",
                "Situation: A senior peer advocated storing raw JSON blobs in SQL, while I favored normalized tables with foreign keys. Task: Resolve without friction. Action: I created a benchmark script testing query latencies and index performance for 500k rows. Result: The data proved normalized schema was 4x faster; we aligned respectfully on data evidence.",
                "Intermediate", "STAR, Teamwork, Conflict Resolution"));

        list.add(new QuestionDef("Microsoft", "Software Engineer", "HR",
                "How do you embody Microsoft's Growth Mindset in your everyday coding journey?",
                "I view failures as learning data. When production bugs occur, I conduct blameless postmortems to understand architectural root causes, and I continuously seek feedback from peers through open code reviews to sharpen my engineering craft.",
                "Intermediate", "Growth Mindset, Culture Fit"));

        // ==========================================
        // 4. TCS - Specific Pack (Java, SQL & Aptitude)
        // ==========================================
        list.add(new QuestionDef("TCS", "Software Engineer", "Technical",
                "What is the difference between String, StringBuilder, and StringBuffer in Java?",
                "String is immutable and stored in the String Constant Pool. StringBuilder is mutable and non-synchronized (fastest, ideal for single-threaded operations). StringBuffer is mutable and thread-safe because its methods are synchronized, making it safer for multithreaded environments at the cost of overhead.",
                "Beginner", "Java, Core Java"));

        list.add(new QuestionDef("TCS", "Software Engineer", "Technical",
                "Explain SQL Clustered Index vs Non-Clustered Index with real-world analogies.",
                "A Clustered Index alters the physical storage order of table rows (like page numbers in a dictionary; only 1 per table). A Non-Clustered Index creates a separate lookup structure with pointers to the physical rows (like an index at the back of a textbook; multiple allowed per table).",
                "Beginner", "SQL, Database"));

        list.add(new QuestionDef("TCS", "Software Engineer", "Role Specific",
                "How do you prevent SQL Injection vulnerabilities in Java web applications?",
                "Always use PreparedStatements with parameterized queries (? placeholders) instead of string concatenation. Alternatively, use ORM frameworks (Hibernate/JPA) with parameterized HQL/JPQL queries, and sanitize/validate all input at API boundaries.",
                "Intermediate", "Security, Java, SQL"));

        list.add(new QuestionDef("TCS", "Software Engineer", "HR",
                "Are you willing to relocate to different client project locations and work in rotational shifts?",
                "Yes, absolutely. I understand enterprise client delivery often requires agility. I look forward to exploring new environments, client cultures, and working closely with distributed cross-functional teams.",
                "Beginner", "Flexibility, HR"));

        list.add(new QuestionDef("TCS", "Software Engineer", "Behavioral",
                "Describe a project deadline where you had to quickly learn a new technology to deliver on time.",
                "Situation: Our college capstone required building an automated notification pipeline using Docker, which was unfamiliar to our group. Task: Containerize application in 5 days. Action: I spent evenings following Docker tutorials, wrote multi-stage Dockerfiles, and debugged networking. Result: Deployed on time with full marks.",
                "Beginner", "STAR, Adaptability, Learning"));

        // ==========================================
        // 5. INFOSYS - Specific Pack (Relational DB, Aptitude & Pseudocode)
        // ==========================================
        list.add(new QuestionDef("Infosys", "Software Engineer", "Technical",
                "Explain the 4 ACID properties in Database Management Systems with a banking transaction example.",
                "Atomicity: Either full money transfer happens or none. Consistency: Balance constraints remain valid before and after. Isolation: Concurrent transactions don't interfere with each other. Durability: Once committed, transferred funds persist even if server power fails.",
                "Beginner", "SQL, DBMS, Database"));

        list.add(new QuestionDef("Infosys", "Software Engineer", "Technical",
                "Write an optimal SQL query to find the 2nd Highest Salary from an Employee table without using LIMIT/TOP.",
                "SELECT MAX(salary) FROM Employee WHERE salary < (SELECT MAX(salary) FROM Employee); Alternatively, using Window functions: SELECT salary FROM (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) as rnk FROM Employee) t WHERE t.rnk = 2;",
                "Intermediate", "SQL, DBMS, Queries"));

        list.add(new QuestionDef("Infosys", "Software Engineer", "Role Specific",
                "How do you design an exception handling strategy in a multi-tier Java application?",
                "Use custom domain unchecked exceptions (e.g. ResourceNotFoundException), catch technical exceptions at DAO/service layer and rethrow meaningful business exceptions, and employ a centralized @ControllerAdvice / Filter to return standardized JSON error envelopes with HTTP status codes.",
                "Intermediate", "Java, Clean Code, Architecture"));

        list.add(new QuestionDef("Infosys", "Software Engineer", "HR",
                "Why did you choose Infosys, and how will you adapt to the Mysore Training program?",
                "Infosys Mysore Global Education Centre is world-renowned for transforming graduates into industry-ready engineers. I am eager to immerse myself in the rigorous training modules, master enterprise standards, and contribute to client innovation.",
                "Beginner", "Company Alignment, HR"));

        // ==========================================
        // 6. ACCENTURE - Specific Pack (Scenario & Agile Delivery)
        // ==========================================
        list.add(new QuestionDef("Accenture", "Software Engineer", "Technical",
                "What is the difference between Synchronous and Asynchronous REST APIs, and when should you use WebSockets?",
                "Synchronous APIs block the client until HTTP response completes (suitable for immediate CRUD operations). Asynchronous APIs accept requests immediately with 202 Accepted and process via background queues. WebSockets provide full-duplex bi-directional communication for live streaming or real-time chats.",
                "Intermediate", "Web, APIs, REST, WebSockets"));

        list.add(new QuestionDef("Accenture", "Software Engineer", "Role Specific",
                "In an Agile Scrum team, what do you do if a assigned user story cannot be completed before sprint end?",
                "Immediately raise an impediment during the Daily Standup rather than waiting for sprint review. Communicate transparently with Scrum Master and Product Owner, break down remaining work, deliver the completed sub-tasks to QA, and roll over remaining scope to the next sprint backlog.",
                "Intermediate", "Agile, Scrum, Communication"));

        list.add(new QuestionDef("Accenture", "Software Engineer", "Behavioral",
                "Describe a situation where a client or stakeholder requested a major scope change right before milestone submission.",
                "Situation: Client requested additional payment gateway support 3 days before final delivery. Task: Handle request professionally without missing release. Action: Calculated additional effort, explained impact on current testing, and proposed phasing gateway as Phase 1.1 release next sprint. Result: Client agreed and milestone delivered on time.",
                "Intermediate", "STAR, Stakeholder Management, Agile"));

        // ==========================================
        // 7. COGNIZANT - Specific Pack (Full Stack & Communication)
        // ==========================================
        list.add(new QuestionDef("Cognizant", "Software Engineer", "Technical",
                "Explain the difference between optimistic locking and pessimistic locking in database transactions.",
                "Pessimistic locking locks the database row for the entire transaction (SELECT FOR UPDATE), preventing other threads from reading/writing. Optimistic locking does not lock rows; instead, it uses a version column and checks whether the version changed before committing, rolling back if conflicting.",
                "Intermediate", "Database, Concurrency, SQL"));

        list.add(new QuestionDef("Cognizant", "Software Engineer", "Role Specific",
                "How do you troubleshoot a sudden Memory Leak (java.lang.OutOfMemoryError) in a Java web server?",
                "Collect JVM heap dumps using `jcmd` or `-XX:+HeapDumpOnOutOfMemoryError`. Analyze memory allocations using Eclipse Memory Analyzer (MAT) or VisualVM to inspect dominator trees. Look for static collection leaks, unclosed database connections, or runaway thread pools.",
                "Intermediate", "Java, Performance, JVM"));

        list.add(new QuestionDef("Cognizant", "Software Engineer", "HR",
                "How do you handle working with cross-cultural team members in different time zones?",
                "I emphasize clear asynchronous written communication (well-documented Jira tickets, pull request descriptions), respect time-zone boundaries by scheduling overlapping standups, and establish unambiguous deliverables with clear acceptance criteria.",
                "Beginner", "Teamwork, Communication, HR"));

        return list;
    }
}
