package com.codeassess.backend.execution;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO from Judge0 API.
 * Contains execution results and status information.
 * 
 * Status IDs:
 * 1 - In Queue
 * 2 - Processing
 * 3 - Accepted
 * 4 - Wrong Answer
 * 5 - Time Limit Exceeded
 * 6 - Compilation Error
 * 7 - Runtime Error (SIGSEGV)
 * 8 - Runtime Error (SIGXFSZ)
 * 9 - Runtime Error (SIGFPE)
 * 10 - Runtime Error (SIGABRT)
 * 11 - Runtime Error (NZEC)
 * 12 - Runtime Error (Other)
 * 13 - Internal Error
 * 14 - Exec Format Error
 */
public class Judge0SubmissionResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("status")
    private Judge0Status status;

    @JsonProperty("stdout")
    private String stdout;

    @JsonProperty("stderr")
    private String stderr;

    @JsonProperty("compile_output")
    private String compileOutput;

    @JsonProperty("message")
    private String message;

    @JsonProperty("time")
    private String time;

    @JsonProperty("memory")
    private Integer memory;

    public Judge0SubmissionResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Judge0Status getStatus() {
        return status;
    }

    public void setStatus(Judge0Status status) {
        this.status = status;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getCompileOutput() {
        return compileOutput;
    }

    public void setCompileOutput(String compileOutput) {
        this.compileOutput = compileOutput;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    /**
     * Nested status object from Judge0 API
     */
    public static class Judge0Status {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("description")
        private String description;

        public Judge0Status() {
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
