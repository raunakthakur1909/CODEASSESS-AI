-- V4__ai_mentor_upgrade.sql

-- Add line tracking and title to issues
ALTER TABLE code_review_issues 
ADD COLUMN IF NOT EXISTS line_start INTEGER,
ADD COLUMN IF NOT EXISTS line_end INTEGER,
ADD COLUMN IF NOT EXISTS title TEXT;

-- Add new fields to code_reviews
ALTER TABLE code_reviews
ADD COLUMN IF NOT EXISTS corrected_code TEXT,
ADD COLUMN IF NOT EXISTS explanation_of_correction TEXT,
ADD COLUMN IF NOT EXISTS opt_current_approach TEXT,
ADD COLUMN IF NOT EXISTS opt_optimized_approach TEXT,
ADD COLUMN IF NOT EXISTS opt_current_time VARCHAR(50),
ADD COLUMN IF NOT EXISTS opt_current_space VARCHAR(50),
ADD COLUMN IF NOT EXISTS opt_optimized_time VARCHAR(50),
ADD COLUMN IF NOT EXISTS opt_optimized_space VARCHAR(50),
ADD COLUMN IF NOT EXISTS opt_explanation TEXT,
ADD COLUMN IF NOT EXISTS opt_code TEXT;

-- Create edge cases table
CREATE TABLE IF NOT EXISTS code_review_edge_cases (
    review_id BIGINT NOT NULL REFERENCES code_reviews(id),
    description TEXT,
    input TEXT,
    expected_behavior TEXT,
    reason TEXT
);

-- Create failure diagnoses table
CREATE TABLE IF NOT EXISTS code_review_failure_diagnoses (
    review_id BIGINT NOT NULL REFERENCES code_reviews(id),
    test_case_id VARCHAR(100),
    failure_reason TEXT,
    likely_fault TEXT,
    explanation TEXT,
    suggested_fix TEXT
);
