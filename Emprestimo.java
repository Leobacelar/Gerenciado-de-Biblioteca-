package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Emprestimo {
    public static final int PRAZO_DIAS = 7;
    public static final int MAX_RENOVACOES = 1;
    public static final BigDecimal MULTA_POR_DIA = new BigDecimal("1.50");

    private final int id;
    private final Livro livro;
    private final Usuario usuario;
    private final LocalDate dataEmprestimo;
    private LocalDate dataPrevista;
    private LocalDate dataDevolucao;
    private int renovacoes;

    public Emprestimo(int id, Livro livro, Usuario usuario, LocalDate dataEmprestimo) {
        this.id = id;
        this.livro = livro;
        this.usuario = usuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevista = dataEmprestimo.plusDays(PRAZO_DIAS);
    }

    public int getId() { return id; }
    public Livro getLivro() { return livro; }
    public Usuario getUsuario() { return usuario; }
    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public int getRenovacoes() { return renovacoes; }
    public boolean estaAtivo() { return dataDevolucao == null; }

    
    public long diasAtraso(LocalDate referencia) {
        LocalDate fim = dataDevolucao != null ? dataDevolucao : referencia;
        return Math.max(0, ChronoUnit.DAYS.between(dataPrevista, fim));
    }

    public BigDecimal calcularMulta(LocalDate referencia) {
        return MULTA_POR_DIA.multiply(BigDecimal.valueOf(diasAtraso(referencia)));
    }

    void renovar() {
        dataPrevista = dataPrevista.plusDays(PRAZO_DIAS);
        renovacoes++;
    }

    void registrarDevolucao(LocalDate data) { dataDevolucao = data; }

    @Override
    public String toString() {
        return String.format("[%d] \"%s\" -> %s | emprestado em %s | devolver até %s%s",
                id, livro.getTitulo(), usuario.getNome(), dataEmprestimo, dataPrevista,
                dataDevolucao != null ? " | devolvido em " + dataDevolucao : "");
    }
}
