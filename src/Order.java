import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe Order (Pedido).
 *
 * Esta é a classe central do exercício, pois ela participa dos DOIS
 * tipos de relacionamento ao mesmo tempo:
 *
 * 1) Order -> Client  : AGREGAÇÃO (relação FRACA)
 *    O pedido apenas REFERENCIA o cliente através de "clienteId".
 *    O cliente existe independentemente do pedido (se o pedido for
 *    deletado, o cliente continua existindo).
 *
 * 2) Order -> OrderItem : COMPOSIÇÃO (relação FORTE)
 *    O pedido CRIA e CONTROLA o ciclo de vida dos seus itens através
 *    da lista "itens" (List<OrderItem>). Os itens são criados DENTRO
 *    do construtor/métodos do Order, e se o pedido for deletado, os
 *    itens são deletados junto (não existem fora do pedido).
 *
 * Note que NÃO existe nenhum atributo "List<Product> produtos" aqui.
 * O Order não conhece os objetos Product, apenas ids de produto
 * (guardados dentro de cada OrderItem). Isso é o que comprova, no
 * código, que a relação com Product é fraca (agregação) e indireta.
 */
public class Order {

    // Atributos privados
    private int id;
    private int clienteId;         // referência ao Client -> AGREGAÇÃO (não é um objeto Client)
    private LocalDate data;
    private List<OrderItem> itens; // lista de itens -> COMPOSIÇÃO FORTE (o Order é "dono" dos itens)
    private String status;         // "PENDENTE", "PAGO" ou "CANCELADO"
    private boolean finalizado;

    /**
     * Construtor: recebe id, clienteId e o ClientRepository.
     * - data é preenchida automaticamente com a data atual (LocalDate.now()).
     * - itens começa como uma lista vazia (o pedido nasce sem itens).
     * - status começa como "PENDENTE".
     * - finalizado começa como false (o pedido ainda pode ser alterado).
     *
     * BÔNUS - Validação extra: antes de criar o pedido, verificamos
     * no clienteRepo se esse clienteId realmente corresponde a um
     * cliente cadastrado. Isso evita criar um "pedido fantasma",
     * associado a um cliente que não existe no sistema.
     *
     * IMPORTANTE: o ClientRepository é usado SÓ durante a validação,
     * dentro deste construtor. Ele NÃO é guardado como atributo do
     * Order. Continuamos guardando apenas "this.clienteId = clienteId"
     * - a agregação (referência fraca por id) não muda em nada.
     */
    public Order(int id, int clienteId, ClientRepository clienteRepo) {
        if (clienteRepo == null || !clienteRepo.existe(clienteId)) {
            throw new IllegalArgumentException(
                "Não é possível criar o pedido: cliente com id " + clienteId + " não existe.");
        }
        this.id = id;
        this.clienteId = clienteId;
        this.data = LocalDate.now();
        this.itens = new ArrayList<>();
        this.status = "PENDENTE";
        this.finalizado = false;
    }

    // ---------- Getters ----------
    // Não criamos setter para "itens" propositalmente: a única forma
    // de alterar os itens do pedido é através dos métodos
    // adicionarItem/removeItem, que sabem respeitar a regra de
    // "pedido finalizado não pode mais ser alterado".

    public int getId() {
        return id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public LocalDate getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    /**
     * Retorna uma cópia NÃO MODIFICÁVEL da lista de itens.
     * Isso protege a composição: se devolvêssemos a lista "itens"
     * diretamente, quem chamasse esse getter poderia fazer
     * order.getItens().add(...) e burlar as regras de negócio
     * (como a verificação de pedido finalizado).
     */
    public List<OrderItem> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /**
     * Cria um novo OrderItem (com id sequencial, baseado no tamanho
     * atual da lista) e adiciona à lista de itens do pedido.
     *
     * Reforçando a COMPOSIÇÃO: é o próprio Order que instancia o
     * OrderItem (new OrderItem(...)) dentro deste método. Quem usa a
     * classe Order de fora NUNCA cria um OrderItem diretamente -
     * o pedido é o único responsável por dar vida aos seus itens.
     *
     * IMPORTANTE: este método NÃO altera o estoque do produto. Quem
     * decide dar baixa no estoque é um sistema externo (ex: o Main,
     * ou um futuro "EstoqueService"), conforme pedido no enunciado.
     *
     * BÔNUS - Validação extra: antes de criar o item, verificamos no
     * produtoRepo se esse produtoId realmente existe no catálogo.
     * Assim como no construtor, o ProductRepository é usado SÓ aqui,
     * na validação - ele não é guardado como atributo do Order nem
     * do OrderItem, que continuam guardando apenas "produtoId" (int).
     */
    public void adicionarItem(int produtoId, int quantidade, double precoUnitario, ProductRepository produtoRepo) {
        if (finalizado) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido já finalizado.");
        }
        if (produtoRepo == null || !produtoRepo.existe(produtoId)) {
            throw new IllegalArgumentException(
                "Não é possível adicionar item: produto com id " + produtoId + " não existe.");
        }
        int novoId = itens.size() + 1; // id sequencial simples dentro do pedido
        OrderItem novoItem = new OrderItem(novoId, produtoId, quantidade, precoUnitario);
        itens.add(novoItem);
    }

    /**
     * Remove um item da lista, pelo índice (posição na lista).
     * Lança exceção se o pedido já estiver finalizado, pelo mesmo
     * motivo de adicionarItem: pedido finalizado é imutável.
     */
    public void removeItem(int index) {
        if (finalizado) {
            throw new IllegalStateException("Não é possível remover itens de um pedido já finalizado.");
        }
        if (index < 0 || index >= itens.size()) {
            throw new IndexOutOfBoundsException("Índice de item inválido: " + index);
        }
        itens.remove(index);
    }

    /**
     * Soma os subtotais de todos os itens do pedido.
     * Cada item já sabe calcular o próprio subtotal
     * (preço congelado * quantidade) - o Order só soma os resultados.
     */
    public double calcularTotal() {
        double total = 0.0;
        for (OrderItem item : itens) {
            total += item.calcularSubtotal();
        }
        return total;
    }

    /**
     * Muda o status do pedido para "PAGO" e marca como finalizado.
     * A partir desse ponto, adicionarItem/removeItem passam a lançar
     * exceção, garantindo que um pedido pago não seja mais alterado.
     */
    public void finalizarPedido() {
        this.status = "PAGO";
        this.finalizado = true;
    }

    /**
     * Retorna quantos itens (linhas) existem no pedido.
     * Note que isso é o número de OrderItems, não a soma das
     * quantidades de produtos.
     */
    public int getQuantidadeItens() {
        return itens.size();
    }

    /**
     * Exibe todas as informações do pedido, incluindo os detalhes do
     * cliente e de cada produto comprado.
     *
     * IMPORTANTE (deixado explícito pelo enunciado): para exibir o
     * NOME do produto, este método precisa RECEBER os produtos como
     * parâmetro (Product... produtos), pois o Order só guarda o
     * "produtoId" dentro de cada OrderItem - ele não tem uma
     * referência direta aos objetos Product. Isso é a prova, em
     * código, de que a relação OrderItem -> Product é uma AGREGAÇÃO:
     * o pedido não "contém" os produtos, apenas conhece seus ids.
     *
     * @param cliente  o Client dono do pedido (recebido de fora,
     *                 pois Order só guarda o clienteId)
     * @param produtos lista de produtos conhecidos pelo sistema,
     *                 usada para "traduzir" produtoId -> nome do produto
     */
    public void exibirResumo(Client cliente, Product... produtos) {
        System.out.println("===== Resumo do Pedido #" + id + " =====");
        System.out.println("Data: " + data);
        System.out.println("Status: " + status + (finalizado ? " (finalizado)" : " (em aberto)"));
        System.out.println("Cliente: " + (cliente != null ? cliente.getNome() : "id=" + clienteId));
        System.out.println("Itens:");

        for (OrderItem item : itens) {
            // Procura, na lista de produtos recebida por parâmetro,
            // aquele cujo id é igual ao produtoId guardado no item.
            Product produtoEncontrado = null;
            for (Product p : produtos) {
                if (p.getId() == item.getProdutoId()) {
                    produtoEncontrado = p;
                    break;
                }
            }
            String nomeProduto = (produtoEncontrado != null) ? produtoEncontrado.getNome()
                    : "Produto#" + item.getProdutoId();

            System.out.println("  - " + nomeProduto
                    + " | qtd: " + item.getQuantidade()
                    + " | preço unit. (congelado no pedido): R$ " + item.getPrecoUnitario()
                    + " | subtotal: R$ " + item.calcularSubtotal());
        }

        System.out.println("TOTAL DO PEDIDO: R$ " + calcularTotal());
        System.out.println("==========================================");
    }
}
