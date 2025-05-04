-- First, create temporary columns with VARCHAR type
ALTER TABLE experiences
ADD COLUMN start_date_temp VARCHAR(255),
ADD COLUMN end_date_temp VARCHAR(255);

-- Copy existing data to temporary columns
UPDATE experiences
SET start_date_temp = start_date,
    end_date_temp = end_date;

-- Drop original columns
ALTER TABLE experiences
DROP COLUMN start_date,
DROP COLUMN end_date;

-- Add new DATE columns
ALTER TABLE experiences
ADD COLUMN start_date DATE NULL,
ADD COLUMN end_date DATE NULL;

-- Convert and copy data from temp columns to new DATE columns
UPDATE experiences
SET start_date = STR_TO_DATE(start_date_temp, '%d-%m-%Y')
WHERE start_date_temp IS NOT NULL;

UPDATE experiences
SET end_date = STR_TO_DATE(end_date_temp, '%d-%m-%Y')
WHERE end_date_temp IS NOT NULL;

-- Drop temporary columns
ALTER TABLE experiences
DROP COLUMN start_date_temp,
DROP COLUMN end_date_temp;

-- Finally, make start_date NOT NULL
ALTER TABLE experiences
MODIFY COLUMN start_date DATE NOT NULL; 