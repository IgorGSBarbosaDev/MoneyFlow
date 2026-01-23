ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(60);

CREATE UNIQUE INDEX uk_categories_user_name ON categories(user_id, LOWER(name));

ALTER TABLE alerts ADD COLUMN alert_type VARCHAR(30);

ALTER TABLE alerts ADD CONSTRAINT chk_alert_type
    CHECK (alert_type IN ('BUDGET_WARNING', 'BUDGET_EXCEEDED', 'BUDGET_CREATED', 'MONTHLY_SUMMARY'));

CREATE INDEX idx_alerts_type ON alerts(alert_type);

CREATE INDEX idx_transactions_active_user_date
    ON transactions(user_id, date)
    WHERE deleted = false;

CREATE INDEX idx_transactions_active_category
    ON transactions(category_id, date)
    WHERE deleted = false;

COMMENT ON COLUMN alerts.alert_type IS 'Tipo do alerta: BUDGET_WARNING, BUDGET_EXCEEDED, BUDGET_CREATED, MONTHLY_SUMMARY';
COMMENT ON INDEX uk_categories_user_name IS 'Garante que cada usuário tenha nomes de categoria únicos (case-insensitive)';
COMMENT ON INDEX idx_transactions_active_user_date IS 'Índice parcial para otimizar queries de transações ativas por usuário e data';
