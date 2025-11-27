-- User account table (authentication)
CREATE TABLE user_account (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,

    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,

    employee_id     BIGINT
);

CREATE TABLE user_roles (
    user_account_id BIGINT NOT NULL,
    role            VARCHAR(50) NOT NULL,

    CONSTRAINT fk_user_roles_user_account
        FOREIGN KEY (user_account_id)
            REFERENCES user_account (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_user_roles UNIQUE (user_account_id, role)
);


-- Employee table
CREATE TABLE employee (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,

    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,

    user_account_id BIGINT NOT NULL UNIQUE,
    manager_id      BIGINT,

    CONSTRAINT fk_employee_user_account
        FOREIGN KEY (user_account_id) REFERENCES user_account (id),
    CONSTRAINT fk_employee_manager
        FOREIGN KEY (manager_id) REFERENCES employee (id)
);

-- Employee Profile table
CREATE TABLE employee_profile (
    id                  BIGSERIAL PRIMARY KEY,
    created_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,

    employee_id         BIGINT NOT NULL UNIQUE,

    -- Non-sensitive fields
    job_title           VARCHAR(100),
    department          VARCHAR(100),
    skills              VARCHAR(500),
    bio                 VARCHAR(1000),
    avatar_url          VARCHAR(500),

    -- Sensitive fields
    salary              NUMERIC(15, 2),
    performance_notes   VARCHAR(2000),
    home_address        VARCHAR(500),
    personal_phone      VARCHAR(50),

    CONSTRAINT fk_employee_profile_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id)
);