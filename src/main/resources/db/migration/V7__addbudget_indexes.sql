CREATE UNIQUE INDEX idx_budget_unique_category_period
    ON budgets(user_id, category_id, month, year);


CREATE INDEX idx_budget_user_period
    ON budgets(user_id, month, year);

CREATE INDEX idx_budget_category
    ON budgets(category_id);