package com.codeassess.backend.problem.dto;

import com.codeassess.backend.problem.Difficulty;
import com.codeassess.backend.submission.Language;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateProblemRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    @Max(value = 1000, message = "Points cannot exceed 1000")
    private Integer points;

    private String inputFormat;

    private String outputFormat;

    private String constraints;

    /**
     * Boilerplate the candidate's editor is pre-filled with.
     */
    private String starterCode;

    /**
     * Language the starter code is written in. Drives the editor's
     * initial language selection.
     */
    private Language starterLanguage;

    public UpdateProblemRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }

    public Language getStarterLanguage() {
        return starterLanguage;
    }

    public void setStarterLanguage(Language starterLanguage) {
        this.starterLanguage = starterLanguage;
    }
}
