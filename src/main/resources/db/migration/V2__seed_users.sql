-- Seed initial User Accounts
INSERT INTO user_account (id, created_at, updated_at, username, password_hash, employee_id)
VALUES
    (1, NOW(), NOW(), 'robins',    '$2a$10$AQRw7hodFrA/rqxzYYfefOkRqnpv9T2a3Q5iatuit7EjkQ8mOrLDi', 1),
    (2, NOW(), NOW(), 'lilia',  '$2a$10$AQRw7hodFrA/rqxzYYfefOkRqnpv9T2a3Q5iatuit7EjkQ8mOrLDi', 2),
    (3, NOW(), NOW(), 'josephk',  '$2a$10$AQRw7hodFrA/rqxzYYfefOkRqnpv9T2a3Q5iatuit7EjkQ8mOrLDi', 3);

-- Seed initial Employee
INSERT INTO employee(id, created_at, updated_at, first_name, last_name, email, user_account_id, manager_id)
VALUES
    (1, NOW(), NOW(), 'Robin', 'Sherbatsky', 'manager@company.com', 1, NULL),
    (2, NOW(), NOW(), 'Lili', 'Aldrin', 'employee1@company.com', 2, 1),
    (3, NOW(), NOW(), 'Joseph', 'Komaro', 'employee2@company.com', 3, 1);

-- Seed initial Employee Profile
INSERT INTO public.employee_profile(id, created_at, updated_at, employee_id, job_title, department, skills, bio, avatar_url, salary, performance_notes, home_address, personal_phone)
VALUES
    (1, NOW(), NOW(), 1, 'Project Manager', 'Peoples Management', 'Manager', NULL, NULL, 100000, NULL, NULL, '0123456781'),
	(2, NOW(), NOW(), 2, 'Software Developer', 'IT', 'Full Stack IC', NULL, NULL, 100000, NULL, NULL, '0123456782'),
	(3, NOW(), NOW(), 3, 'Product Owner', 'Human Resources', 'Public Relationship Expert', NULL, NULL, 100000, NULL, NULL, '0123456783');

-- Seed roles for each user
INSERT INTO user_roles (user_account_id, role) VALUES
    (1, 'MANAGER'),

    (2, 'EMPLOYEE'),

    (3, 'EMPLOYEE');
