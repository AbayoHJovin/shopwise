-- Add open flag to business table
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                    WHERE table_name = 'business' AND column_name = 'open') THEN
        ALTER TABLE business ADD COLUMN open BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- Drop availability_slot table and related constraints
DROP TABLE IF EXISTS availability_slot;
