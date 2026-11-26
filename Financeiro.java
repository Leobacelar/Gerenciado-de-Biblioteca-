package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Financeiro {
    public record Resumo(
            BigDecimal totalEntradas,
            BigDecimal totalSaidas,
            BigDecimal saldoCaixa,
            BigDecimal multasPendentes,
            int quantidadeMovimentacoes
    ) {}

    private final Map<Integer, MovimentacaoFinanceira> movimentacoes = new LinkedHashMap<>();
    private int proxId = 1;

    public MovimentacaoFinanceira registrar(
            MovimentacaoFinanceira.Tipo tipo,
            String categoria,
            String descricao,
            BigDecimal valor,
            LocalDate data) {
        MovimentacaoFinanceira mov = new MovimentacaoFinanceira(proxId++, tipo, categoria, descricao, valor, data);
        movimentacoes.put(mov.getId(), mov);
        return mov;
    }

    public MovimentacaoFinanceira registrarPagamentoMulta(Usuario usuario, BigDecimal valorPago, LocalDate data) {
        if (usuario == null) {
            throw new BibliotecaException("Usuário é obrigatório para registrar pagamento de multa.");
        }
        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BibliotecaException("Valor da multa paga deve ser maior que zero.");
        }
        LocalDate dataEfetiva = (data != null) ? data : LocalDate.now();
        String descricao = String.format("Pagamento de multa - %s (ID %d)", usuario.getNome(), usuario.getId());
        return registrar(MovimentacaoFinanceira.Tipo.ENTRADA, "Multa", descricao, valorPago, dataEfetiva);
    }

    public MovimentacaoFinanceira buscarPorId(int id) {
        MovimentacaoFinanceira mov = movimentacoes.get(id);
        if (mov == null) {
            throw new BibliotecaException("Movimentação " + id + " não encontrada.");
        }
        return mov;
    }

    public MovimentacaoFinanceira removerPorId(int id) {
        MovimentacaoFinanceira removida = movimentacoes.remove(id);
        if (removida == null) {
            throw new BibliotecaException("Movimentação " + id + " não encontrada para remoção.");
        }
        return removida;
    }

    public List<MovimentacaoFinanceira> listarMovimentacoes() {
        return new ArrayList<>(movimentacoes.values());
    }

    public List<MovimentacaoFinanceira> filtrarPorTipo(MovimentacaoFinanceira.Tipo tipo) {
        if (tipo == null) {
            throw new BibliotecaException("Tipo é obrigatório para filtrar movimentações.");
        }
        return movimentacoes.values().stream()
                .filter(m -> m.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    public List<MovimentacaoFinanceira> filtrarPorCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new BibliotecaException("Categoria é obrigatória para filtrar movimentações.");
        }
        String alvo = categoria.trim().toLowerCase();
        return movimentacoes.values().stream()
                .filter(m -> m.getCategoria().toLowerCase().equals(alvo))
                .collect(Collectors.toList());
    }

    public List<MovimentacaoFinanceira> filtrarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new BibliotecaException("Data inicial e data final são obrigatórias.");
        }
        if (fim.isBefore(inicio)) {
            throw new BibliotecaException("Data final não pode ser anterior à data inicial.");
        }
        return movimentacoes.values().stream()
                .filter(m -> !m.getData().isBefore(inicio) && !m.getData().isAfter(fim))
                .collect(Collectors.toList());
    }

    public BigDecimal totalEntradas() {
        return somarPorTipo(MovimentacaoFinanceira.Tipo.ENTRADA);
    }

    public BigDecimal totalSaidas() {
        return somarPorTipo(MovimentacaoFinanceira.Tipo.SAIDA);
    }

    public BigDecimal saldoCaixa() {
        return totalEntradas().subtract(totalSaidas());
    }

    public BigDecimal totalMultasPendentes(Collection<Usuario> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return usuarios.stream()
                .map(Usuario::getMultaPendente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Resumo gerarResumo(Collection<Usuario> usuarios) {
        return new Resumo(
                totalEntradas(),
                totalSaidas(),
                saldoCaixa(),
                totalMultasPendentes(usuarios),
                movimentacoes.size()
        );
    }

    private BigDecimal somarPorTipo(MovimentacaoFinanceira.Tipo tipo) {
        return movimentacoes.values().stream()
                .filter(m -> m.getTipo() == tipo)
                .map(MovimentacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}