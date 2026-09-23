-- Demo only (profile "demo"): the account the page uses, VITE_ACCOUNT_ID=1.
-- V1__init.sql seeds towns but no accounts; the acceptance tests insert their own.
INSERT INTO accounts (id, name, password_hash, email)
VALUES (1, 'demo', 'not-a-real-hash', 'demo@example.com');
