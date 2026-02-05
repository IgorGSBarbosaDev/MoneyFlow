ALTER TABLE alerts ADD COLUMN read_at TIMESTAMP NULL;

UPDATE alerts SET read_at = created_at WHERE read = true AND read_at IS NULL;
