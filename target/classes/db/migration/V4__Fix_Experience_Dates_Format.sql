-- Create temporary columns
ALTER TABLE experience ADD COLUMN start_date_temp DATE;
ALTER TABLE experience ADD COLUMN end_date_temp DATE;

-- Update temporary columns with correctly formatted dates
UPDATE experience 
SET start_date_temp = STR_TO_DATE(start_date, '%d-%m-%Y'),
    end_date_temp = CASE 
        WHEN end_date IS NULL THEN NULL
        ELSE STR_TO_DATE(end_date, '%d-%m-%Y')
    END;

-- Drop old columns
ALTER TABLE experience DROP COLUMN start_date;
ALTER TABLE experience DROP COLUMN end_date;

-- Rename temporary columns to original names
ALTER TABLE experience CHANGE start_date_temp start_date DATE;
ALTER TABLE experience CHANGE end_date_temp end_date DATE; 