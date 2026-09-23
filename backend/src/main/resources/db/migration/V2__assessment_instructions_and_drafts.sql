ALTER TABLE assessments ADD COLUMN IF NOT EXISTS instructions TEXT;

CREATE TABLE IF NOT EXISTS drafts (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL REFERENCES users(id),
    assessment_id BIGINT NOT NULL REFERENCES assessments(id),
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    language VARCHAR(20) NOT NULL,
    source_code TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (candidate_id, assessment_id, problem_id)
);
