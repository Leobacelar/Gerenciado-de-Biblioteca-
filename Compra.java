package biblioteca;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    public static final int LIVROS_PARA_BONUS = 3;
    public static final BigDecimal VALOR_BONUS = new BigDecimal("10.00");

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int id;
    private final Usuario usuario;
    private final LocalDate data;
    private final List<Livro> livros;
    private final List<Desconto> descontos = new ArrayList<>();
    // Valores calculados na criação: mudar o preço do livro depois não altera a compra
    private final BigDecimal subtotal;
    private final BigDecimal total;

    Compra( int id, Usuario usuario, List<Livro> livros, LocalDate data ) {
        this.id = id;
        this.usuario = usuario;
        this.data = data;
        this.livros = new ArrayList<>(livros);

        BigDecimal soma = BigDecimal.ZERO;
        for( Livro l : this.livros ){
            soma = soma.add(l.getPreco());
        }
        this.subtotal = soma;

        // O nível usado é o que o usuário tinha ANTES desta compra
        NivelFidelidade nivel = usuario.getNivel();
        if( nivel.getPorcentagemDesconto() > 0 ){
            descontos.add( new DescontoRelativo(nivel.getPorcentagemDesconto()) );
        }
        if( this.livros.size() >= LIVROS_PARA_BONUS ){
            descontos.add( new DescontoAbsoluto(VALOR_BONUS) );
        }

        BigDecimal valorFinal = this.subtotal;
        for( Desconto d : descontos ){
            valorFinal = d.valorReduzido(valorFinal);
        }
        this.total = valorFinal;
    }

    public int getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public LocalDate getData() { return data; }
    public List<Livro> getLivros() { return new ArrayList<>(livros); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTotal() { return total; }

    @Override
    public String toString() {
        String texto = String.format("[%d] %s | %s | %d livro(s)",
                id, data.format(FMT), usuario.getNome(), livros.size());
        for( Livro l : livros ){
            texto += "\n     - " + l.getTitulo();
        }
        texto += String.format("%n     subtotal: R$ %.2f", subtotal);
        for( Desconto d : descontos ){
            texto += "\n     desconto: " + d.descricao();
        }
        texto += String.format("%n     total: R$ %.2f", total);
        return texto;
    }

    private abstract class Desconto {
        public abstract BigDecimal valorReduzido( BigDecimal valorOriginal );
        public abstract String descricao();
    }

    private class DescontoAbsoluto extends Desconto {
        private final BigDecimal valor;

        DescontoAbsoluto( BigDecimal valor ) {
            this.valor = valor;
        }

        @Override
        public BigDecimal valorReduzido( BigDecimal valorOriginal ) {
            if( valorOriginal.compareTo(valor) < 0 ){
                return BigDecimal.ZERO;
            }
            return valorOriginal.subtract(valor);
        }

        @Override
        public String descricao() {
            return String.format("R$ %.2f por levar %d livros ou mais", valor, LIVROS_PARA_BONUS);
        }
    }

    private class DescontoRelativo extends Desconto {
        private final int porcentagem;

        DescontoRelativo( int porcentagem ) {
            this.porcentagem = porcentagem;
        }

        @Override
        public BigDecimal valorReduzido( BigDecimal valorOriginal ) {
            BigDecimal desconto = valorOriginal
                    .multiply(BigDecimal.valueOf(porcentagem))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return valorOriginal.subtract(desconto);
        }

        @Override
        public String descricao() {
            return porcentagem + "% de fidelidade";
        }
    }
}
