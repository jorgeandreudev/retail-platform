-- V1: initial schema for products-service

CREATE TABLE products (
    id           UUID PRIMARY KEY,
    sku          VARCHAR(100)  NOT NULL,
    name         VARCHAR(200)  NOT NULL,
    price        NUMERIC(19,2) NOT NULL CHECK (price >= 0),
    stock        INTEGER       NOT NULL CHECK (stock >= 0),
    category     VARCHAR(100)  NOT NULL,
    description  VARCHAR(100)  NOT NULL,
    text         VARCHAR(500)  NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL,
    updated_at   TIMESTAMPTZ   NOT NULL,
    deleted_at   TIMESTAMPTZ   NULL,
    version      BIGINT        NOT NULL DEFAULT 0
);

ALTER TABLE products
    ADD CONSTRAINT uk_products_sku UNIQUE (sku);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_deleted_at ON products (deleted_at);

