package com.codeassess.backend.config;

import com.codeassess.backend.assessment.Assessment;
import com.codeassess.backend.assessment.AssessmentStatus;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.problem.Difficulty;
import com.codeassess.backend.problem.Problem;
import com.codeassess.backend.repository.AssessmentRepository;
import com.codeassess.backend.repository.ProblemRepository;
import com.codeassess.backend.repository.TestCaseRepository;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.submission.Language;
import com.codeassess.backend.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            UserRepository userRepository,
            AssessmentRepository assessmentRepository,
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Starting demo data seeding...");

        createDemoUsers();

        User admin = userRepository.findByEmail("admin@codeassess.local")
                .orElseGet(() -> createAdminUser());

        if (assessmentRepository.count() == 0) {
            seedBackendScreening(admin);
            seedFullStackChallenge(admin);
            seedAIEngineeringChallenge(admin);
        }

        seedAdditionalAssessments(admin);
        logger.info("Demo data seeding completed successfully!");
    }

    private void createDemoUsers() {
        if (userRepository.findByEmail("candidate@codeassess.local").isEmpty()) {
            User candidate = new User(
                    "Demo Candidate",
                    "candidate@codeassess.local",
                    passwordEncoder.encode("CandidatePass123"),
                    Role.CANDIDATE
            );
            userRepository.save(candidate);
            logger.info("Created demo candidate: candidate@codeassess.local");
        }

        if (userRepository.findByEmail("candidate2@codeassess.local").isEmpty()) {
            User candidate2 = new User(
                    "Second Candidate",
                    "candidate2@codeassess.local",
                    passwordEncoder.encode("CandidatePass123"),
                    Role.CANDIDATE
            );
            userRepository.save(candidate2);
            logger.info("Created demo candidate: candidate2@codeassess.local");
        }
    }

    private User createAdminUser() {
        User admin = new User(
                "Admin User",
                "admin@codeassess.local",
                passwordEncoder.encode("AdminSecure123"),
                Role.ADMIN
        );
        return userRepository.save(admin);
    }

    private Assessment createAssessment(User admin, String title, String description, String instructions,
                                       Integer durationMinutes, Integer totalMarks) {
        Assessment assessment = new Assessment();
        assessment.setTitle(title);
        assessment.setDescription(description);
        assessment.setInstructions(instructions);
        assessment.setDurationMinutes(durationMinutes);
        assessment.setTotalMarks(totalMarks);
        assessment.setCreatedBy(admin);
        assessment.setStatus(AssessmentStatus.PUBLISHED);
        return assessmentRepository.save(assessment);
    }

    private Problem createProblem(Assessment assessment, String title, Difficulty difficulty, Integer points,
                                  String description, String inputFormat, String outputFormat, String constraints,
                                  String starterCode, Language starterLanguage) {
        Problem problem = new Problem();
        problem.setAssessment(assessment);
        problem.setTitle(title);
        problem.setDescription(description);
        problem.setDifficulty(difficulty);
        problem.setPoints(points);
        problem.setInputFormat(inputFormat);
        problem.setOutputFormat(outputFormat);
        problem.setConstraints(constraints);
        problem.setStarterCode(starterCode);
        problem.setStarterLanguage(starterLanguage);
        return problemRepository.save(problem);
    }

    private void createTestCase(Problem problem, String input, String expectedOutput, boolean hidden) {
        TestCase testCase = new TestCase();
        testCase.setProblem(problem);
        testCase.setInput(input);
        testCase.setExpectedOutput(expectedOutput);
        testCase.setHidden(hidden);
        testCase.setTimeLimitMs(2000);
        testCase.setMemoryLimitMb(256);
        testCaseRepository.save(testCase);
    }

    private void seedBackendScreening(User admin) {
        Assessment assessment = createAssessment(
                admin,
                "Backend Developer — Java & Algorithms",
                "Master the fundamentals of Data Structures and Algorithms using Java",
                "Solve all coding problems using the provided starter code. You may run your solution against public test cases before submitting. Your final submission will be evaluated against both public and hidden test cases.",
                45,
                100
        );

        Problem twoSum = createProblem(assessment, "Two Sum", Difficulty.EASY, 20,
                "Given an integer array nums and an integer target, return the indices of the two numbers whose sum equals target.\n\nYou may assume that exactly one valid pair exists. Return the indices in ascending order.",
                "Line 1: target (integer)\nLine 2: n, the length of nums\nLine 3: n space-separated integers",
                "The two indices, formatted exactly as [i, j] — square brackets, comma, single space.",
                "2 <= nums.length <= 100000\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9",
                starterTwoSum(), Language.JAVA);
        createTestCase(twoSum, "9\n4\n2 7 11 15", "[0, 1]", false);
        createTestCase(twoSum, "6\n3\n3 2 4", "[1, 2]", false);
        // nums[0] + nums[1] == -1 + -2 == -3, so the answer is [0, 1].
        createTestCase(twoSum, "-3\n4\n-1 -2 -3 -4", "[0, 1]", true);
        createTestCase(twoSum, "6\n2\n3 3", "[0, 1]", true);
        createTestCase(twoSum, "9\n5\n2 7 11 15 20", "[0, 1]", true);

        Problem longestValid = createProblem(assessment, "Longest Valid Parentheses", Difficulty.MEDIUM, 40,
                "Given a string containing only '(' and ')', find the length of the longest valid (well-formed) parentheses substring.",
                "A single line containing only the characters '(' and ')'. The line may be empty.",
                "A single integer: the length of the longest valid substring.",
                "0 <= s.length <= 100000",
                starterLongestValidParentheses(), Language.JAVA);
        // "()" is itself the longest valid substring, and its length is 2.
        createTestCase(longestValid, "()", "2", false);
        createTestCase(longestValid, ")()())", "4", false);
        createTestCase(longestValid, "(()", "2", true);
        createTestCase(longestValid, ")()())(()", "4", true);
        createTestCase(longestValid, "((()))", "6", true);

        Problem lruCache = createProblem(assessment, "LRU Cache", Difficulty.HARD, 40,
                "Design a Least Recently Used (LRU) cache supporting get and put operations in O(1) average time complexity.\n\nWhen the cache is full, a put must evict the least recently used key. Both get and put count as a use.",
                "Line 1: capacity (integer)\nRemaining lines: one operation per line, either put(key,value) or get(key)",
                "One line per get operation, containing the returned value, or -1 when the key is absent.",
                "1 <= capacity <= 10^4",
                starterLruCache(), Language.JAVA);
        createTestCase(lruCache, "2\nput(1,1)\nput(1,0)\nget(1)", "0", false);
        // get(1) -> 1, then put(3,3) evicts key 2, so get(2) -> -1. Two gets, two output lines.
        createTestCase(lruCache, "2\nput(1,1)\nput(2,2)\nget(1)\nput(3,3)\nget(2)", "1\n-1", true);
        // capacity 1: get(1) -> 1, put(2,2) evicts key 1, get(1) -> -1, get(2) -> 2.
        createTestCase(lruCache, "1\nput(1,1)\nget(1)\nput(2,2)\nget(1)\nget(2)", "1\n-1\n2", true);
    }

    private void seedFullStackChallenge(User admin) {
        Assessment assessment = createAssessment(
                admin,
                "Full Stack Developer Coding Challenge",
                "Multi-language full-stack problems testing algorithmic and system design thinking",
                "Solve problems in your preferred language. Focus on clean code, correct output format, and edge-case handling.",
                60,
                150
        );

        Problem anagram = createProblem(assessment, "Valid Anagram", Difficulty.EASY, 30,
                "Given two strings s and t, return true if t is an anagram of s, and false otherwise.",
                "Line 1: s\nLine 2: t",
                "true or false (lowercase).",
                "1 <= s.length, t.length <= 5 * 10^4",
                starterAnagram(), Language.JAVA);
        createTestCase(anagram, "listen\nsilent", "true", false);
        createTestCase(anagram, "hello\nworld", "false", false);
        createTestCase(anagram, "anagram\nnagaram", "true", true);
        createTestCase(anagram, "a\nb", "false", true);

        Problem mergeIntervals = createProblem(assessment, "Merge Intervals", Difficulty.MEDIUM, 60,
                "Given an array of intervals where intervals[i] = [start_i, end_i], merge all overlapping intervals and return the non-overlapping intervals that cover all the intervals in the input.\n\nIntervals that merely touch (for example [1,4] and [4,5]) are considered overlapping. Return the result sorted by start.",
                "A single line holding the interval list, for example [[1,3],[2,6],[8,10],[15,18]]",
                "The merged list in the same notation, with no spaces, for example [[1,6],[8,10],[15,18]]",
                "1 <= intervals.length <= 10^4",
                starterMergeIntervals(), Language.JAVA);
        createTestCase(mergeIntervals, "[[1,3],[2,6],[8,10],[15,18]]", "[[1,6],[8,10],[15,18]]", false);
        createTestCase(mergeIntervals, "[[1,4],[4,5]]", "[[1,5]]", true);
        createTestCase(mergeIntervals, "[[1,4],[2,3]]", "[[1,4]]", true);
        createTestCase(mergeIntervals, "[[5,7]]", "[[5,7]]", true);

        Problem apiCounter = createProblem(assessment, "API Request Counter", Difficulty.MEDIUM, 60,
                "You receive a sequence of API requests represented by their arrival timestamps in seconds. Given a window size T, determine the maximum number of requests that fall inside any window of T seconds.\n\nA window starting at time t covers the half-open interval [t, t + T), so a request at exactly t + T falls outside it.",
                "Line 1: space-separated non-decreasing integer timestamps\nLine 2: T, the window size in seconds",
                "A single integer: the maximum number of requests in any T-second window.",
                "1 <= timestamps.length <= 10^5\n1 <= T <= 10^9",
                starterApiCounter(), Language.JAVA);
        // Window [1,4) holds 1,2,3 and window [7,10) holds 7,8,9 — both 3.
        createTestCase(apiCounter, "1 2 3 7 8 9\n3", "3", false);
        createTestCase(apiCounter, "1 1 1 1 1\n2", "5", true);
        createTestCase(apiCounter, "100\n100", "1", true);
    }

    private void seedAIEngineeringChallenge(User admin) {
        Assessment assessment = createAssessment(
                admin,
                "AI Engineering & Data Processing Assessment",
                "Advanced problems in frequency analysis, streaming, and dependency resolution",
                "These problems test your ability to work with data streams, graph algorithms, and statistical processing. Choose your preferred language.",
                45,
                120
        );

        Problem frequency = createProblem(assessment, "Frequency Analyzer", Difficulty.EASY, 30,
                "Given a string, return the K most frequently occurring characters. Order by descending frequency; when two characters occur equally often, order them lexicographically.\n\nEvery character in the line counts, including spaces.",
                "Line 1: the input string (may contain spaces)\nLine 2: K",
                "The top K characters joined by commas, with no spaces around the commas, for example g,m,r",
                "1 <= s.length <= 10^5\n1 <= K <= 26",
                starterFrequencyAnalyzer(), Language.JAVA);
        createTestCase(frequency, "programming\n3", "g,m,r", false);
        // 'l' occurs 3 times and 'o' twice, so the answer has no leading separator.
        createTestCase(frequency, "hello world\n2", "l,o", true);
        createTestCase(frequency, "aaaaabbbbcc\n2", "a,b", true);

        Problem streaming = createProblem(assessment, "Streaming Statistics", Difficulty.MEDIUM, 40,
                "Given a stream of integers, output the running average after each new value, formatted to exactly 2 decimal places.\n\nFormat with a dot as the decimal separator regardless of the machine locale.",
                "Line 1: space-separated integers",
                "The running averages, space-separated, each with exactly 2 decimal places.",
                "1 <= values.length <= 10^4",
                starterStreamingStats(), Language.JAVA);
        createTestCase(streaming, "10 20 30 40", "10.00 15.00 20.00 25.00", false);
        createTestCase(streaming, "5", "5.00", true);
        createTestCase(streaming, "1 2 3 4 5", "1.00 1.50 2.00 2.50 3.00", true);

        Problem dependency = createProblem(assessment, "Dependency Resolution", Difficulty.HARD, 50,
                "Given a set of software modules and the dependency relationships between them, determine whether all modules can be built. A cycle in the dependency graph means the build is impossible.",
                "Line 1: N, the number of modules\nRemaining lines: one dependency per line in the form A->B, meaning A depends on B",
                "BUILDABLE or CYCLE",
                "1 <= N <= 1000",
                starterDependencyResolution(), Language.JAVA);
        createTestCase(dependency, "4\nA->B\nB->C\nC->D", "BUILDABLE", false);
        createTestCase(dependency, "3\nA->B\nB->C\nC->A", "CYCLE", true);
        createTestCase(dependency, "2\nA->B\nB->A", "CYCLE", true);
    }

        private void seedAdditionalAssessments(User admin) {
        seedIfMissing(admin, "Java Backend Engineer - Advanced", "Advanced Java, collections, concurrency, and system design", 60,
            new String[][]{
                {"LRU Cache Implementation", "HARD", "40", "Implement an LRU cache with O(1) average-time get and put operations using a HashMap and doubly linked list.", "2\\nput(1,1)\\nput(2,2)\\nget(1)\\nput(3,3)\\nget(2)", "1\\n-1"},
                {"Concurrent Task Scheduler", "HARD", "30", "Schedule tasks by priority and execution time. Preserve FIFO order for equal priorities and make the scheduler safe for concurrent callers.", "3\\ncompile:2\\nemail:1\\nbackup:3", "backup\\ncompile\\nemail"},
                {"Rate Limiter", "MEDIUM", "30", "Implement a sliding-window rate limiter that accepts at most limit requests during the previous window in seconds.", "5 10\\n1 2 3 4 5 6 11 12 13 14", "true true true true true false true true true true"}
            });
        seedIfMissing(admin, "Frontend React Developer", "React, TypeScript, asynchronous programming, and browser performance", 50,
            new String[][]{
                {"Debounced Search", "MEDIUM", "35", "Return the search results produced by the latest query after a debounce interval, ignoring stale asynchronous responses.", "cat\\ncat,catalog,dog", "cat,catalog"},
                {"Virtualized List", "MEDIUM", "35", "Render the visible slice of a large fixed-height list for a scroll offset and viewport size.", "100000 40 120 10", "4 5 6 7"},
                {"Form Validation Engine", "EASY", "30", "Validate required, email, minimum-length, and matching-field rules and return stable field error messages.", "email=bad;password=x;confirm=x", "email:invalid-email|password:min-length"}
            });
        seedIfMissing(admin, "Data Structures & Algorithms - Interview Challenge", "Arrays, hash maps, traversal, heaps, and complexity analysis", 60,
            new String[][]{
                {"Longest Consecutive Sequence", "MEDIUM", "30", "Find the length of the longest consecutive integer sequence in an unsorted array in O(n) expected time.", "100 4 200 1 3 2", "4"},
                {"Number of Islands", "MEDIUM", "35", "Count connected groups of land in a rectangular grid using four-directional BFS or DFS.", "3 4\\n1100\\n1101\\n0011", "2"},
                {"Top K Frequent Elements", "MEDIUM", "35", "Return the k most frequent values, breaking frequency ties by ascending numeric value.", "1 1 1 2 2 3\\n2", "1 2"}
            });
        seedIfMissing(admin, "SQL & Database Engineer Assessment", "PostgreSQL queries, relational modeling, and analytical SQL", 45,
            new String[][]{
                {"Second Highest Salary", "EASY", "30", "Return the second distinct highest salary from an employee table, or null when it does not exist. Expected complexity: one query.", "100\\n200\\n300\\n300", "200"},
                {"Department Top Performers", "MEDIUM", "35", "Join employees to departments and return every employee tied for the highest salary in their department.", "Engineering|Ada|200\\nEngineering|Lin|200\\nSales|Sam|150", "Engineering:Ada,Lin|Sales:Sam"},
                {"Customer Purchase Analytics", "MEDIUM", "35", "Use joins, GROUP BY, HAVING, a subquery, and a window function to rank customers whose total spend exceeds a threshold.", "A|10\\nA|20\\nB|5\\nC|50\\n10", "C:50:1|A:30:2"}
            });
        seedIfMissing(admin, "Full Stack Java + React Assessment", "Spring Boot, REST APIs, React, PostgreSQL, JWT, and RBAC", 75,
            new String[][]{
                {"REST API Pagination", "MEDIUM", "25", "Implement stable page and size pagination with total elements, total pages, and deterministic ordering.", "23 5 2", "11 5 6 7 8 9"},
                {"Product Search & Filtering", "MEDIUM", "35", "Filter products by a case-insensitive query and price range, then sort by relevance and price.", "phone 10 100|Phone Case|25|Desk Lamp|40|Phone Stand|30", "Phone Case|Phone Stand"},
                {"Role-Based Access Control", "HARD", "40", "Authorize an operation when a user has a required role directly or through an inherited role, denying unknown permissions by default.", "ADMIN:reports:read\\nUSER:reports:read", "true false"}
            });
        seedIfMissing(admin, "Algorithms - Graph & Trees", "Trees, graphs, BFS, DFS, topological sorting, and Dijkstra", 60,
            new String[][]{
                {"Binary Tree Lowest Common Ancestor", "MEDIUM", "30", "Find the lowest common ancestor of two values in a binary tree in O(n) time.", "3 5|6 2 8 0 4 7 9", "2"},
                {"Course Schedule", "MEDIUM", "30", "Determine whether all courses can be completed from prerequisite pairs using topological sorting.", "4\\n1 0\\n2 1\\n3 2", "true"},
                {"Shortest Path in Weighted Graph", "HARD", "40", "Return shortest distances from a source in a non-negative weighted graph using Dijkstra's algorithm.", "4 4 0\\n0 1 4\\n0 2 1\\n2 1 2\\n1 3 1", "0 3 1 4"}
            });
        seedIfMissing(admin, "API & Backend Engineering", "REST APIs, hashing, concurrency, idempotency, and backend design", 60,
            new String[][]{
                {"URL Shortener Service", "MEDIUM", "20", "Create deterministic short URLs for long URLs and resolve them without collisions for repeated inputs.", "https://example.com/jobs/123", "aHR0cHM6Ly9leGFtcGxlLmNvbS9qb2JzLzEyMw"},
                {"API Request Counter", "MEDIUM", "20", "Count requests per client in a rolling time window and report the maximum count without double-counting timestamps.", "a 1 2 3 7\\nb 2 4\\n5", "a:3|b:2"},
                {"Idempotent Payment Request", "HARD", "40", "Process a payment key once, return the original result for retries, and reject conflicting payloads for the same key.", "pay-1|100|pay-1|100|pay-1|120", "PROCESSED 100|REPLAYED 100|CONFLICT"}
            });
        seedIfMissing(admin, "AI / Machine Learning Engineer", "Python, NLP fundamentals, vectors, and machine learning concepts", 60,
            new String[][]{
                {"Text Frequency Analyzer", "EASY", "20", "Tokenize text case-insensitively and return the k most frequent words, breaking ties lexicographically.", "AI helps teams. AI helps users.\\n2", "ai:2 helps:2"},
                {"Cosine Similarity", "MEDIUM", "30", "Compute cosine similarity for two sparse vectors, returning 0 when either vector has zero magnitude.", "1 2 3\\n2 4 6", "1.0000"},
                {"Simple K-Means Clustering", "HARD", "50", "Assign points to k clusters and update centroids until convergence or a fixed iteration limit.", "2 2 1\\n0,0\\n0,2\\n10,10\\n10,12", "0 0 1 1"}
            });
        seedIfMissing(admin, "Software Engineering Fundamentals", "Programming fundamentals, arrays, stacks, and clean code", 45,
            new String[][]{
                {"Palindrome Validator", "EASY", "15", "Return whether a string is a palindrome after ignoring case and non-alphanumeric characters.", "A man, a plan, a canal: Panama", "true"},
                {"Balanced Brackets", "EASY", "15", "Validate that every bracket is closed in the correct order.", "{[()]}", "true"},
                {"Merge Sorted Arrays", "MEDIUM", "30", "Merge two sorted integer arrays into one sorted array in linear time.", "1 3 5\\n2 4 6", "1 2 3 4 5 6"}
            });
        seedIfMissing(admin, "System Design Coding Challenge", "Object-oriented design, concurrency, scalability, and data structures", 90,
            new String[][]{
                {"Design a Thread-Safe Cache", "HARD", "30", "Design a bounded cache with synchronized reads and writes, eviction, and clear semantics.", "put a 1\\nget a\\nput b 2\\nget b", "1 2"},
                {"Design a Notification Queue", "HARD", "30", "Design a queue that preserves per-user ordering, retries transient failures, and dead-letters exhausted messages.", "u1:m1\\nu1:m2\\nu2:m3", "u1:m1|u1:m2|u2:m3"},
                {"Design a Distributed Rate Limiter", "HARD", "40", "Design a shared token-bucket limiter with atomic refill and fail-closed behavior when the backing store is unavailable.", "api 3 1\\napi api api api", "true true true false"}
            });
        }

        private void seedIfMissing(User admin, String title, String description, int durationMinutes,
                       String[][] problems) {
        if (assessmentRepository.findByTitle(title).isPresent()) {
            return;
        }

        int totalMarks = java.util.Arrays.stream(problems)
            .mapToInt(problem -> Integer.parseInt(problem[2]))
            .sum();
        Assessment assessment = createAssessment(admin, title, description,
            "Solve all three problems. Public cases are shown for development; hidden cases check edge cases, correctness, and the stated complexity.",
            durationMinutes, totalMarks);

        for (String[] problem : problems) {
            Problem created = createProblem(assessment, problem[0], Difficulty.valueOf(problem[1]),
                Integer.parseInt(problem[2]), problem[3],
                "Input is provided as described in the examples. Parse whitespace and delimiters defensively.",
                "Print the expected result using the format shown in the examples.",
                "Use an efficient solution appropriate for production data. Expected complexity: O(n) or O(n log n) unless the problem requires otherwise.",
                starterForCatalogProblem(problem[0]), Language.JAVA);
            createTestCase(created, decodeCatalogText(problem[4]), decodeCatalogText(problem[5]), false);
            for (String[] hiddenCase : hiddenCatalogCases(problem[0])) {
                createTestCase(created, decodeCatalogText(hiddenCase[0]), decodeCatalogText(hiddenCase[1]), true);
            }
        }
        }

        private String decodeCatalogText(String text) {
        return text.replace("\\\\n", "\n").replace("\\n", "\n");
        }

        private String[][] hiddenCatalogCases(String title) {
        return switch (title) {
            case "LRU Cache Implementation" -> new String[][]{{"1\\\\nput(1,1)\\\\nget(1)\\\\nput(2,2)\\\\nget(1)\\\\nget(2)", "1\\\\n-1\\\\n2"}, {"3\\\\nput(1,1)\\\\nput(2,2)\\\\nput(3,3)\\\\nget(1)\\\\nput(4,4)\\\\nget(2)", "1\\\\n-1"}};
            case "Concurrent Task Scheduler" -> new String[][]{{"2\\\\na:1\\\\nb:1", "a\\\\nb"}, {"4\\\\nslow:4\\\\nfast:1\\\\nmedium:2\\\\nfast-2:1", "fast\\\\nfast-2\\\\nmedium\\\\nslow"}};
            case "Rate Limiter" -> new String[][]{{"2 3\\\\n1 2 4", "true true false"}, {"1 1\\\\n10", "true"}};
            case "Debounced Search" -> new String[][]{{"z\\\\ncat,dog", ""}, {"app\\\\napple,application,banana", "apple,application"}};
            case "Virtualized List" -> new String[][]{{"10 0 40 10", "0 1 2 3"}, {"5 40 20 10", "4"}};
            case "Form Validation Engine" -> new String[][]{{"email=a@b.com;password=secret;confirm=secret", ""}, {"email=;password=;confirm=x", "email:required|password:required|confirm:mismatch"}};
            case "Longest Consecutive Sequence" -> new String[][]{{"", "0"}, {"9 1 4 7 3 2 6 5", "7"}};
            case "Number of Islands" -> new String[][]{{"1 1\\\\n1", "1"}, {"4 5\\\\n11110\\\\n11010\\\\n11000\\\\n00000", "1"}};
            case "Top K Frequent Elements" -> new String[][]{{"4 4 4 2 2 1\\\\n1", "4"}, {"5 6 5 6 7 7\\\\n3", "5 6 7"}};
            case "Second Highest Salary" -> new String[][]{{"100\\\\n100", "null"}, {"500\\\\n100\\\\n300\\\\n200", "300"}};
            case "Department Top Performers" -> new String[][]{{"Engineering|Ada|200", "Engineering:Ada"}, {"Sales|Sam|150\\\\nSales|Jo|200\\\\nSales|Bea|200", "Sales:Jo,Bea"}};
            case "Customer Purchase Analytics" -> new String[][]{{"A|5\\\\nB|5\\\\n5", ""}, {"A|20\\\\nA|20\\\\nB|50\\\\nC|1\\\\n10", "B:50:1|A:40:2"}};
            case "REST API Pagination" -> new String[][]{{"3 5 0", "1 3 1 2 3"}, {"0 10 0", "0 0"}};
            case "Product Search & Filtering" -> new String[][]{{"phone 0 30|Phone Case|25|Desk Lamp|40|Phone Stand|30", "Phone Case"}, {"tablet 10 100|Phone Case|25|Desk Lamp|40", ""}};
            case "Role-Based Access Control" -> new String[][]{{"ADMIN:users:read\\\\nUSER:users:read", "true true"}, {"GUEST:reports:read\\\\nUSER:reports:write", "false false"}};
            case "Binary Tree Lowest Common Ancestor" -> new String[][]{{"2 8|6 2 8 0 4 7 9", "6"}, {"0 4|6 2 8 0 4 7 9", "2"}};
            case "Course Schedule" -> new String[][]{{"2\\\\n1 0\\\\n0 1", "false"}, {"3\\\\n1 0\\\\n2 0", "true"}};
            case "Shortest Path in Weighted Graph" -> new String[][]{{"3 3 0\\\\n0 1 2\\\\n1 2 3\\\\n0 2 10", "0 2 5"}, {"2 0 1", "-1 0"}};
            case "URL Shortener Service" -> new String[][]{{"https://example.com", "aHR0cHM6Ly9leGFtcGxlLmNvbQ"}, {"https://example.com/jobs/456", "aHR0cHM6Ly9leGFtcGxlLmNvbS9qb2JzLzQ1Ng"}};
            case "API Request Counter" -> new String[][]{{"a 1 1 1\\\\nb 2\\\\n1", "a:3|b:1"}, {"a 10 20\\\\n10", "a:1"}};
            case "Idempotent Payment Request" -> new String[][]{{"x|1|x|1", "PROCESSED 1|REPLAYED 1"}, {"x|1|y|2", "PROCESSED 1|PROCESSED 2"}};
            case "Text Frequency Analyzer" -> new String[][]{{"one one two\\\\n1", "one:2"}, {"Java JAVA code\\\\n2", "java:2 code:1"}};
            case "Cosine Similarity" -> new String[][]{{"1 0\\\\n0 1", "0.0000"}, {"0 0\\\\n1 2", "0.0000"}};
            case "Simple K-Means Clustering" -> new String[][]{{"1 1 1\\\\n5,5", "0"}, {"2 1 1\\\\n-1,-1\\\\n1,1", "0 1"}};
            case "Palindrome Validator" -> new String[][]{{"", "true"}, {"race a car", "false"}};
            case "Balanced Brackets" -> new String[][]{{"", "true"}, {"([)]", "false"}};
            case "Merge Sorted Arrays" -> new String[][]{{"\\\\n1 2 3", "1 2 3"}, {"-3 0 8\\\\n-2 4 9", "-3 -2 0 4 8 9"}};
            case "Design a Thread-Safe Cache" -> new String[][]{{"put a 1\\\\nget a\\\\nget c", "1 -1"}, {"put a 1\\\\nput b 2\\\\nput c 3\\\\nget a\\\\nget b\\\\nget c", "-1 2 3"}};
            case "Design a Notification Queue" -> new String[][]{{"u1:m1", "u1:m1"}, {"u2:m1\\\\nu1:m1\\\\nu2:m2", "u2:m1|u2:m2|u1:m1"}};
            case "Design a Distributed Rate Limiter" -> new String[][]{{"api 1 10\\\\napi api", "true false"}, {"other 2 1\\\\nother other other", "true true false"}};
            default -> throw new IllegalArgumentException("Missing hidden catalog cases for " + title);
        };
        }

        private String starterForCatalogProblem(String title) {
        return switch (title) {
            case "LRU Cache Implementation" -> starterLruCacheImplementation();
            case "Concurrent Task Scheduler" -> starterConcurrentTaskScheduler();
            case "Rate Limiter" -> starterRateLimiter();
            case "Debounced Search" -> starterDebouncedSearch();
            case "Virtualized List" -> starterVirtualizedList();
            case "Form Validation Engine" -> starterFormValidationEngine();
            case "Longest Consecutive Sequence" -> starterLongestConsecutiveSequence();
            case "Number of Islands" -> starterNumberOfIslands();
            case "Top K Frequent Elements" -> starterTopKFrequentElements();
            case "Second Highest Salary" -> starterSecondHighestSalary();
            case "Department Top Performers" -> starterDepartmentTopPerformers();
            case "Customer Purchase Analytics" -> starterCustomerPurchaseAnalytics();
            case "REST API Pagination" -> starterRestApiPagination();
            case "Product Search & Filtering" -> starterProductSearchFiltering();
            case "Role-Based Access Control" -> starterRoleBasedAccessControl();
            case "Binary Tree Lowest Common Ancestor" -> starterLowestCommonAncestor();
            case "Course Schedule" -> starterCourseSchedule();
            case "Shortest Path in Weighted Graph" -> starterShortestPath();
            case "URL Shortener Service" -> starterUrlShortener();
            case "API Request Counter" -> starterApiRequestCounter();
            case "Idempotent Payment Request" -> starterIdempotentPayment();
            case "Text Frequency Analyzer" -> starterTextFrequencyAnalyzer();
            case "Cosine Similarity" -> starterCosineSimilarity();
            case "Simple K-Means Clustering" -> starterKMeansClustering();
            case "Palindrome Validator" -> starterPalindromeValidator();
            case "Balanced Brackets" -> starterBalancedBrackets();
            case "Merge Sorted Arrays" -> starterMergeSortedArrays();
            case "Design a Thread-Safe Cache" -> starterThreadSafeCache();
            case "Design a Notification Queue" -> starterNotificationQueue();
            case "Design a Distributed Rate Limiter" -> starterDistributedRateLimiter();
            default -> throw new IllegalArgumentException("Missing starter template for " + title);
        };
        }

        private String catalogJavaStarter(String methods, String main) {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;
                import java.util.*;

                public class Main {
                """ + methods + "\n" + main + "\n}\n";
        }

        private String starterLruCacheImplementation() {
        return catalogJavaStarter("""
                static class LruCache {
                    private final int capacity;
                    private final LinkedHashMap<Integer, Integer> entries;

                    LruCache(int capacity) {
                        this.capacity = capacity;
                        this.entries = new LinkedHashMap<>(16, 0.75f, true);
                    }

                    int get(int key) {
                        return -1;
                    }

                    void put(int key, int value) {
                    }
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    LruCache cache = new LruCache(Integer.parseInt(input.readLine().trim()));
                    String line;
                    while ((line = input.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        String[] parts = line.substring(line.indexOf('(') + 1, line.length() - 1).split(",");
                        if (line.startsWith("put")) {
                            cache.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
                        } else {
                            System.out.println(cache.get(Integer.parseInt(parts[0].trim())));
                        }
                    }
                }
                """);
        }

        private String starterConcurrentTaskScheduler() {
        return catalogJavaStarter("""
                static List<String> scheduleTasks(List<String> taskDefinitions) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    int count = Integer.parseInt(input.readLine().trim());
                    List<String> tasks = new ArrayList<>();
                    for (int i = 0; i < count; i++) tasks.add(input.readLine());
                    for (String task : scheduleTasks(tasks)) System.out.println(task);
                }
                """);
        }

        private String starterRateLimiter() {
        return catalogJavaStarter("""
                static boolean[] allowRequests(int limit, int windowSeconds, int[] timestamps) {
                    return new boolean[timestamps.length];
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String[] settings = input.readLine().trim().split("\\\\s+");
                    String[] values = input.readLine().trim().split("\\\\s+");
                    int[] timestamps = Arrays.stream(values).mapToInt(Integer::parseInt).toArray();
                    boolean[] allowed = allowRequests(Integer.parseInt(settings[0]), Integer.parseInt(settings[1]), timestamps);
                    for (int i = 0; i < allowed.length; i++) System.out.print((i == 0 ? "" : " ") + allowed[i]);
                }
                """);
        }

        private String starterDebouncedSearch() {
        return catalogJavaStarter("""
                static List<String> searchLatestQuery(String query, List<String> candidates) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String query = input.readLine();
                    List<String> candidates = Arrays.asList(input.readLine().split(","));
                    System.out.println(String.join(",", searchLatestQuery(query, candidates)));
                }
                """);
        }

        private String starterVirtualizedList() {
        return catalogJavaStarter("""
                static List<Integer> visibleIndexes(int itemCount, int itemHeight, int scrollOffset, int viewportHeight) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    int[] values = Arrays.stream(input.readLine().trim().split("\\\\s+")).mapToInt(Integer::parseInt).toArray();
                    System.out.println(visibleIndexes(values[0], values[1], values[2], values[3]).stream().map(String::valueOf).reduce((a, b) -> a + " " + b).orElse(""));
                }
                """);
        }

        private String starterFormValidationEngine() {
        return catalogJavaStarter("""
                static String validateForm(String serializedFields) {
                    return "";
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println(validateForm(input.readLine()));
                }
                """);
        }

        private String starterLongestConsecutiveSequence() {
        return catalogJavaStarter("""
                static int longestConsecutive(int[] values) {
                    return 0;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String line = input.readLine();
                    int[] values = line == null || line.isBlank() ? new int[0] : Arrays.stream(line.trim().split("\\\\s+")).mapToInt(Integer::parseInt).toArray();
                    System.out.println(longestConsecutive(values));
                }
                """);
        }

        private String starterNumberOfIslands() {
        return catalogJavaStarter("""
                static int countIslands(char[][] grid) {
                    return 0;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String[] dimensions = input.readLine().trim().split("\\\\s+");
                    char[][] grid = new char[Integer.parseInt(dimensions[0])][];
                    for (int row = 0; row < grid.length; row++) grid[row] = input.readLine().trim().toCharArray();
                    System.out.println(countIslands(grid));
                }
                """);
        }

        private String starterTopKFrequentElements() {
        return catalogJavaStarter("""
                static int[] topKFrequent(int[] values, int k) {
                    return new int[0];
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    int[] values = Arrays.stream(input.readLine().trim().split("\\\\s+")).mapToInt(Integer::parseInt).toArray();
                    int k = Integer.parseInt(input.readLine().trim());
                    System.out.println(Arrays.stream(topKFrequent(values, k)).mapToObj(String::valueOf).reduce((a, b) -> a + " " + b).orElse(""));
                }
                """);
        }

        private String starterSecondHighestSalary() {
        return catalogJavaStarter("""
                static Integer secondHighestSalary(int[] salaries) {
                    return null;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    List<Integer> salaries = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) salaries.add(Integer.parseInt(line.trim()));
                    System.out.println(secondHighestSalary(salaries.stream().mapToInt(Integer::intValue).toArray()));
                }
                """);
        }

        private String starterDepartmentTopPerformers() {
        return catalogJavaStarter("""
                static List<String> topPerformers(List<String> employeeRows) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    List<String> rows = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) rows.add(line);
                    System.out.println(String.join("|", topPerformers(rows)));
                }
                """);
        }

        private String starterCustomerPurchaseAnalytics() {
        return catalogJavaStarter("""
                static List<String> rankCustomers(List<String> purchases, int minimumSpend) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    List<String> rows = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null) rows.add(line);
                    int threshold = Integer.parseInt(rows.remove(rows.size() - 1).trim());
                    System.out.println(String.join("|", rankCustomers(rows, threshold)));
                }
                """);
        }

        private String starterRestApiPagination() {
        return catalogJavaStarter("""
                static List<Integer> page(int totalElements, int pageSize, int pageNumber) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    int[] values = Arrays.stream(input.readLine().trim().split("\\\\s+")).mapToInt(Integer::parseInt).toArray();
                    System.out.println(page(values[0], values[1], values[2]).stream().map(String::valueOf).reduce((a, b) -> a + " " + b).orElse(""));
                }
                """);
        }

        private String starterProductSearchFiltering() {
        return catalogJavaStarter("""
                static List<String> filterProducts(String query, int minimumPrice, int maximumPrice, List<String> products) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String[] records = input.readLine().split("\\\\|");
                    String[] header = records[0].split(" ");
                    List<String> products = Arrays.asList(Arrays.copyOfRange(records, 1, records.length));
                    System.out.println(String.join("|", filterProducts(header[0], Integer.parseInt(header[1]), Integer.parseInt(header[2]), products)));
                }
                """);
        }

        private String starterRoleBasedAccessControl() {
        return catalogJavaStarter("""
                static boolean[] authorize(List<String> requests) {
                    return new boolean[requests.size()];
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    List<String> requests = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) requests.add(line);
                    boolean[] result = authorize(requests);
                    for (int i = 0; i < result.length; i++) System.out.print((i == 0 ? "" : " ") + result[i]);
                }
                """);
        }

        private String starterLowestCommonAncestor() {
        return catalogJavaStarter("""
                static int lowestCommonAncestor(int first, int second, int[] levelOrder) {
                    return 0;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String[] parts = input.readLine().split("\\\\|");
                    int[] targets = Arrays.stream(parts[0].trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                    int[] tree = Arrays.stream(parts[1].trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                    System.out.println(lowestCommonAncestor(targets[0], targets[1], tree));
                }
                """);
        }

        private String starterCourseSchedule() {
        return catalogJavaStarter("""
                static boolean canFinish(int courseCount, int[][] prerequisites) {
                    return false;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    int count = Integer.parseInt(input.readLine().trim());
                    List<int[]> pairs = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) {
                        int[] pair = Arrays.stream(line.trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                        pairs.add(pair);
                    }
                    System.out.println(canFinish(count, pairs.toArray(new int[0][])));
                }
                """);
        }

        private String starterShortestPath() {
        return catalogJavaStarter("""
                static int[] shortestPaths(int nodeCount, int source, int[][] edges) {
                    return new int[nodeCount];
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    int[] header = Arrays.stream(input.readLine().trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                    List<int[]> edges = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) edges.add(Arrays.stream(line.split(" ")).mapToInt(Integer::parseInt).toArray());
                    System.out.println(Arrays.stream(shortestPaths(header[0], header[2], edges.toArray(new int[0][]))).mapToObj(String::valueOf).reduce((a, b) -> a + " " + b).orElse(""));
                }
                """);
        }

        private String starterUrlShortener() {
        return catalogJavaStarter("""
                static String shorten(String url) {
                    return "";
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println(shorten(input.readLine()));
                }
                """);
        }

        private String starterApiRequestCounter() {
        return catalogJavaStarter("""
                static Map<String, Integer> countRequests(Map<String, List<Integer>> requests, int windowSeconds) {
                    return new LinkedHashMap<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    Map<String, List<Integer>> requests = new LinkedHashMap<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) {
                        String[] parts = line.split(" ");
                        List<Integer> times = new ArrayList<>();
                        for (int i = 1; i < parts.length; i++) times.add(Integer.parseInt(parts[i]));
                        requests.put(parts[0], times);
                    }
                    String windowRow = requests.keySet().stream().reduce((first, second) -> second).orElse("0");
                    int window = Integer.parseInt(windowRow);
                    requests.remove(windowRow);
                    countRequests(requests, window).forEach((key, value) -> System.out.println(key + ":" + value));
                }
                """);
        }

        private String starterIdempotentPayment() {
        return catalogJavaStarter("""
                static List<String> processPayments(List<String> requests) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    List<String> requests = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) requests.add(line);
                    System.out.println(String.join("|", processPayments(requests)));
                }
                """);
        }

        private String starterTextFrequencyAnalyzer() {
        return catalogJavaStarter("""
                static List<String> topWords(String text, int k) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String text = input.readLine();
                    int k = Integer.parseInt(input.readLine().trim());
                    System.out.println(String.join(" ", topWords(text == null ? "" : text, k)));
                }
                """);
        }

        private String starterCosineSimilarity() {
        return catalogJavaStarter("""
                static double cosineSimilarity(double[] first, double[] second) {
                    return 0.0;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    double[] first = Arrays.stream(input.readLine().trim().split(" ")).mapToDouble(Double::parseDouble).toArray();
                    double[] second = Arrays.stream(input.readLine().trim().split(" ")).mapToDouble(Double::parseDouble).toArray();
                    System.out.printf(Locale.ROOT, "%.4f%n", cosineSimilarity(first, second));
                }
                """);
        }

        private String starterKMeansClustering() {
        return catalogJavaStarter("""
                static int[] cluster(double[][] points, int clusterCount, int iterations) {
                    return new int[points.length];
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String[] header = input.readLine().trim().split(" ");
                    int clusterCount = Integer.parseInt(header[0]);
                    int iterations = Integer.parseInt(header[1]);
                    List<double[]> points = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) points.add(Arrays.stream(line.split(",")).mapToDouble(Double::parseDouble).toArray());
                    System.out.println(Arrays.stream(cluster(points.toArray(new double[0][]), clusterCount, iterations)).mapToObj(String::valueOf).reduce((a, b) -> a + " " + b).orElse(""));
                }
                """);
        }

        private String starterPalindromeValidator() {
        return catalogJavaStarter("""
                static boolean isPalindrome(String value) {
                    return false;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String value = input.readLine();
                    System.out.println(isPalindrome(value == null ? "" : value));
                }
                """);
        }

        private String starterBalancedBrackets() {
        return catalogJavaStarter("""
                static boolean isBalanced(String value) {
                    return false;
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String value = input.readLine();
                    System.out.println(isBalanced(value == null ? "" : value));
                }
                """);
        }

        private String starterMergeSortedArrays() {
        return catalogJavaStarter("""
                static int[] mergeSorted(int[] first, int[] second) {
                    return new int[0];
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    int[] first = Arrays.stream(input.readLine().trim().split(" ")).filter(value -> !value.isBlank()).mapToInt(Integer::parseInt).toArray();
                    int[] second = Arrays.stream(input.readLine().trim().split(" ")).filter(value -> !value.isBlank()).mapToInt(Integer::parseInt).toArray();
                    System.out.println(Arrays.stream(mergeSorted(first, second)).mapToObj(String::valueOf).reduce((a, b) -> a + " " + b).orElse(""));
                }
                """);
        }

        private String starterThreadSafeCache() {
        return catalogJavaStarter("""
                static class ThreadSafeCache {
                    private final Map<String, String> values = new HashMap<>();

                    synchronized String get(String key) {
                        return null;
                    }

                    synchronized void put(String key, String value) {
                    }
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    ThreadSafeCache cache = new ThreadSafeCache();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) {
                        String[] parts = line.split(" ");
                        if (parts[0].equals("put")) cache.put(parts[1], parts[2]);
                        else System.out.println(cache.get(parts[1]));
                    }
                }
                """);
        }

        private String starterNotificationQueue() {
        return catalogJavaStarter("""
                static List<String> deliverInOrder(List<String> notifications) {
                    return new ArrayList<>();
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    List<String> notifications = new ArrayList<>();
                    String line;
                    while ((line = input.readLine()) != null && !line.isBlank()) notifications.add(line);
                    System.out.println(String.join("|", deliverInOrder(notifications)));
                }
                """);
        }

        private String starterDistributedRateLimiter() {
        return catalogJavaStarter("""
                static boolean[] allowWithTokenBucket(String key, int capacity, int refillPerSecond, int requests) {
                    return new boolean[requests];
                }
                """, """
                public static void main(String[] args) throws IOException {
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    String[] settings = input.readLine().trim().split(" ");
                    String[] requests = input.readLine().trim().split(" ");
                    boolean[] allowed = allowWithTokenBucket(settings[0], Integer.parseInt(settings[1]), Integer.parseInt(settings[2]), requests.length);
                    for (int i = 0; i < allowed.length; i++) System.out.print((i == 0 ? "" : " ") + allowed[i]);
                }
                """);
        }

    /*
     * Starter templates.
     *
     * Each one is a complete, compilable `public class Main` that already reads
     * stdin in the problem's input format and prints in the problem's output
     * format, leaving only the algorithm for the candidate. Judge0 compiles Java
     * sources as Main.java, so the class must be named Main and must carry a
     * main method — a bare `public class Solution` with no entry point fails to
     * run and every test case reports a compilation error instead of a wrong
     * answer.
     */

    private String starterTwoSum() {
        return """
                import java.util.Scanner;

                public class Main {

                    /**
                     * TODO: return the indices of the two values that sum to target.
                     * Indices must be in ascending order.
                     */
                    static int[] twoSum(int[] nums, int target) {
                        return new int[]{0, 0};
                    }

                    public static void main(String[] args) {
                        Scanner in = new Scanner(System.in);
                        int target = in.nextInt();
                        int n = in.nextInt();
                        int[] nums = new int[n];
                        for (int i = 0; i < n; i++) {
                            nums[i] = in.nextInt();
                        }

                        int[] answer = twoSum(nums, target);
                        System.out.println("[" + answer[0] + ", " + answer[1] + "]");
                    }
                }
                """;
    }

    private String starterLongestValidParentheses() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;

                public class Main {

                    /**
                     * TODO: return the length of the longest valid parentheses substring.
                     */
                    static int longestValidParentheses(String s) {
                        return 0;
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        String line = in.readLine();
                        String s = (line == null) ? "" : line.trim();

                        System.out.println(longestValidParentheses(s));
                    }
                }
                """;
    }

    private String starterLruCache() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;

                public class Main {

                    /**
                     * TODO: implement an LRU cache with O(1) get and put.
                     */
                    static class LRUCache {

                        LRUCache(int capacity) {
                        }

                        int get(int key) {
                            return -1;
                        }

                        void put(int key, int value) {
                        }
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        LRUCache cache = new LRUCache(Integer.parseInt(in.readLine().trim()));
                        StringBuilder out = new StringBuilder();

                        String line;
                        while ((line = in.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }

                            String inside = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
                            String[] parts = inside.split(",");

                            if (line.startsWith("put")) {
                                cache.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
                            } else {
                                out.append(cache.get(Integer.parseInt(parts[0].trim()))).append('\\n');
                            }
                        }

                        System.out.print(out);
                    }
                }
                """;
    }

    private String starterAnagram() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;

                public class Main {

                    /**
                     * TODO: return true when t is an anagram of s.
                     */
                    static boolean isAnagram(String s, String t) {
                        return false;
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        String s = in.readLine();
                        String t = in.readLine();

                        System.out.println(isAnagram(
                                (s == null) ? "" : s.trim(),
                                (t == null) ? "" : t.trim()));
                    }
                }
                """;
    }

    private String starterMergeIntervals() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;
                import java.util.ArrayList;
                import java.util.List;
                import java.util.regex.Matcher;
                import java.util.regex.Pattern;

                public class Main {

                    /**
                     * TODO: merge every pair of overlapping or touching intervals
                     * and return the result sorted by start.
                     */
                    static int[][] merge(int[][] intervals) {
                        return intervals;
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        String line = in.readLine();
                        int[][] intervals = parse((line == null) ? "" : line);

                        int[][] merged = merge(intervals);

                        StringBuilder out = new StringBuilder("[");
                        for (int i = 0; i < merged.length; i++) {
                            if (i > 0) {
                                out.append(',');
                            }
                            out.append('[').append(merged[i][0]).append(',').append(merged[i][1]).append(']');
                        }
                        System.out.println(out.append(']'));
                    }

                    private static int[][] parse(String text) {
                        List<int[]> pairs = new ArrayList<>();
                        Matcher matcher = Pattern.compile("\\\\[\\\\s*(-?\\\\d+)\\\\s*,\\\\s*(-?\\\\d+)\\\\s*]").matcher(text);
                        while (matcher.find()) {
                            pairs.add(new int[]{
                                    Integer.parseInt(matcher.group(1)),
                                    Integer.parseInt(matcher.group(2))
                            });
                        }
                        return pairs.toArray(new int[0][]);
                    }
                }
                """;
    }

    private String starterApiCounter() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;
                import java.util.StringTokenizer;

                public class Main {

                    /**
                     * TODO: return the largest number of timestamps that fit inside
                     * any half-open window [t, t + windowSeconds).
                     */
                    static int maxRequests(int[] timestamps, int windowSeconds) {
                        return 0;
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                        StringTokenizer tokenizer = new StringTokenizer(in.readLine());
                        int[] timestamps = new int[tokenizer.countTokens()];
                        for (int i = 0; i < timestamps.length; i++) {
                            timestamps[i] = Integer.parseInt(tokenizer.nextToken());
                        }

                        int windowSeconds = Integer.parseInt(in.readLine().trim());

                        System.out.println(maxRequests(timestamps, windowSeconds));
                    }
                }
                """;
    }

    private String starterFrequencyAnalyzer() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;

                public class Main {

                    /**
                     * TODO: return the k most frequent characters of s, joined by
                     * commas. Sort by descending frequency, then lexicographically.
                     */
                    static String topKFrequent(String s, int k) {
                        return "";
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        String s = in.readLine();
                        int k = Integer.parseInt(in.readLine().trim());

                        System.out.println(topKFrequent((s == null) ? "" : s, k));
                    }
                }
                """;
    }

    private String starterStreamingStats() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;
                import java.util.Locale;
                import java.util.StringTokenizer;

                public class Main {

                    /**
                     * TODO: return the running average after each value, space
                     * separated, each formatted to exactly 2 decimal places.
                     * Use Locale.ROOT so the separator is always a dot.
                     */
                    static String runningAverages(int[] values) {
                        return String.format(Locale.ROOT, "%.2f", 0.0);
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                        StringTokenizer tokenizer = new StringTokenizer(in.readLine());
                        int[] values = new int[tokenizer.countTokens()];
                        for (int i = 0; i < values.length; i++) {
                            values[i] = Integer.parseInt(tokenizer.nextToken());
                        }

                        System.out.println(runningAverages(values));
                    }
                }
                """;
    }

    private String starterDependencyResolution() {
        return """
                import java.io.BufferedReader;
                import java.io.IOException;
                import java.io.InputStreamReader;
                import java.util.ArrayList;
                import java.util.List;

                public class Main {

                    /**
                     * TODO: return "BUILDABLE" when the dependency graph is acyclic,
                     * otherwise "CYCLE". Each dependency is a {from, to} pair.
                     */
                    static String canBuild(int moduleCount, List<String[]> dependencies) {
                        return "BUILDABLE";
                    }

                    public static void main(String[] args) throws IOException {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        int moduleCount = Integer.parseInt(in.readLine().trim());

                        List<String[]> dependencies = new ArrayList<>();
                        String line;
                        while ((line = in.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            String[] parts = line.split("->");
                            dependencies.add(new String[]{parts[0].trim(), parts[1].trim()});
                        }

                        System.out.println(canBuild(moduleCount, dependencies));
                    }
                }
                """;
    }
}
