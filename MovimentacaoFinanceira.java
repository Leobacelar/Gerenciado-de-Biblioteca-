package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MovimentacaoFinanceira {
    public enum Tipo {
        ENTRADA,
        SAIDA
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int id;
    private final Tipo tipo;
    private final String categoria;
    private final String descricao;
    private final BigDecimal valor;
    private final LocalDate data;

    public MovimentacaoFinanceira(
            int id,
            Tipo tipo,
            String categoria,
            String descricao,
            BigDecimal valor,
            LocalDate data) {
        if (id < 1) {
            throw new BibliotecaException("ID da movimentação deve ser maior que zero.");
        }
        if (tipo == null) {
            throw new BibliotecaException("Tipo da movimentação é obrigatório.");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new BibliotecaException("Categoria da movimentação é obrigatória.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new BibliotecaException("Descrição da movimentação é obrigatória.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BibliotecaException("Valor da movimentação deve ser maior que zero.");
        }
        if (data == null) {
            throw new BibliotecaException("Data da movimentação é obrigatória.");
        }

        this.id = id;
        this.tipo = tipo;
        this.categoria = categoria.trim();
        this.descricao = descricao.trim();
        this.valor = valor;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimentacaoFinanceira that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String sinal = (tipo == Tipo.ENTRADA) ? "+" : "-";
        return String.format("[%d] %s | %-7s | %-15s | %s R$ %.2f | %s",
                id, data.format(FMT), tipo, categoria, sinal, valor, descricao);
    }
}