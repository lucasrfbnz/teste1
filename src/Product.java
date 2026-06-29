/**
 * Classe Product (Produto).
 *
 * RELACIONAMENTO: OrderItem -> Product é uma AGREGAÇÃO (relação FRACA).
 * O Produto existe INDEPENDENTEMENTE do item de pedido. Se um item de
 * pedido (OrderItem) for deletado, o Produto continua existindo no
 * catálogo da loja (ele pode estar em outros pedidos, ou em nenhum).
 *
 * Por isso, dentro da classe OrderItem, NÃO guardamos um objeto
 * Product inteiro. Guardamos apenas o "produtoId" (int), que é só
 * uma REFERÊNCIA para o produto. Isso deixa explícito que o item de
 * pedido não é "dono" do produto.
 *
 * Além disso, o OrderItem guarda uma CÓPIA do preço (precoUnitario)
 * no momento da compra. Isso é importante: se o preço do Product for
 * alterado depois, o pedido antigo NÃO deve ser afetado, pois ele
 * já fechou aquele preço no passado (ver demonstração no Main).
 */
public class Product {

    // Atributos privados (encapsulamento)
    private int id;
    private String nome;
    private double preco;
    private int estoque;

    /**
     * Construtor: recebe id, nome, preço. O estoque sempre inicia em 0
     * (ele só aumenta quando alguém "reabastece" o produto através do
     * método reporEstoque).
     */
    public Product(int id, String nome, double preco) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = 0;
    }

    // ---------- Getters e Setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    /**
     * Aumenta o estoque do produto (ex: chegou mercadoria nova).
     * Quem decide QUANDO chamar esse método é um sistema externo de
     * estoque, não a classe Order (o pedido não controla estoque,
     * conforme deixado explícito no enunciado).
     */
    public void reporEstoque(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade para reposição não pode ser negativa.");
        }
        this.estoque += quantidade;
    }

    /**
     * Diminui o estoque do produto (ex: produto foi vendido/retirado).
     * Lança exceção se não houver estoque suficiente, para impedir
     * estoque negativo, o que não faria sentido no mundo real.
     */
    public void baixarEstoque(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade para baixa não pode ser negativa.");
        }
        if (quantidade > this.estoque) {
            throw new IllegalStateException(
                "Estoque insuficiente para o produto '" + nome + "'. "
                + "Estoque atual: " + this.estoque + ", solicitado: " + quantidade);
        }
        this.estoque -= quantidade;
    }

    @Override
    public String toString() {
        return "Produto{id=" + id + ", nome='" + nome + "', preco=" + preco
                + ", estoque=" + estoque + "}";
    }
}
