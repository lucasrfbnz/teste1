import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe ProductRepository (BÔNUS - Persistência).
 *
 * Mesma ideia do ClientRepository, mas para gerenciar os produtos do
 * catálogo da loja. Veja os comentários em ClientRepository.java para
 * a explicação completa do "Repository Pattern".
 *
 * É este repositório que o Order consulta (através do método existe)
 * antes de adicionar um item ao pedido, para garantir que ninguém
 * compre um produto que não existe no catálogo.
 */
public class ProductRepository {

    private Map<Integer, Product> produtos = new HashMap<>();

    public void adicionar(Product produto) {
        produtos.put(produto.getId(), produto);
    }

    public Product buscarPorId(int id) {
        return produtos.get(id);
    }

    public boolean existe(int id) {
        return produtos.containsKey(id);
    }

    public Collection<Product> listarTodos() {
        return Collections.unmodifiableCollection(produtos.values());
    }
}
