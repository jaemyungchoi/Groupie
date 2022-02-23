-- Use this URL to connect to the database:
-- jdbc:sqlite:database.sqlite3?cipher=chacha20&key=6oDoFUeJ7iWYcTmp4FJSbM2GMMBGDGWF
-- You might have to use an absolute path:
-- e.g. jdbc:sqlite:C:/path/to/database.sqlite3 ... or jdbc:sqlite:/path/to/database.sqlite3 ...

-- Create tables
CREATE TABLE IF NOT EXISTS users (
	id INTEGER PRIMARY KEY,
	name TEXT NOT NULL UNIQUE,
	password_hash TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS proposals (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    owner_uid INTEGER NOT NULL,
    finalized_event_id INTEGER,
    created_at TEXT DEFAULT (datetime('now')),

    FOREIGN KEY (owner_uid) REFERENCES users(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    FOREIGN KEY (finalized_event_id) REFERENCES proposal_events(id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS proposal_users (
    id INTEGER PRIMARY KEY,
    proposal_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL, -- no need for username, JOIN with users table for that
                              -- also no need for is owner, since is owner is saved in proposals table
    hide_proposal INTEGER NOT NULL DEFAULT 0,
    accept_proposal INTEGER,

    FOREIGN KEY (proposal_id) REFERENCES proposals(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    UNIQUE(proposal_id, user_id)
    ON CONFLICT ROLLBACK
  );

CREATE TABLE IF NOT EXISTS proposal_events (
    id INTEGER PRIMARY KEY,
    proposal_id integer NOT NULL,
    tm_event_key TEXT NOT NULL,

    FOREIGN KEY (proposal_id) REFERENCES proposals(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS block_list (
    id INTEGER PRIMARY KEY,
    user_id integer NOT NULL,
    blocked_user_id integer NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    FOREIGN KEY (blocked_user_id) REFERENCES users(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    UNIQUE(user_id, blocked_user_id)
    ON CONFLICT ROLLBACK
);

CREATE TABLE IF NOT EXISTS event_votes (
    id INTEGER PRIMARY KEY,
    event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    can_attend INTEGER,
    rating INTEGER,
    is_draft INTEGER NOT NULL,

    FOREIGN KEY (event_id) REFERENCES proposal_events(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    UNIQUE(event_id, user_id)
    ON CONFLICT REPLACE
);

CREATE TABLE IF NOT EXISTS proposal_drafts (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    owner_uid INTEGER NOT NULL,
    data TEXT NOT NULL,

    FOREIGN KEY (owner_uid) REFERENCES users(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);