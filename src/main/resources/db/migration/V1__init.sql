CREATE TABLE client (
    id       INT          PRIMARY KEY,
    nome     VARCHAR(120) NOT NULL,
    email    VARCHAR(120) NOT NULL,
    telefone VARCHAR(30)
);

CREATE TABLE product (
    id      INT           PRIMARY KEY,
    nome    VARCHAR(120)  NOT NULL,
    preco   DECIMAL(10,2) NOT NULL,
    estoque INT           NOT NULL DEFAULT 0
);

CREATE TABLE orders (
    id         INT         PRIMARY KEY,
    cliente_id INT         NOT NULL,
    data       DATE        NOT NULL,
    status     VARCHAR(20) NOT NULL,
    finalizado BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_order_client
        FOREIGN KEY (cliente_id) REFERENCES client(id)
        ON DELETE RESTRICT
);

CREATE TABLE order_item (
    id             INT           NOT NULL,
    order_id       INT           NOT NULL,
    produto_id     INT           NOT NULL,
    quantidade     INT           NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, id),
    CONSTRAINT fk_item_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_product
        FOREIGN KEY (produto_id) REFERENCES product(id)
        ON DELETE RESTRICT
);
