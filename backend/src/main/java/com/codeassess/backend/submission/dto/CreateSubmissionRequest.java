package com.codeassess.backend.submission.dto;

import com.codeassess.backend.submission.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request to create a new code submission.
 * Only accepts language and source code - all other fields controlled by backend.
 */
public class CreateSubmissionRequest {

    @NotNull(message = "Language is required")
    private Language language;

    @NotBlank(message = "Source code is required")
    @Size(max = 100000, message = "Source code cannot exceed 100000 characters")
    private String sourceCode;

    public CreateSubmissionRequest() {
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
