package biblioteca;

import java.util.Comparator;

// Ordem: mais pontos, depois mais popular, depois título (A-Z)
public class ComparadorRecomendacoes implements Comparator<Recomendacao> {

    @Override
    public int compare( Recomendacao r1, Recomendacao r2 ) {
        int diffPontos = r2.getPontos() - r1.getPontos();
        if( diffPontos != 0 ){
            return diffPontos;
        }

        int diffPopularidade = r2.getPopularidade() - r1.getPopularidade();
        if( diffPopularidade != 0 ){
            return diffPopularidade;
        }

        return r1.getLivro().getTitulo().compareToIgnoreCase( r2.getLivro().getTitulo() );
    }
}
