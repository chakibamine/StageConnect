-- Create temporary columns
ALTER TABLE experiences 
ADD COLUMN start_date_temp DATE NULL,
ADD COLUMN end_date_temp DATE NULL;

-- Update the temporary columns with correctly formatted dates
UPDATE experiences 
SET start_date_temp = STR_TO_DATE(start_date, '%d-%m-%Y'),
    end_date_temp = CASE 
        WHEN end_date IS NOT NULL AND end_date != '' 
        THEN STR_TO_DATE(end_date, '%d-%m-%Y')
        ELSE NULL 
    END;

-- Drop the old columns
ALTER TABLE experiences 
DROP COLUMN start_date,
DROP COLUMN end_date;

-- Rename the temporary columns
ALTER TABLE experiences 
CHANGE COLUMN start_date_temp start_date DATE NOT NULL,
CHANGE COLUMN end_date_temp end_date DATE NULL; 