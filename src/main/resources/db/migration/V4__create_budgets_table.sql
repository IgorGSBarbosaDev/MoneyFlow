CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Validação de mês (1-12)
ALTER TABLE budgets ADD CONSTRAINT chk_budget_month
    CHECK (month >= 1 AND month <= 12);

-- Validação de ano (não permitir anos muito antigos ou muito futuros)
ALTER TABLE budgets ADD CONSTRAINT chk_budget_year
    CHECK (year >= 2000 AND year <= 2100);

-- Validação de valor positivo
ALTER TABLE budgets ADD CONSTRAINT chk_budget_amount_positive
    CHECK (amount > 0);

-- Constraint de unicidade: um usuário não pode ter mais de um orçamento para a mesma categoria no mesmo mês/ano
CREATE UNIQUE INDEX uk_budgets_user_category_month_year
    ON budgets(user_id, category_id, month, year);

-- Índices para otimização
CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_month_year ON budgets(month, year);
CREATE INDEX idx_budgets_user_month_year ON budgets(user_id, month, year);

-- Comentários de documentação
COMMENT ON TABLE budgets IS 'Orçamentos mensais definidos por categoria';
COMMENT ON COLUMN budgets.month IS 'Mês do orçamento (1-12)';
COMMENT ON COLUMN budgets.year IS 'Ano do orçamento';
COMMENT ON COLUMN budgets.amount IS 'Valor limite do orçamento';
