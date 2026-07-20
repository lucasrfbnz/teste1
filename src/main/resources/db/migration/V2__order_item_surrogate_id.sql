-- Troca a chave primária composta (order_id, id) por um id AUTO_INCREMENT simples.
-- JPA exigiria @IdClass ou @EmbeddedId para chaves compostas — complexidade desnecessária.
ALTER TABLE order_item ADD INDEX idx_item_order (order_id);
ALTER TABLE order_item DROP PRIMARY KEY;
ALTER TABLE order_item DROP COLUMN id;
ALTER TABLE order_item ADD COLUMN id INT NOT NULL AUTO_INCREMENT PRIMARY KEY;
