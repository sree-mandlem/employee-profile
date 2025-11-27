-- Absence request table
CREATE TABLE absence_request (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,

    employee_id     BIGINT NOT NULL,
    approver_id     BIGINT, -- manager who approves/rejects

    from_date       DATE NOT NULL,
    to_date         DATE NOT NULL,
    type            VARCHAR(50) NOT NULL,   -- VACATION, SICK, etc.
    status          VARCHAR(50) NOT NULL,   -- PENDING, APPROVED, REJECTED

    decision_at     TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT fk_absence_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_absence_approver
        FOREIGN KEY (approver_id) REFERENCES employee (id)
);
