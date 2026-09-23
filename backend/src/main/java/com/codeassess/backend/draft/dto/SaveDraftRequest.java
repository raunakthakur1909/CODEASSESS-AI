package com.codeassess.backend.draft.dto;

import com.codeassess.backend.submission.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SaveDraftRequest {

    @NotNull(message = "Language is required")
    private Language language;

    @NotBlank(message = "Source code is required")
    private String sourceCode;

    public SaveDraftRequest() {
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
