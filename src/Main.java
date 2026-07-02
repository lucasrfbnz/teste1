import java.util.List;

/**
 * Classe Main - aqui executamos testes manuais para comprovar que o
 * sistema de pedidos funciona e que os relacionamentos (agregação e
 * composição) estão corretamente implementados.
 *
 * Lembrete dos dois tipos de relacionamento usados neste sistema:
 *
 * - AGREGAÇÃO (relação FRACA): Order -> Client e OrderItem -> Product.
 *   Implementada guardando apenas um ID (int clienteId / int produtoId).
 *   O "dono" e o "referenciado" têm ciclos de vida independentes.
 *
 * - COMPOSIÇÃO (relação FORTE): Order -> OrderItem.
 *   Implementada com "List<OrderItem> itens" criada e controlada
 *   inteiramente DENTRO da classe Order. Os itens não existem fora
 *   de um pedido.
 *
 * A partir daqui também demonstramos os itens de BÔNUS:
 * 1) Comparação explícita de memória (agregação vs composição "indevida").
 * 2) Validações extras (produto/cliente precisam existir nos repositórios).
 * 3) Persistência via ClientRepository / ProductRepository / OrderRepository.
 * 4) Relatório de pedidos por cliente.
 */
public class Main {

    public static void main(String[] args) {

        // ===================================================
        // BÔNUS 3 (Persistência) - criamos os repositórios primeiro,
        // pois agora Client/Product precisam estar CADASTRADOS antes
        // de qualquer pedido poder referenciá-los (ver bônus 2).
        // ===================================================
        ClientRepository clienteRepo = new ClientRepository();
        ProductRepository produtoRepo = new ProductRepository();
        OrderRepository pedidoRepo = new OrderRepository();

        // ===================================================
        // 1) CRIAR CLIENTES
        // ===================================================
        // Os clientes são criados de forma totalmente independente
        // dos pedidos - eles existem por si só no sistema. Aqui,
        // além de criá-los, também os registramos no clienteRepo,
        // que é quem vai "atestar" que eles existem.
        System.out.println(">>> 1) Criando clientes...");
        Client cliente1 = new Client(1, "João Silva", "joao.silva@email.com", "(11) 91234-5678");
        Client cliente2 = new Client(2, "Maria Santos", "maria.santos@email.com", "(11) 98765-4321");
        clienteRepo.adicionar(cliente1);
        clienteRepo.adicionar(cliente2);
        System.out.println(cliente1);
        System.out.println(cliente2);
        System.out.println();

        // ===================================================
        // 2) CRIAR PRODUTOS COM ESTOQUE
        // ===================================================
        // O construtor de Product sempre inicia o estoque em 0.
        // Por isso, para deixar o estoque já carregado, usamos o
        // método reporEstoque(...) logo após criar cada produto -
        // simulando o sistema externo de estoque mencionado no
        // enunciado (o Order NÃO é responsável por isso). Também
        // registramos cada produto no produtoRepo.
        System.out.println(">>> 2) Criando produtos com estoque...");
        Product notebook = new Product(1, "Notebook", 3500.00);
        notebook.reporEstoque(10);

        Product mouse = new Product(2, "Mouse", 50.00);
        mouse.reporEstoque(20);

        Product teclado = new Product(3, "Teclado", 200.00);
        teclado.reporEstoque(5);

        Product monitor = new Product(4, "Monitor", 1200.00);
        monitor.reporEstoque(3);

        produtoRepo.adicionar(notebook);
        produtoRepo.adicionar(mouse);
        produtoRepo.adicionar(teclado);
        produtoRepo.adicionar(monitor);

        System.out.println(notebook);
        System.out.println(mouse);
        System.out.println(teclado);
        System.out.println(monitor);
        System.out.println();

        // ===================================================
        // 3) CRIAR PEDIDOS
        // ===================================================
        // Repare que cada Order é criado passando apenas o
        // clienteId (cliente1.getId()) + o clienteRepo (usado só
        // para validar que o cliente existe). O Order continua sem
        // guardar o objeto Client inteiro. Isso é a AGREGAÇÃO sendo
        // aplicada na prática.
        System.out.println(">>> 3) Criando pedidos...");

        // ---- Pedido #1: João Silva compra 2 Notebooks e 3 Mouses ----
        // A validação de existência do cliente agora é feita AQUI, no
        // Main (que age como camada de serviço), ANTES de criar o Order.
        // O Order em si é uma entidade pura: só recebe id e clienteId.
        if (!clienteRepo.existe(cliente1.getId())) {
            throw new IllegalArgumentException("Cliente não encontrado: " + cliente1.getId());
        }
        Order pedido1 = new Order(1, cliente1.getId());

        // Validamos a existência do produto ANTES de chamar adicionarItem.
        // O preço guardado em cada item é uma CÓPIA do preço atual do
        // produto. A partir daqui, o pedido não depende mais do preço
        // "ao vivo" do produto.
        if (!produtoRepo.existe(notebook.getId())) throw new IllegalArgumentException("Produto não encontrado.");
        pedido1.adicionarItem(notebook.getId(), 2, notebook.getPreco());
        if (!produtoRepo.existe(mouse.getId())) throw new IllegalArgumentException("Produto não encontrado.");
        pedido1.adicionarItem(mouse.getId(), 3, mouse.getPreco());

        // Quem dá baixa no estoque é o "sistema externo" (aqui, o
        // próprio Main fazendo esse papel), e não o Order.
        notebook.baixarEstoque(2);
        mouse.baixarEstoque(3);

        // ---- Pedido #2: Maria Santos compra 1 Teclado e 2 Monitores ----
        if (!clienteRepo.existe(cliente2.getId())) {
            throw new IllegalArgumentException("Cliente não encontrado: " + cliente2.getId());
        }
        Order pedido2 = new Order(2, cliente2.getId());
        if (!produtoRepo.existe(teclado.getId())) throw new IllegalArgumentException("Produto não encontrado.");
        pedido2.adicionarItem(teclado.getId(), 1, teclado.getPreco());
        if (!produtoRepo.existe(monitor.getId())) throw new IllegalArgumentException("Produto não encontrado.");
        pedido2.adicionarItem(monitor.getId(), 2, monitor.getPreco());

        teclado.baixarEstoque(1);
        monitor.baixarEstoque(2);

        // Registramos os pedidos no pedidoRepo - é isso que vai
        // permitir gerar o relatório por cliente no passo 11.
        pedidoRepo.adicionar(pedido1);
        pedidoRepo.adicionar(pedido2);

        System.out.println("Pedidos criados com sucesso.");
        System.out.println();

        // ===================================================
        // 4) EXIBIR RESUMO DE CADA PEDIDO
        // ===================================================
        // exibirResumo precisa receber o Client e os Products como
        // parâmetro, pois o Order só guarda IDs (clienteId/produtoId),
        // nunca os objetos completos - prova da agregação.
        System.out.println(">>> 4) Exibindo resumo dos pedidos...");
        pedido1.exibirResumo(cliente1, notebook, mouse, teclado, monitor);
        pedido2.exibirResumo(cliente2, notebook, mouse, teclado, monitor);
        System.out.println();

        // ===================================================
        // 5) CALCULAR E EXIBIR O TOTAL DE CADA PEDIDO
        // ===================================================
        System.out.println(">>> 5) Calculando totais...");
        System.out.println("Total do Pedido #1: R$ " + pedido1.calcularTotal());
        System.out.println("Total do Pedido #2: R$ " + pedido2.calcularTotal());
        System.out.println();

        // ===================================================
        // 6) FINALIZAR O PEDIDO #1
        // ===================================================
        System.out.println(">>> 6) Finalizando o Pedido #1...");
        pedido1.finalizarPedido();
        System.out.println("Status do Pedido #1 agora: " + pedido1.getStatus()
                + " | finalizado? " + pedido1.isFinalizado());
        System.out.println();

        // ===================================================
        // 7) TENTAR ADICIONAR UM NOVO ITEM AO PEDIDO #1 (DEVE DAR ERRO)
        // ===================================================
        // Isso comprova a regra de negócio: depois de finalizado, o
        // pedido (e, por consequência, sua composição de itens)
        // não pode mais ser alterado.
        System.out.println(">>> 7) Tentando adicionar item a um pedido já finalizado...");
        try {
            pedido1.adicionarItem(teclado.getId(), 1, teclado.getPreco());
            System.out.println("ERRO: o item foi adicionado, mas isso não deveria ter acontecido!");
        } catch (IllegalStateException e) {
            System.out.println("Exceção capturada como esperado: " + e.getMessage());
        }
        System.out.println();

        // ===================================================
        // 8) DEMONSTRAR A INDEPENDÊNCIA (AGREGAÇÃO OrderItem -> Product)
        // ===================================================
        // Alteramos o preço "ao vivo" do produto Notebook. Como o
        // Pedido #1 já guardou uma CÓPIA do preço (precoUnitario)
        // dentro de cada OrderItem no momento da compra, o resumo do
        // pedido deve continuar mostrando o preço ANTIGO, mesmo após
        // essa alteração - prova de que o item de pedido não depende
        // mais do objeto Product após ser criado.
        System.out.println(">>> 8) Demonstrando independência entre OrderItem e Product...");
        System.out.println("Preço atual (ao vivo) do Notebook antes da alteração: R$ " + notebook.getPreco());

        notebook.setPreco(4200.00); // simula um reajuste de preço na loja
        System.out.println("Preço do Notebook alterado para: R$ " + notebook.getPreco());

        System.out.println("Exibindo novamente o resumo do Pedido #1:");
        pedido1.exibirResumo(cliente1, notebook, mouse, teclado, monitor);

        System.out.println(">>> Observe que, no resumo acima, o item do Notebook ainda mostra "
                + "o preço unitário ANTIGO (R$ 3500.0), preservado dentro do OrderItem, mesmo "
                + "o produto já estando com o novo preço (R$ " + notebook.getPreco() + ").");
        System.out.println();

        // ===================================================
        // 9) BÔNUS - VALIDAÇÕES EXTRAS (cliente/produto inexistentes)
        // ===================================================
        // A validação agora fica na camada de serviço (aqui, o Main),
        // ANTES de chamar Order. O Order não conhece repositório — quem
        // checa existência é o código que orquestra a operação.
        System.out.println(">>> 9) Testando validações extras (cliente/produto inexistentes)...");
        try {
            int clienteIdInexistente = 999;
            if (!clienteRepo.existe(clienteIdInexistente)) {
                throw new IllegalArgumentException("Cliente com id " + clienteIdInexistente + " não existe.");
            }
            new Order(99, clienteIdInexistente);
        } catch (IllegalArgumentException e) {
            System.out.println("Exceção capturada como esperado (cliente inexistente): " + e.getMessage());
        }

        try {
            int produtoIdInexistente = 888;
            if (!produtoRepo.existe(produtoIdInexistente)) {
                throw new IllegalArgumentException("Produto com id " + produtoIdInexistente + " não existe.");
            }
            pedido2.adicionarItem(produtoIdInexistente, 1, 10.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Exceção capturada como esperado (produto inexistente): " + e.getMessage());
        }
        System.out.println();

        // ===================================================
        // 10) BÔNUS - RELATÓRIO: listar todos os pedidos de um cliente
        // ===================================================
        // Esse relatório só é possível porque registramos os pedidos
        // no pedidoRepo (persistência) e porque cada Order guarda seu
        // clienteId (agregação) - é esse id que usamos para filtrar.
        System.out.println(">>> 10) Relatório: pedidos do cliente '" + cliente1.getNome() + "'...");
        List<Order> pedidosDoCliente1 = pedidoRepo.listarPedidosPorCliente(cliente1.getId());
        for (Order pedido : pedidosDoCliente1) {
            System.out.println("  - Pedido #" + pedido.getId() + " | status: " + pedido.getStatus()
                    + " | total: R$ " + pedido.calcularTotal());
        }
        System.out.println();

        // ===================================================
        // 11) BÔNUS - COMPARAÇÃO EXPLÍCITA DE MEMÓRIA
        // ===================================================
        System.out.println(">>> 11) Comparando memória: agregação (IDs) vs composição (objetos completos)...");
        compararMemoriaAgregacaoVsComposicao();
    }

    /**
     * BÔNUS 1 - Demonstra, na prática, por que a AGREGAÇÃO (guardar
     * apenas um id) é mais econômica em memória do que duplicar o
     * objeto inteiro (o que seria, na verdade, um uso indevido do
     * conceito de composição para uma relação que deveria ser fraca).
     *
     * Cenário simulado: 100.000 "pedidos" precisam apontar para o
     * MESMO cliente.
     *
     * - Versão AGREGAÇÃO (correta para Order -> Client): cada pedido
     *   guarda só um int (4 bytes) - o clienteId. O objeto Client
     *   existe UMA ÚNICA VEZ na memória, e todos compartilham essa
     *   mesma referência.
     *
     * - Versão "COMPOSIÇÃO INDEVIDA" (o que aconteceria se Order
     *   guardasse um Client inteiro, copiado, em vez de só o id):
     *   cada pedido carrega sua PRÓPRIA cópia completa de Client
     *   (id + 3 Strings), 100.000 vezes.
     *
     * IMPORTANTE: a primeira versão deste método tentava medir a
     * memória "ao vivo" com Runtime.totalMemory()/freeMemory() antes
     * e depois de alocar os objetos. Isso se mostrou pouco confiável
     * na prática: o Garbage Collector da JVM roda em momentos
     * imprevisíveis, então a medição às vezes dava até valores
     * NEGATIVOS (parecia que "sobrou" memória depois de alocar mais
     * objetos, o que não faz sentido). Por isso, aqui usamos uma
     * ESTIMATIVA determinística baseada no tamanho conhecido de cada
     * tipo (ponteiro/referência = 8 bytes em JVM 64 bits, int = 4
     * bytes, cabeçalho de objeto ~16 bytes no HotSpot). Não é o
     * número exato que a JVM usaria, mas é suficiente para mostrar,
     * de forma didática e repetível, a ORDEM DE GRANDEZA da diferença.
     */
    private static void compararMemoriaAgregacaoVsComposicao() {
        final int QUANTIDADE = 100_000;
        final int TAMANHO_REFERENCIA = 8; // ponteiro/referência em JVM 64 bits
        final int TAMANHO_INT = 4;
        final int CABECALHO_OBJETO = 16;  // overhead típico de um objeto no HotSpot

        // Tamanho estimado de UM objeto Client: cabeçalho + 1 int (id)
        // + 3 referências (nome, email, telefone são guardados como
        // referência para String, não como o texto "dentro" do Client).
        long tamanhoUmCliente = CABECALHO_OBJETO + TAMANHO_INT + 3 * TAMANHO_REFERENCIA;

        // ---- Versão AGREGAÇÃO: 100.000 ints apontando para 1 único Client ----
        long custoAgregacao = (long) QUANTIDADE * TAMANHO_INT + tamanhoUmCliente;

        // ---- Versão "composição indevida": 100.000 cópias completas de Client ----
        long custoComposicao = (long) QUANTIDADE * tamanhoUmCliente;

        System.out.println("Tamanho estimado de 1 objeto Client: ~" + tamanhoUmCliente + " bytes");
        System.out.println("Guardando " + QUANTIDADE + " referências (agregação - apenas ids) "
                + "+ 1 Client compartilhado: ~" + custoAgregacao + " bytes");
        System.out.println("Guardando " + QUANTIDADE + " objetos Client completos (cópia indevida): ~"
                + custoComposicao + " bytes");

        double vezes = custoComposicao / (double) custoAgregacao;
        System.out.println("=> A versão com objetos completos usaria aproximadamente "
                + String.format("%.1f", vezes) + "x mais memória que a versão com apenas ids.");

        System.out.println("Conclusão: por isso o Order guarda 'int clienteId' (agregação) "
                + "em vez de 'Client cliente' completo - evita duplicar dados que já existem "
                + "em outro lugar do sistema (o ClientRepository).");
    }
}
