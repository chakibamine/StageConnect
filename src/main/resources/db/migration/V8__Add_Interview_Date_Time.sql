-- Add interview date and time columns to applications table
ALTER TABLE applications ADD COLUMN interview_date VARCHAR(255);
ALTER TABLE applications ADD COLUMN interview_time VARCHAR(255); 