package biblioteca;

// Benefícios de quem compra livros: quanto mais livros comprados, maior o nível.
public enum NivelFidelidade implements Sequencial<NivelFidelidade> {
    BRONZE( "Bronze", 0, 0, Biblioteca.MAX_EMPRESTIMOS_POR_USUARIO ) {
        @Override
        public NivelFidelidade proximo() {
            return PRATA;
        }
    },
    PRATA( "Prata", 3, 5, Biblioteca.MAX_EMPRESTIMOS_POR_USUARIO + 1 ) {
        @Override
        public NivelFidelidade proximo() {
            return OURO;
        }
    },
    OURO( "Ouro", 6, 10, Biblioteca.MAX_EMPRESTIMOS_POR_USUARIO + 2 ) {
        @Override
        public NivelFidelidade proximo() {
            return this;
        }

        @Override
        public boolean temProximo() {
            return false;
        }
    };

    private final String nome;
    private final int livrosNecessarios;
    private final int porcentagemDesconto;
    private final int limiteEmprestimos;

    NivelFidelidade( String nome, int livrosNecessarios, int porcentagemDesconto, int limiteEmprestimos ) {
        this.nome = nome;
        this.livrosNecessarios = livrosNecessarios;
        this.porcentagemDesconto = porcentagemDesconto;
        this.limiteEmprestimos = limiteEmprestimos;
    }

    @Override
    public boolean temProximo() {
        return true;
    }

    // Avança de nível enquanto a quantidade comprada alcançar o próximo
    public static NivelFidelidade porLivrosComprados( int quantidade ) {
        NivelFidelidade nivel = BRONZE;
        while( nivel.temProximo() && quantidade >= nivel.proximo().getLivrosNecessarios() ){
            nivel = nivel.proximo();
        }
        return nivel;
    }

    public String getNome() { return nome; }
    public int getLivrosNecessarios() { return livrosNecessarios; }
    public int getPorcentagemDesconto() { return porcentagemDesconto; }
    public int getLimiteEmprestimos() { return limiteEmprestimos; }

    @Override
    public String toString() {
        return nome + " (" + porcentagemDesconto + "% de desconto nas compras, até "
                + limiteEmprestimos + " empréstimos simultâneos)";
    }
}
