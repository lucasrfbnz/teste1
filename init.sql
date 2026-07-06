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

-- "order" é palavra reservada; use "orders"
CREATE TABLE orders (
    id          INT          PRIMARY KEY,
    cliente_id  INT          NOT NULL,
    data        DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    finalizado  BOOLEAN      NOT NULL DEFAULT FALSE,

    -- AGREGAÇÃO (Order -> Client): o cliente existe sem o pedido.
    -- Por isso RESTRICT: o banco IMPEDE apagar um cliente que ainda tem pedidos.
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

    -- COMPOSIÇÃO (Order -> OrderItem): o item só existe dentro do pedido.
    -- Por isso CASCADE: apagar o pedido APAGA JUNTO todos os seus itens.
    CONSTRAINT fk_item_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE,

    -- AGREGAÇÃO (OrderItem -> Product): o produto existe sem o item.
    -- RESTRICT: não deixa apagar um produto que está em algum pedido.
    CONSTRAINT fk_item_product
        FOREIGN KEY (produto_id) REFERENCES product(id)
        ON DELETE RESTRICT
);
