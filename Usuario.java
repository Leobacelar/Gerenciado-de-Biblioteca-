package biblioteca;

import java.math.BigDecimal;

public class Usuario {
    private final int id;
    private final String nome;
    private final String email;
    private final String senha;
    private final boolean admin;
    private BigDecimal multaPendente = BigDecimal.ZERO;
    private int livrosComprados = 0;

    // Construtor padrão (mantém compatibilidade com o simulador e o resto do grupo)
    public Usuario(int id, String nome, String email) {
        this(id, nome, email, "1234", false);
    }

    // Construtor completo com senha e perfil (admin ou comum)
    public Usuario(int id, String nome, String email, String senha, boolean admin) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.admin = admin;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public boolean isAdmin() { return admin; }
    public boolean validarSenha(String tentativa) { return senha.equals(tentativa); }
    public BigDecimal getMultaPendente() { return multaPendente; }

    void adicionarMulta(BigDecimal valor) { multaPendente = multaPendente.add(valor); }
    void quitarMulta() { multaPendente = BigDecimal.ZERO; }

    public int getLivrosComprados() { return livrosComprados; }
    public NivelFidelidade getNivel() { return NivelFidelidade.porLivrosComprados(livrosComprados); }
    void registrarCompra(int quantidade) { livrosComprados += quantidade; }

    public boolean temMulta() { return multaPendente.signum() > 0; }

    @Override
    public String toString() {
        String perfil = admin ? "ADMIN" : "LEITOR";
        return String.format("[%d] [%s] %s <%s> | multa pendente: R$ %.2f", id, perfil, nome, email, multaPendente);
    }
}