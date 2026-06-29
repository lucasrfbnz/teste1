/**
 * Classe Client (Cliente).
 *
 * RELACIONAMENTO: Order -> Client é uma AGREGAÇÃO (relação FRACA).
 * Isso significa que o Cliente EXISTE INDEPENDENTEMENTE do Pedido.
 * Se um Pedido for deletado, o Cliente continua existindo no sistema
 * (ele pode ter outros pedidos, ou nenhum pedido).
 *
 * Por isso, dentro da classe Order, NÃO guardamos um objeto Client
 * inteiro. Guardamos apenas o "clienteId" (int), que é só uma
 * REFERÊNCIA para o cliente. Isso deixa explícito que o pedido não
 * é "donO" do cliente, apenas aponta para ele.
 */
public class Client {

    // Atributos privados (encapsulamento - ninguém de fora acessa direto)
    private int id;
    private String nome;
    private String email;
    private String telefone;

    /**
     * Construtor: recebe todos os atributos do cliente.
     */
    public Client(int id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    // ---------- Getters e Setters ----------
    // Necessários porque os atributos são privados (encapsulamento),
    // então o acesso externo precisa passar por esses métodos.

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    /**
     * Sobrescrita de toString apenas para facilitar a exibição
     * de informações do cliente nos testes do Main.
     */
    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nome='" + nome + "', email='" + email
                + "', telefone='" + telefone + "'}";
    }
}
