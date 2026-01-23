CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(200) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    notes VARCHAR(500),
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Validação de valores do ENUM TransactionType
ALTER TABLE transactions ADD CONSTRAINT chk_transaction_type
    CHECK (type IN ('INCOME', 'EXPENSE'));

-- Validação de valores do ENUM PaymentMethod
ALTER TABLE transactions ADD CONSTRAINT chk_payment_method
    CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'CASH', 'PIX'));

-- Validação de valor positivo
ALTER TABLE transactions ADD CONSTRAINT chk_transaction_amount_positive
    CHECK (amount > 0);

-- Índices para otimização de consultas
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_deleted ON transactions(deleted);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_user_type_date ON transactions(user_id, type, date);

-- Comentários de documentação
COMMENT ON TABLE transactions IS 'Transações financeiras (receitas e despesas)';
COMMENT ON COLUMN transactions.amount IS 'Valor da transação (sempre positivo)';
COMMENT ON COLUMN transactions.type IS 'Tipo: INCOME (receita) ou EXPENSE (despesa)';
COMMENT ON COLUMN transactions.payment_method IS 'Método de pagamento utilizado';
COMMENT ON COLUMN transactions.deleted IS 'Soft delete - indica se a transação foi excluída';
