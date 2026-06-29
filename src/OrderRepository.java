import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe OrderRepository (BÔNUS - Persistência + Relatório).
 *
 * Mesma ideia dos outros repositórios: o Order representa UM pedido,
 * e o OrderRepository é quem administra a COLEÇÃO de todos os
 * pedidos já criados no sistema.
 *
 * Esta classe também resolve o item de bônus "Relatório": como ela
 * conhece todos os pedidos, ela consegue filtrar e listar apenas os
 * pedidos que pertencem a um cliente específico (método
 * listarPedidosPorCliente). Sem um repositório centralizando os
 * pedidos, essa busca não seria possível, já que o Order não tem
 * nenhuma referência aos "outros" pedidos.
 */
public class OrderRepository {

    private Map<Integer, Order> pedidos = new HashMap<>();

    public void adicionar(Order pedido) {
        pedidos.put(pedido.getId(), pedido);
    }

    public Order buscarPorId(int id) {
        return pedidos.get(id);
    }

    public boolean existe(int id) {
        return pedidos.containsKey(id);
    }

    public Collection<Order> listarTodos() {
        return Collections.unmodifiableCollection(pedidos.values());
    }

    /**
     * RELATÓRIO: percorre todos os pedidos cadastrados e devolve
     * apenas aqueles cujo clienteId é igual ao clienteId informado.
     *
     * Repare que essa busca só é possível PORQUE o Order guarda o
     * clienteId (agregação). Se o Order não soubesse a qual cliente
     * pertence, não teria como filtrar "os pedidos do João Silva".
     */
    public List<Order> listarPedidosPorCliente(int clienteId) {
        List<Order> resultado = new ArrayList<>();
        for (Order pedido : pedidos.values()) {
            if (pedido.getClienteId() == clienteId) {
                resultado.add(pedido);
            }
        }
        return resultado;
    }
}
