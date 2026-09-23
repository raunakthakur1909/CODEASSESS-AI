package com.codeassess.backend.run.dto;

import com.codeassess.backend.submission.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RunRequest {

    @NotNull(message = "Language is required")
    private Language language;

    @NotBlank(message = "Source code is required")
    private String sourceCode;

    public RunRequest() {
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
