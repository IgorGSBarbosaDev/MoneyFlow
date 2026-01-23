CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    message VARCHAR(200) NOT NULL,
    level VARCHAR(20) NOT NULL,
    budget_amount NUMERIC(15, 2) NOT NULL,
    current_amount NUMERIC(15, 2) NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    category_id BIGINT,
    budget_id BIGINT,
    user_id BIGINT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_alert_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_alert_budget FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE SET NULL,
    CONSTRAINT fk_alert_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Validação de valores do ENUM AlertLevel
ALTER TABLE alerts ADD CONSTRAINT chk_alert_level
    CHECK (level IN ('INFO', 'WARNING', 'CRITICAL'));

-- Validação de mês (1-12)
ALTER TABLE alerts ADD CONSTRAINT chk_alert_month
    CHECK (month >= 1 AND month <= 12);

-- Validação de ano
ALTER TABLE alerts ADD CONSTRAINT chk_alert_year
    CHECK (year >= 2000 AND year <= 2100);

-- Validação de valores positivos
ALTER TABLE alerts ADD CONSTRAINT chk_alert_amounts_positive
    CHECK (budget_amount >= 0 AND current_amount >= 0);

-- Índices para otimização
CREATE INDEX idx_alerts_user_id ON alerts(user_id);
CREATE INDEX idx_alerts_category_id ON alerts(category_id);
CREATE INDEX idx_alerts_budget_id ON alerts(budget_id);
CREATE INDEX idx_alerts_read ON alerts(read);
CREATE INDEX idx_alerts_user_read ON alerts(user_id, read);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);
CREATE INDEX idx_alerts_level ON alerts(level);

-- Comentários de documentação
COMMENT ON TABLE alerts IS 'Alertas de orçamento para notificação do usuário';
COMMENT ON COLUMN alerts.level IS 'Nível de severidade: INFO, WARNING, CRITICAL';
COMMENT ON COLUMN alerts.budget_amount IS 'Valor do orçamento definido';
COMMENT ON COLUMN alerts.current_amount IS 'Valor atual gasto';
COMMENT ON COLUMN alerts.read IS 'Indica se o alerta já foi lido pelo usuário';
