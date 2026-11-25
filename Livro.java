package biblioteca;

public class Livro {
    private final int id;
    private final String titulo;
    private final String autor;
    private final String genero;
    private final int ano;
    private final int totalCopias;
    private int disponiveis;

    public Livro(int id, String titulo, String autor, String genero, int ano, int totalCopias) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.ano = ano;
        this.totalCopias = totalCopias;
        this.disponiveis = totalCopias;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getGenero() { return genero; }
    public int getAno() { return ano; }
    public int getTotalCopias() { return totalCopias; }
    public int getDisponiveis() { return disponiveis; }

    void retirarCopia() { disponiveis--; }
    void devolverCopia() { disponiveis++; }

    public boolean contem(String termo) {
        String t = termo.toLowerCase();
        return titulo.toLowerCase().contains(t)
                || autor.toLowerCase().contains(t)
                || genero.toLowerCase().contains(t);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s (%s, %d) | disponíveis: %d/%d",
                id, titulo, autor, genero, ano, disponiveis, totalCopias);
    }
}
