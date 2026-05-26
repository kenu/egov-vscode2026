CREATE TABLE IF NOT EXISTS members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS member_login_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    member_id INTEGER NOT NULL,
    access_date TEXT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(id)
);
