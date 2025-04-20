-- Option 1: Make username column nullable (preferred if you have existing data)
ALTER TABLE users MODIFY username VARCHAR(255) NULL;

-- Option 2: Set a default value for username based on email (for new records)
-- This works if you have a trigger or application logic to handle it
-- ALTER TABLE users ALTER COLUMN username SET DEFAULT '';

-- Option 3: Drop username column completely (if you're sure you don't need it)
-- Be careful with this option if you have existing data
-- ALTER TABLE users DROP COLUMN username; 