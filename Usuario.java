package biblioteca;

import java.math.BigDecimal;

public class Usuario {
    private final int id;
    private final String nome;
    private final String email;
    private BigDecimal multaPendente = BigDecimal.ZERO;

    public Usuario(int id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public BigDecimal getMultaPendente() { return multaPendente; }

    void adicionarMulta(BigDecimal valor) { multaPendente = multaPendente.add(valor); }
    void quitarMulta() { multaPendente = BigDecimal.ZERO; }

    public boolean temMulta() { return multaPendente.signum() > 0; }

    @Override
    public String toString() {
        return String.format("[%d] %s <%s> | multa pendente: R$ %.2f", id, nome, email, multaPendente);
    }
}
