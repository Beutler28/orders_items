-- Tabela de pedidos (raiz de agregação)
CREATE TABLE orders (
    id           UUID           PRIMARY KEY,
    customer_id  UUID           NOT NULL,
    status       VARCHAR(30)    NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    version      BIGINT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL
);

-- Itens de cada pedido
CREATE TABLE order_items (
    id         UUID           PRIMARY KEY,
    order_id   UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id VARCHAR(255)   NOT NULL,
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL
);

-- Índices para consultas frequentes
CREATE INDEX idx_orders_customer_id   ON orders (customer_id);
CREATE INDEX idx_orders_status        ON orders (status);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
