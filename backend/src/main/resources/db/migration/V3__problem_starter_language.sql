-- ---------------------------------------------------------------
-- P0.2: Starter code delivery
--
-- problems.starter_code already exists (V1) but was never exposed
-- through the API. To render starter code correctly the editor also
-- needs to know which language that starter code is written in,
-- otherwise the client has to guess (it previously hardcoded PYTHON
-- while all seeded starter code was Java).
--
-- starter_language is nullable: existing rows and problems authored
-- without starter code simply fall back to the platform default.
-- ---------------------------------------------------------------

ALTER TABLE problems
    ADD COLUMN IF NOT EXISTS starter_language VARCHAR(20);
