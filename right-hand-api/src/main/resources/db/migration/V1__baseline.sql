CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS app_schema_baseline (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    note text NOT NULL
);

INSERT INTO app_schema_baseline (note)
SELECT 'Right Hand Sprint 1 baseline'
WHERE NOT EXISTS (SELECT 1 FROM app_schema_baseline);
