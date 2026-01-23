CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(200),
    type VARCHAR(50) NOT NULL,
    color VARCHAR(20) NOT NULL,
    icon VARCHAR(200) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Validação de valores do ENUM CategoryType
ALTER TABLE categories ADD CONSTRAINT chk_category_type
    CHECK (type IN ('INCOME', 'EXPENSE'));

-- Índices para otimização
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);
CREATE INDEX idx_categories_user_type ON categories(user_id, type);

-- Comentários de documentação
COMMENT ON TABLE categories IS 'Categorias de transações (Alimentação, Transporte, etc)';
COMMENT ON COLUMN categories.type IS 'Tipo da categoria: INCOME (receita) ou EXPENSE (despesa)';
COMMENT ON COLUMN categories.color IS 'Cor hexadecimal para UI (#FF5733)';
COMMENT ON COLUMN categories.icon IS 'Nome do ícone ou URL';
