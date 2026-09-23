package com.codeassess.backend.submission;

/**
 * Supported programming languages for code submissions.
 * Maps to Judge0 language IDs for execution.
 */
public enum Language {
    
    /**
    * Java (OpenJDK 25)
     */
    JAVA("java", 91),
    
    /**
     * Python 3
     */
    PYTHON("python", 92),
    
    /**
     * C++ (GCC 11)
     */
    CPP("cpp", 76),
    
    /**
     * C (GCC 11)
     */
    C("c", 75);
    
    private final String displayName;
    private final int judge0Id;
    
    Language(String displayName, int judge0Id) {
        this.displayName = displayName;
        this.judge0Id = judge0Id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getJudge0Id() {
        return judge0Id;
    }
    
    /**
     * Get language by Judge0 ID
     */
    public static Language fromJudge0Id(int id) {
        for (Language lang : values()) {
            if (lang.judge0Id == id) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unknown Judge0 language ID: " + id);
    }
}
