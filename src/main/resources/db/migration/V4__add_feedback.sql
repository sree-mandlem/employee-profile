-- Feedback table
CREATE TABLE feedback (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,

    employee_id     BIGINT NOT NULL, -- profile owner
    author_id       BIGINT NOT NULL, -- who wrote it
    text            TEXT NOT NULL,
    visibility      VARCHAR(50) NOT NULL, -- EMPLOYEE_AND_MANAGER, MANAGER_ONLY

    CONSTRAINT fk_feedback_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_feedback_author
        FOREIGN KEY (author_id) REFERENCES employee (id)
);