package com.student.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.student.entity.Student;

@Service
public class AiService {

    // =========================================================
    // GEMINI CONFIGURATION
    // =========================================================

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${gemini.timeout.seconds:30}")
    private long timeoutSeconds;

    @Value("${gemini.max-retries:2}")
    private int maxRetries;

    // =========================================================
    // MOCK / TEST MODE
    // =========================================================

    /*
     * true  = Gemini API will NOT be called.
     * false = Real Gemini API will be called.
     *
     * Keep this true while testing if Gemini quota is exhausted.
     */
    @Value("${gemini.mock-mode:false}")
    private boolean mockMode;

    private static final int MAX_QUESTION_LENGTH = 2000;

    // =========================================================
    // REST CLIENT
    // =========================================================

    private final RestClient restClient;

    @Autowired
    private StudentService studentService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AiService() {

        this.restClient = RestClient.builder()
                .requestFactory(createRequestFactory())
                .build();
    }

    // =========================================================
    // CREATE HTTP REQUEST FACTORY
    // =========================================================

    private org.springframework.http.client.ClientHttpRequestFactory createRequestFactory() {

        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(10));

        /*
         * Keep a safe default here because @Value fields are injected
         * after the constructor runs.
         */
        factory.setReadTimeout(Duration.ofSeconds(30));

        return factory;
    }

    // =========================================================
    // ASK AI
    // =========================================================

    public String askAi(String question) {

        // ---------------------------------------------------------
        // VALIDATE QUESTION
        // ---------------------------------------------------------

        if (question == null || question.trim().isEmpty()) {

            return "Please enter a question.";
        }

        String cleanedQuestion = question.trim();

        if (cleanedQuestion.length() > MAX_QUESTION_LENGTH) {

            return "Your question is too long. "
                    + "Please enter a question with maximum "
                    + MAX_QUESTION_LENGTH
                    + " characters.";
        }

        // ---------------------------------------------------------
        // MOCK / TEST MODE
        // ---------------------------------------------------------

        /*
         * IMPORTANT:
         *
         * When mockMode = true:
         *
         * - Gemini API is NOT called.
         * - Gemini quota is NOT consumed.
         * - No waiting time.
         * - No retry.
         * - No API key is required.
         *
         * This allows unlimited local testing.
         */

        if (mockMode) {

            System.out.println();
            System.out.println("================================");
            System.out.println("AI MOCK / TEST MODE");
            System.out.println("Gemini API will NOT be called.");
            System.out.println("QUESTION : " + cleanedQuestion);
            System.out.println("================================");

            return getMockResponse(cleanedQuestion);
        }

        // ---------------------------------------------------------
        // VALIDATE API KEY
        // ---------------------------------------------------------

        if (!isApiKeyConfigured()) {

            System.err.println("Gemini API key is missing.");

            return """
                    Gemini AI is not configured.

                    Please configure the GEMINI_API_KEY environment
                    variable and restart the application.
                    """;
        }

        // ---------------------------------------------------------
        // BUILD PROJECT CONTEXT
        // ---------------------------------------------------------

        String projectContext = buildProjectContext();

        // ---------------------------------------------------------
        // BUILD PROMPT
        // ---------------------------------------------------------

        String prompt = """
                You are the AI Assistant for a Student Management System.

                PROJECT TECHNOLOGIES:

                - Java 17
                - Spring Boot
                - Spring Data JPA
                - Hibernate
                - MySQL
                - Thymeleaf
                - HTML
                - CSS
                - JavaScript
                - Spring Security
                - JWT Authentication
                - REST APIs
                - Audit Logs

                You are assisting the administrator of this Student
                Management System.

                IMPORTANT RULES:

                1. Answer questions related to this Student Management
                   System, Java, Spring Boot, MySQL, JPA, Hibernate,
                   Thymeleaf, Spring Security, JWT, REST APIs and
                   programming.

                2. When the question asks about current student data,
                   use ONLY the database information provided below.

                3. Never invent student numbers, names, emails, ages,
                   departments, cities or other student information.

                4. If requested information is not available in the
                   provided project context, clearly say that it is
                   not available.

                5. Give simple, clear and useful answers.

                6. For project-related questions, explain the answer
                   in the context of this Student Management System.

                7. Do not claim that a feature exists unless it is
                   mentioned in the project context.

                8. If the user asks a general Java or Spring Boot
                   question, answer normally and give a simple example
                   when useful.

                9. Never expose passwords, API keys, JWT secrets or
                   other security-sensitive information.

                10. Keep answers reasonably concise unless the user
                    asks for a detailed explanation.

                CURRENT PROJECT / DATABASE INFORMATION:

                %s

                USER QUESTION:

                %s
                """.formatted(
                        projectContext,
                        cleanedQuestion
                );

        // ---------------------------------------------------------
        // CREATE REQUEST BODY
        // ---------------------------------------------------------

        Map<String, Object> requestBody = Map.of(
                "contents",
                new Object[]{
                        Map.of(
                                "parts",
                                new Object[]{
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                }
                        )
                }
        );

        // ---------------------------------------------------------
        // CALL GEMINI
        // ---------------------------------------------------------

        try {

            Map<?, ?> response =
                    callGeminiWithRetry(requestBody);

            return extractResponseText(response);

        } catch (RestClientResponseException e) {

            return handleGeminiHttpError(e);

        } catch (ResourceAccessException e) {

            System.err.println(
                    "Gemini connection error: "
                            + e.getMessage()
            );

            return """
                    Unable to connect to the Gemini AI service.

                    Please check:

                    - Internet connection
                    - Gemini API availability
                    - Firewall or proxy settings

                    Your Student Management System itself is
                    running correctly.
                    """;

        } catch (Exception e) {

            System.err.println(
                    "Unexpected Gemini error: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return """
                    Unable to process your AI request right now.

                    The Student Management System is running,
                    but the Gemini AI service returned an
                    unexpected error.

                    Please try again later.
                    """;
        }
    }

    // =========================================================
    // MOCK AI RESPONSE
    // =========================================================

    private String getMockResponse(String question) {

        String q = question.toLowerCase().trim();

        // ---------------------------------------------------------
        // HELLO
        // ---------------------------------------------------------

        if (q.equals("hi")
                || q.equals("hello")
                || q.contains("hello ai")
                || q.contains("hi ai")) {

            return """
                    Hello! 👋

                    The Student Management System AI Assistant
                    is working correctly.

                    Current mode: MOCK / TEST MODE

                    Gemini API is not being called, so your
                    Gemini quota is not being consumed.
                    """;
        }

        // ---------------------------------------------------------
        // WHAT IS THIS SYSTEM
        // ---------------------------------------------------------

        if (q.contains("what is this system")
                || q.contains("what is student management system")
                || q.contains("about this project")) {

            return """
                    This is a Student Management System developed
                    using Java and Spring Boot.

                    The project uses Spring Data JPA, Hibernate,
                    MySQL, Thymeleaf, Spring Security, JWT,
                    REST APIs and Audit Logs.

                    It provides functionality for managing
                    students, administrators, authentication,
                    profiles and system activities.
                    """;
        }

        // ---------------------------------------------------------
        // TOTAL STUDENTS
        // ---------------------------------------------------------

        if (q.contains("how many students")
                || q.contains("total students")
                || q.contains("student count")
                || q.contains("number of students")) {

            try {

                List<Student> students =
                        studentService.getAllStudentsOnly();

                int count =
                        students != null
                                ? students.size()
                                : 0;

                return "There are currently "
                        + count
                        + " students in the database.";

            } catch (Exception e) {

                System.err.println(
                        "Mock mode database error: "
                                + e.getMessage()
                );

                return """
                        I could not retrieve the current student
                        count from the database.
                        """;
            }
        }

        // ---------------------------------------------------------
        // TECHNOLOGIES
        // ---------------------------------------------------------

        if (q.contains("technology")
                || q.contains("technologies")
                || q.contains("tech stack")
                || q.contains("technology used")) {

            return """
                    The Student Management System uses:

                    • Java
                    • Spring Boot
                    • Spring Data JPA
                    • Hibernate
                    • MySQL
                    • Thymeleaf
                    • HTML
                    • CSS
                    • JavaScript
                    • Spring Security
                    • JWT
                    • REST APIs
                    • Audit Logs
                    """;
        }

        // ---------------------------------------------------------
        // JAVA
        // ---------------------------------------------------------

        if (q.contains("what is java")
                || q.equals("java")
                || q.contains("explain java")) {

            return """
                    Java is an object-oriented programming language
                    commonly used for backend and enterprise
                    application development.

                    This Student Management System uses Java
                    as its primary backend programming language.
                    """;
        }

        // ---------------------------------------------------------
        // SPRING BOOT
        // ---------------------------------------------------------

        if (q.contains("what is spring boot")
                || q.contains("explain spring boot")
                || q.contains("spring boot")) {

            return """
                    Spring Boot is a Java framework used to build
                    web applications and REST APIs quickly.

                    In this Student Management System, Spring Boot
                    is used for controllers, services, repositories,
                    security configuration and REST APIs.
                    """;
        }

        // ---------------------------------------------------------
        // JPA
        // ---------------------------------------------------------

        if (q.contains("what is jpa")
                || q.contains("explain jpa")
                || q.equals("jpa")) {

            return """
                    JPA stands for Java Persistence API.

                    It provides a standard way for Java applications
                    to interact with relational databases.

                    In this project, Spring Data JPA is used for
                    database operations.
                    """;
        }

        // ---------------------------------------------------------
        // HIBERNATE
        // ---------------------------------------------------------

        if (q.contains("what is hibernate")
                || q.contains("explain hibernate")
                || q.equals("hibernate")) {

            return """
                    Hibernate is an ORM framework.

                    It maps Java objects and entities to database
                    tables.

                    In this Student Management System, Hibernate
                    works with Spring Data JPA to communicate with
                    the MySQL database.
                    """;
        }

        // ---------------------------------------------------------
        // MYSQL
        // ---------------------------------------------------------

        if (q.contains("what is mysql")
                || q.contains("explain mysql")
                || q.contains("mysql database")
                || q.equals("mysql")) {

            return """
                    MySQL is a relational database management system.

                    This Student Management System uses MySQL to
                    store student, role, audit log and other
                    application data.
                    """;
        }

        // ---------------------------------------------------------
        // JWT
        // ---------------------------------------------------------

        if (q.contains("what is jwt")
                || q.contains("explain jwt")
                || q.equals("jwt")) {

            return """
                    JWT stands for JSON Web Token.

                    JWT is used for authentication between the
                    client and server.

                    In this Student Management System, JWT is used
                    with Spring Security to authenticate API
                    requests and provide role-based access.
                    """;
        }

        // ---------------------------------------------------------
        // SPRING SECURITY
        // ---------------------------------------------------------

        if (q.contains("spring security")
                || q.contains("what is spring security")
                || q.contains("explain spring security")) {

            return """
                    Spring Security is used to secure the
                    Student Management System.

                    The project uses authentication, authorization,
                    JWT authentication and role-based access.

                    ADMIN and STUDENT users have different
                    permissions.
                    """;
        }

        // ---------------------------------------------------------
        // ROLE
        // ---------------------------------------------------------

        if (q.contains("role")
                || q.contains("admin permission")
                || q.contains("student permission")) {

            return """
                    The Student Management System uses roles
                    for access control.

                    ROLE_ADMIN is used for administrative
                    operations.

                    ROLE_STUDENT is used for student-level
                    operations.

                    Spring Security controls access based on
                    these roles.
                    """;
        }

        // ---------------------------------------------------------
        // REST API
        // ---------------------------------------------------------

        if (q.contains("rest api")
                || q.contains("what is rest")
                || q.contains("api")) {

            return """
                    REST API is a way for applications to
                    communicate with a backend using HTTP.

                    This Student Management System provides
                    REST endpoints for authentication, students,
                    administrators, audit logs and the AI
                    assistant.
                    """;
        }

        // ---------------------------------------------------------
        // AUDIT LOG
        // ---------------------------------------------------------

        if (q.contains("audit log")
                || q.contains("audit logs")
                || q.contains("what is audit")) {

            return """
                    Audit Logs record important activities
                    performed in the Student Management System.

                    Examples include login activity and
                    password changes.

                    The audit_logs table stores information such
                    as action, username, role, description,
                    IP address and timestamp.
                    """;
        }

        // ---------------------------------------------------------
        // THYMELEAF
        // ---------------------------------------------------------

        if (q.contains("thymeleaf")
                || q.contains("what is thymeleaf")) {

            return """
                    Thymeleaf is a server-side Java template
                    engine.

                    In this Student Management System, Thymeleaf
                    is used to create dynamic HTML pages for
                    the web interface.
                    """;
        }

        // ---------------------------------------------------------
        // CONTROLLER
        // ---------------------------------------------------------

        if (q.contains("controller")
                || q.contains("what is controller")) {

            return """
                    A controller handles HTTP requests in a
                    Spring Boot application.

                    In this project, controllers are used to
                    handle web pages and REST API requests.

                    Examples include StudentController and
                    AdminPageController.
                    """;
        }

        // ---------------------------------------------------------
        // SERVICE
        // ---------------------------------------------------------

        if (q.contains("service layer")
                || q.contains("what is service")
                || q.contains("service layer")) {

            return """
                    The service layer contains the application's
                    business logic.

                    In this project, services such as
                    StudentService and AiService contain logic
                    used by controllers.
                    """;
        }

        // ---------------------------------------------------------
        // REPOSITORY
        // ---------------------------------------------------------

        if (q.contains("repository")
                || q.contains("what is repository")) {

            return """
                    A repository is used to communicate with
                    the database.

                    In this project, Spring Data JPA repositories
                    provide database operations without requiring
                    manual SQL for every operation.
                    """;
        }

        // ---------------------------------------------------------
        // AI
        // ---------------------------------------------------------

        if (q.contains("how does ai work")
                || q.contains("how ai works")
                || q.contains("ai assistant")) {

            return """
                    The AI Assistant is exposed through the
                    Student Management System's AI endpoint.

                    In real mode, the application builds a
                    project-aware prompt and sends it to Gemini.

                    In MOCK / TEST MODE, Gemini is not called.
                    The application returns local test responses
                    instead.
                    """;
        }

        // ---------------------------------------------------------
        // DATABASE
        // ---------------------------------------------------------

        if (q.contains("database")
                || q.contains("which database")
                || q.contains("database used")) {

            return """
                    The Student Management System uses MySQL
                    as its database.

                    Spring Data JPA and Hibernate are used to
                    communicate with MySQL.
                    """;
        }

        // ---------------------------------------------------------
        // SECURITY
        // ---------------------------------------------------------

        if (q.contains("password")
                || q.contains("authentication")
                || q.contains("authorization")) {

            return """
                    The Student Management System uses Spring
                    Security for authentication and authorization.

                    Passwords are stored using password hashing,
                    and JWT is used for API authentication.

                    Sensitive information such as passwords and
                    API keys should never be exposed through the
                    AI Assistant.
                    """;
        }

        // ---------------------------------------------------------
        // DEFAULT RESPONSE
        // ---------------------------------------------------------

        return """
                AI Assistant is working correctly in
                MOCK / TEST MODE.

                Your question was:

                "%s"

                Gemini API was NOT called.

                This means you can continue testing the
                application without consuming Gemini API quota.

                Try questions such as:

                • How many students are there?
                • What technologies are used?
                • What is Spring Boot?
                • What is JPA?
                • What is Hibernate?
                • What is JWT?
                • What is Spring Security?
                • What is an audit log?
                • What database does this system use?
                • What is Thymeleaf?
                """.formatted(question);
    }

    // =========================================================
    // API KEY VALIDATION
    // =========================================================

    private boolean isApiKeyConfigured() {

        if (geminiApiKey == null) {

            return false;
        }

        String key = geminiApiKey.trim();

        if (key.isEmpty()) {

            return false;
        }

        if (key.equalsIgnoreCase("${GEMINI_API_KEY}")) {

            return false;
        }

        if (key.equalsIgnoreCase("YOUR_GEMINI_API_KEY")) {

            return false;
        }

        return true;
    }

    // =========================================================
    // CALL GEMINI WITH RETRY
    // =========================================================

    private Map<?, ?> callGeminiWithRetry(
            Map<String, Object> requestBody) {

        int attempt = 0;

        while (true) {

            attempt++;

            try {

                return callGemini(requestBody);

            } catch (RestClientResponseException e) {

                int statusCode =
                        e.getStatusCode().value();

                // -------------------------------------------------
                // NEVER RETRY QUOTA ERRORS
                // -------------------------------------------------

                if (statusCode == 429) {

                    System.err.println(
                            "Gemini quota exceeded. "
                                    + "No retry will be attempted."
                    );

                    throw e;
                }

                // -------------------------------------------------
                // RETRY TEMPORARY SERVER ERRORS
                // -------------------------------------------------

                if (isRetryableStatus(statusCode)
                        && attempt <= maxRetries) {

                    long delay =
                            attempt * 2000L;

                    System.err.println(
                            "Gemini temporary error HTTP "
                                    + statusCode
                                    + ". Retry "
                                    + attempt
                                    + "/"
                                    + maxRetries
                                    + " after "
                                    + delay
                                    + " ms."
                    );

                    sleep(delay);

                    continue;
                }

                throw e;
            }
        }
    }

    // =========================================================
    // CALL GEMINI API
    // =========================================================

    private Map<?, ?> callGemini(
            Map<String, Object> requestBody) {

        /*
         * IMPORTANT:
         *
         * Keep this URL as a normal Java String.
         * Do NOT use Markdown link syntax.
         */

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + geminiModel
                        + ":generateContent";

        System.out.println("Gemini URL: " + url);

        return restClient.post()
                .uri(url + "?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
    }

    // =========================================================
    // HANDLE GEMINI HTTP ERRORS
    // =========================================================

    private String handleGeminiHttpError(
            RestClientResponseException e) {

        int statusCode =
                e.getStatusCode().value();

        String responseBody =
                e.getResponseBodyAsString();

        System.err.println(
                "Gemini API HTTP Error: "
                        + statusCode
        );

        System.err.println(
                "Gemini API Response: "
                        + responseBody
        );

        // ---------------------------------------------------------
        // 429 - QUOTA EXCEEDED
        // ---------------------------------------------------------

        if (statusCode == 429) {

            String apiMessage =
                    extractGeminiErrorMessage(
                            responseBody
                    );

            System.err.println(
                    "Gemini quota exhausted."
            );

            return """
                    AI service quota has been exceeded.

                    Gemini API returned HTTP 429.

                    %s

                    Please check your Gemini API usage and
                    billing settings.

                    This is a Gemini API quota limitation,
                    not an error in your Student Management System.

                    You can enable MOCK MODE to continue testing
                    without calling Gemini.
                    """.formatted(apiMessage);
        }

        // ---------------------------------------------------------
        // 401
        // ---------------------------------------------------------

        if (statusCode == 401) {

            return """
                    Gemini API authentication failed.

                    Please check your Gemini API key.

                    Make sure GEMINI_API_KEY contains a valid
                    Gemini API key and restart the application.
                    """;
        }

        // ---------------------------------------------------------
        // 403
        // ---------------------------------------------------------

        if (statusCode == 403) {

            return """
                    Gemini API access was denied.

                    Please check:

                    - Gemini API key
                    - Google AI Studio project
                    - API permissions
                    - Billing configuration
                    - API restrictions
                    """;
        }

        // ---------------------------------------------------------
        // 400
        // ---------------------------------------------------------

        if (statusCode == 400) {

            String apiMessage =
                    extractGeminiErrorMessage(
                            responseBody
                    );

            return """
                    Gemini rejected the request.

                    Reason:

                    %s

                    Please check the Gemini model name
                    and request format.
                    """.formatted(apiMessage);
        }

        // ---------------------------------------------------------
        // 404
        // ---------------------------------------------------------

        if (statusCode == 404) {

            return """
                    Gemini model was not found.

                    Configured model:

                    %s

                    Please check gemini.model in
                    application.properties.
                    """.formatted(geminiModel);
        }

        // ---------------------------------------------------------
        // 408
        // ---------------------------------------------------------

        if (statusCode == 408) {

            return """
                    Gemini API request timed out.

                    Please try again after a few seconds.
                    """;
        }

        // ---------------------------------------------------------
        // SERVER ERRORS
        // ---------------------------------------------------------

        if (statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504) {

            return """
                    Gemini AI service is temporarily unavailable.

                    Please try again after a few seconds.

                    Your Student Management System is running
                    correctly.
                    """;
        }

        // ---------------------------------------------------------
        // OTHER ERRORS
        // ---------------------------------------------------------

        String apiMessage =
                extractGeminiErrorMessage(
                        responseBody
                );

        return """
                Gemini API returned HTTP %s.

                Message:

                %s
                """.formatted(
                        statusCode,
                        apiMessage
                );
    }

    // =========================================================
    // EXTRACT GEMINI ERROR MESSAGE
    // =========================================================

    private String extractGeminiErrorMessage(
            String responseBody) {

        if (responseBody == null
                || responseBody.trim().isEmpty()) {

            return "No additional error information was provided.";
        }

        try {

            int messageIndex =
                    responseBody.indexOf("\"message\"");

            if (messageIndex >= 0) {

                int colonIndex =
                        responseBody.indexOf(
                                ":",
                                messageIndex
                        );

                if (colonIndex >= 0) {

                    int start =
                            responseBody.indexOf(
                                    "\"",
                                    colonIndex + 1
                            );

                    if (start >= 0) {

                        int end =
                                findClosingQuote(
                                        responseBody,
                                        start + 1
                                );

                        if (end > start) {

                            return responseBody
                                    .substring(
                                            start + 1,
                                            end
                                    )
                                    .replace("\\n", " ")
                                    .replace("\\\"", "\"")
                                    .trim();
                        }
                    }
                }
            }

        } catch (Exception ignored) {

            // Ignore parsing errors.
        }

        if (responseBody.length() > 500) {

            return responseBody.substring(0, 500)
                    + "...";
        }

        return responseBody;
    }

    // =========================================================
    // FIND JSON CLOSING QUOTE
    // =========================================================

    private int findClosingQuote(
            String text,
            int start) {

        boolean escaped = false;

        for (int i = start; i < text.length(); i++) {

            char c = text.charAt(i);

            if (c == '"' && !escaped) {

                return i;
            }

            if (c == '\\' && !escaped) {

                escaped = true;

            } else {

                escaped = false;
            }
        }

        return -1;
    }

    // =========================================================
    // EXTRACT GEMINI RESPONSE TEXT
    // =========================================================

    private String extractResponseText(
            Map<?, ?> response) {

        if (response == null) {

            return "Sorry, no response was received from Gemini.";
        }

        Object candidatesObject =
                response.get("candidates");

        if (!(candidatesObject instanceof List<?> candidates)
                || candidates.isEmpty()) {

            Object errorObject =
                    response.get("error");

            if (errorObject != null) {

                return "Gemini API returned an error.";
            }

            return "Sorry, Gemini did not return an answer.";
        }

        // ---------------------------------------------------------
        // SEARCH ALL CANDIDATES
        // ---------------------------------------------------------

        for (Object candidateObject : candidates) {

            if (!(candidateObject instanceof Map<?, ?> candidate)) {

                continue;
            }

            // -----------------------------------------------------
            // FINISH REASON
            // -----------------------------------------------------

            Object finishReason =
                    candidate.get("finishReason");

            if (finishReason != null) {

                String reason =
                        String.valueOf(finishReason);

                if ("SAFETY".equalsIgnoreCase(reason)) {

                    return """
                            Gemini could not provide an answer
                            because the request was blocked by
                            its safety filters.

                            Please rephrase your question.
                            """;
                }
            }

            // -----------------------------------------------------
            // CONTENT
            // -----------------------------------------------------

            Object contentObject =
                    candidate.get("content");

            if (!(contentObject instanceof Map<?, ?> content)) {

                continue;
            }

            // -----------------------------------------------------
            // PARTS
            // -----------------------------------------------------

            Object partsObject =
                    content.get("parts");

            if (!(partsObject instanceof List<?> parts)) {

                continue;
            }

            // -----------------------------------------------------
            // FIND TEXT
            // -----------------------------------------------------

            for (Object partObject : parts) {

                if (partObject instanceof Map<?, ?> part) {

                    Object text =
                            part.get("text");

                    if (text != null) {

                        String answer =
                                String.valueOf(text).trim();

                        if (!answer.isEmpty()) {

                            return answer;
                        }
                    }
                }
            }
        }

        return "Sorry, Gemini returned an empty answer.";
    }

    // =========================================================
    // BUILD PROJECT / DATABASE CONTEXT
    // =========================================================

    private String buildProjectContext() {

        StringBuilder context =
                new StringBuilder();

        // ---------------------------------------------------------
        // PROJECT INFORMATION
        // ---------------------------------------------------------

        context.append(
                "Project name: Student Management System\n"
        );

        context.append(
                "Backend: Java 17 and Spring Boot\n"
        );

        context.append(
                "Database: MySQL\n"
        );

        context.append(
                "ORM: Spring Data JPA / Hibernate\n"
        );

        context.append(
                "Frontend: Thymeleaf, HTML, CSS and JavaScript\n"
        );

        context.append(
                "Authentication: Spring Security and JWT\n"
        );

        context.append(
                "API architecture: REST API\n"
        );

        context.append(
                "Audit system: Audit Logs\n"
        );

        context.append(
                "AI endpoint: POST /api/ai/ask\n"
        );

        // ---------------------------------------------------------
        // DATABASE INFORMATION
        // ---------------------------------------------------------

        try {

            List<Student> students =
                    studentService.getAllStudentsOnly();

            int totalStudents =
                    students != null
                            ? students.size()
                            : 0;

            context.append(
                    "Total students currently registered "
                            + "in database: "
                            + totalStudents
                            + "\n"
            );

        } catch (Exception e) {

            System.err.println(
                    "Unable to load student database context: "
                            + e.getMessage()
            );

            context.append(
                    "Current student database information "
                            + "is temporarily unavailable.\n"
            );
        }

        context.append(
                "\nThe AI assistant should answer questions "
                        + "specifically about this Student Management "
                        + "System and its technologies."
        );

        return context.toString();
    }

    // =========================================================
    // CHECK RETRYABLE HTTP STATUS
    // =========================================================

    private boolean isRetryableStatus(
            int statusCode) {

        return statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504;
    }

    // =========================================================
    // SLEEP BEFORE RETRY
    // =========================================================

    private void sleep(long milliseconds) {

        try {

            Thread.sleep(milliseconds);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Gemini retry interrupted.",
                    e
            );
        }
    }
}