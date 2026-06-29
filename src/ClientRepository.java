import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe ClientRepository (BÔNUS - Persistência).
 *
 * Esta classe tem uma responsabilidade única: GERENCIAR os clientes
 * cadastrados no sistema (guardar, buscar, verificar existência).
 *
 * Por que separar isso da classe Client? Porque a classe Client deve
 * representar APENAS um cliente individual (seus dados: nome, email,
 * etc). Quem decide "onde" e "como" todos os clientes são guardados
 * é responsabilidade de outra classe - o Repository. Esse é um
 * padrão de projeto muito comum chamado "Repository Pattern":
 * separar a entidade (Client) de quem administra a coleção de
 * entidades (ClientRepository).
 *
 * Essa classe também é o que torna possível a VALIDAÇÃO de existência
 * de cliente: o Order, antes de ser criado, pergunta ao
 * ClientRepository "esse clienteId existe?" (veja Order.java).
 */
public class ClientRepository {

    // Usamos um Map<Integer, Client> em vez de uma List<Client> porque
    // buscar um cliente pelo id (clienteRepo.buscarPorId(5)) fica muito
    // mais rápido com Map (busca O(1)) do que percorrendo uma lista
    // inteira procurando o id (busca O(n)).
    private Map<Integer, Client> clientes = new HashMap<>();

    /**
     * Adiciona (ou substitui, se o id já existir) um cliente no repositório.
     */
    public void adicionar(Client cliente) {
        clientes.put(cliente.getId(), cliente);
    }

    /**
     * Busca um cliente pelo id. Retorna null se não encontrar -
     * por isso, antes de usar o resultado, é importante checar
     * existe(id) ou tratar o retorno null.
     */
    public Client buscarPorId(int id) {
        return clientes.get(id);
    }

    /**
     * Verifica se existe um cliente cadastrado com esse id.
     * Este é o método usado pelo Order para validar o clienteId
     * antes de criar um pedido.
     */
    public boolean existe(int id) {
        return clientes.containsKey(id);
    }

    /**
     * Retorna todos os clientes cadastrados, em uma coleção
     * NÃO MODIFICÁVEL (mesma ideia usada em Order.getItens()):
     * quem chama este método pode ler a lista, mas não pode
     * adicionar/remover clientes "por fora" do repositório.
     */
    public Collection<Client> listarTodos() {
        return Collections.unmodifiableCollection(clientes.values());
    }
}
