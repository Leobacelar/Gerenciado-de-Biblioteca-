package biblioteca;

import java.time.LocalDate;


public class Agendamento {
    public enum Status { ATIVO, ATENDIDO, CANCELADO }

    private final int id;
    private final Livro livro;
    private final Usuario usuario;
    private final LocalDate dataRetirada;
    private Status status = Status.ATIVO;

    public Agendamento(int id, Livro livro, Usuario usuario, LocalDate dataRetirada) {
        this.id = id;
        this.livro = livro;
        this.usuario = usuario;
        this.dataRetirada = dataRetirada;
    }

    public int getId() { return id; }
    public Livro getLivro() { return livro; }
    public Usuario getUsuario() { return usuario; }
    public LocalDate getDataRetirada() { return dataRetirada; }
    public Status getStatus() { return status; }
    public boolean estaAtivo() { return status == Status.ATIVO; }

    void atender() { status = Status.ATENDIDO; }
    void cancelar() { status = Status.CANCELADO; }

    @Override
    public String toString() {
        return String.format("[%d] \"%s\" para %s | retirada em %s | %s",
                id, livro.getTitulo(), usuario.getNome(), dataRetirada, status);
    }
}
