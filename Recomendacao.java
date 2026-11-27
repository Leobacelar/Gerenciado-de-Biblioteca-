package biblioteca;

public class Recomendacao {
    private final Livro livro;
    private final int pontos;
    private final int popularidade;
    private final String motivo;

    Recomendacao( Livro livro, int pontos, int popularidade, String motivo ) {
        this.livro = livro;
        this.pontos = pontos;
        this.popularidade = popularidade;
        this.motivo = motivo;
    }

    public Livro getLivro() { return livro; }
    public int getPontos() { return pontos; }
    public int getPopularidade() { return popularidade; }
    public String getMotivo() { return motivo; }

    @Override
    public String toString() {
        return livro + "\n     motivo: " + motivo;
    }
}
