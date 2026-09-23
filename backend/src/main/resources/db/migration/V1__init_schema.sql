CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE assessments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    total_marks INTEGER NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP
);

CREATE TABLE problems (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id),
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    points INTEGER NOT NULL,
    input_format TEXT,
    output_format TEXT,
    constraints TEXT,
    starter_code TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE test_cases (
    id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    input TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    hidden BOOLEAN NOT NULL DEFAULT TRUE,
    time_limit_ms INTEGER NOT NULL DEFAULT 2000,
    memory_limit_mb INTEGER NOT NULL DEFAULT 256,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    candidate_id BIGINT NOT NULL REFERENCES users(id),
    source_code TEXT NOT NULL,
    language VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    result VARCHAR(30),
    score INTEGER NOT NULL DEFAULT 0,
    execution_time_ms INTEGER,
    memory_used_kb INTEGER,
    passed_tests INTEGER NOT NULL DEFAULT 0,
    total_tests INTEGER NOT NULL DEFAULT 0,
    compiler_output TEXT,
    runtime_output TEXT,
    execution_token VARCHAR(100),
    submitted_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE TABLE code_reviews (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE REFERENCES submissions(id),
    overall_score INTEGER NOT NULL DEFAULT 0,
    summary TEXT,
    complexity_time VARCHAR(50),
    complexity_space VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    next_attempt_at TIMESTAMP
);

CREATE TABLE code_review_issues (
    review_id BIGINT NOT NULL REFERENCES code_reviews(id),
    severity VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    message TEXT,
    suggestion TEXT
);

CREATE TABLE code_review_strengths (
    review_id BIGINT NOT NULL REFERENCES code_reviews(id),
    strength TEXT NOT NULL
);

CREATE TABLE code_review_optimization_suggestions (
    review_id BIGINT NOT NULL REFERENCES code_reviews(id),
    suggestion TEXT NOT NULL
);

CREATE TABLE code_review_security_issues (
    review_id BIGINT NOT NULL REFERENCES code_reviews(id),
    issue TEXT NOT NULL
);
