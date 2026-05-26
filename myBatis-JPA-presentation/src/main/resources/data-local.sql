INSERT INTO members (id, name, is_deleted, created_date, updated_date)
SELECT 1, 'Local Member 1', 0, '2026-05-01 09:00:00', '2026-05-01 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM members WHERE id = 1);

INSERT INTO members (id, name, is_deleted, created_date, updated_date)
SELECT 2, 'Local Member 2', 0, '2026-05-01 11:20:00', '2026-05-01 11:20:00'
WHERE NOT EXISTS (SELECT 1 FROM members WHERE id = 2);

INSERT INTO member_login_history (member_id, access_date)
SELECT 1, '2026-05-01 09:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM member_login_history
    WHERE member_id = 1 AND access_date = '2026-05-01 09:00:00'
);

INSERT INTO member_login_history (member_id, access_date)
SELECT 1, '2026-05-02 18:30:00'
WHERE NOT EXISTS (
    SELECT 1 FROM member_login_history
    WHERE member_id = 1 AND access_date = '2026-05-02 18:30:00'
);

INSERT INTO member_login_history (member_id, access_date)
SELECT 2, '2026-05-01 11:20:00'
WHERE NOT EXISTS (
    SELECT 1 FROM member_login_history
    WHERE member_id = 2 AND access_date = '2026-05-01 11:20:00'
);
