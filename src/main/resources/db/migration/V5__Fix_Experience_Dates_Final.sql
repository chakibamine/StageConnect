-- First, create temporary columns with VARCHAR type
ALTER TABLE experience 
ADD COLUMN start_date_temp VARCHAR(255),
ADD COLUMN end_date_temp VARCHAR(255);

-- Copy existing data to temporary columns
UPDATE experience 
SET start_date_temp = start_date,
    end_date_temp = end_date;

-- Drop original columns
ALTER TABLE experience 
DROP COLUMN start_date,
DROP COLUMN end_date;

-- Add new DATE columns
ALTER TABLE experience 
ADD COLUMN start_date DATE NULL,
ADD COLUMN end_date DATE NULL;

-- Convert and copy data from temp columns to new DATE columns
-- Handle both dd-mm-yyyy and yyyy-MM-dd formats
UPDATE experience 
SET start_date = CASE 
    WHEN start_date_temp LIKE '%-%-%' THEN 
        STR_TO_DATE(start_date_temp, '%d-%m-%Y')
    ELSE 
        STR_TO_DATE(start_date_temp, '%Y-%m-%d')
    END
WHERE start_date_temp IS NOT NULL;

UPDATE experience 
SET end_date = CASE 
    WHEN end_date_temp IS NULL THEN NULL
    WHEN end_date_temp LIKE '%-%-%' THEN 
        STR_TO_DATE(end_date_temp, '%d-%m-%Y')
    ELSE 
        STR_TO_DATE(end_date_temp, '%Y-%m-%d')
    END
WHERE end_date_temp IS NOT NULL;

-- Drop temporary columns
ALTER TABLE experience 
DROP COLUMN start_date_temp,
DROP COLUMN end_date_temp;

-- Make start_date NOT NULL
ALTER TABLE experience 
MODIFY COLUMN start_date DATE NOT NULL; 