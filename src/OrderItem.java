/**
 * Classe OrderItem (Item de Pedido).
 *
 * RELACIONAMENTO: Order -> OrderItem é uma COMPOSIÇÃO (relação FORTE).
 * O OrderItem SÓ EXISTE dentro de um Order (Pedido). Ele é "parte" do
 * pedido, sem identidade própria fora dele. Se o pedido for deletado,
 * todos os seus itens são deletados junto (não faz sentido um item
 * de pedido "sobrar sozinho" no sistema).
 *
 * Já a relação OrderItem -> Product é uma AGREGAÇÃO (relação FRACA):
 * o item de pedido não "contém" o produto, ele apenas REFERENCIA o
 * produto através do seu id (produtoId). O produto continua existindo
 * no catálogo independentemente do item de pedido.
 *
 * Por ser "parte" da composição, o OrderItem é IMUTÁVEL depois de
 * criado: não existem setters. Uma vez que o pedido foi montado, o
 * item não deveria mudar sozinho (qualquer alteração deveria passar
 * pela classe Order, que é quem controla o ciclo de vida dos itens).
 */
public class OrderItem {

    // Atributos privados, todos finais (imutáveis após o construtor)
    private final int id;            // identificador único DENTRO do pedido
    private final int produtoId;     // referência ao Product (AGREGAÇÃO, não é o objeto inteiro)
    private final int quantidade;
    private final double precoUnitario; // cópia do preço do produto NO MOMENTO do pedido

    /**
     * Construtor: recebe id, produtoId, quantidade e precoUnitario.
     *
     * IMPORTANTE: o precoUnitario é uma CÓPIA do preço do produto no
     * instante em que o item foi criado. Isso "congela" o valor pago,
     * para que alterações futuras no preço do Product (Product.setPreco)
     * não afetem pedidos já feitos. Essa é a demonstração de
     * independência pedida no Main.
     */
    public OrderItem(int id, int produtoId, int quantidade, double precoUnitario) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    // ---------- Apenas Getters (sem setters: item é imutável) ----------

    public int getId() {
        return id;
    }

    public int getProdutoId() {
        return produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    /**
     * Calcula o subtotal deste item: preço unitário (congelado no
     * momento da compra) multiplicado pela quantidade.
     */
    public double calcularSubtotal() {
        return precoUnitario * quantidade;
    }

    @Override
    public String toString() {
        return "OrderItem{id=" + id + ", produtoId=" + produtoId
                + ", quantidade=" + quantidade + ", precoUnitario=" + precoUnitario
                + ", subtotal=" + calcularSubtotal() + "}";
    }
}
