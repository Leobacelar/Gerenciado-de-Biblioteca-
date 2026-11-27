package biblioteca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Recomendador {
    public static final int MAX_RECOMENDACOES = 3;
    private static final int PESO_AUTOR = 3;
    private static final int PESO_GENERO = 2;

    public static List<Recomendacao> recomendar( Biblioteca bib, Usuario usuario ) {
        // 1. Livros que o usuário já pegou emprestado ou comprou (Set não repete)
        Set<Livro> jaConhece = new HashSet<>();
        // 2. Popularidade: quantas vezes cada livro foi emprestado ou vendido
        Map<Livro, Integer> popularidade = new HashMap<>();

        for( Emprestimo e : bib.listarEmprestimos() ){
            somar( popularidade, e.getLivro() );
            if( e.getUsuario().equals(usuario) ){
                jaConhece.add( e.getLivro() );
            }
        }
        for( Compra c : bib.listarCompras() ){
            for( Livro l : c.getLivros() ){
                somar( popularidade, l );
                if( c.getUsuario().equals(usuario) ){
                    jaConhece.add(l);
                }
            }
        }

        // 3. Gosto do usuário: contagem por gênero e por autor
        Map<String, Integer> generos = new HashMap<>();
        Map<String, Integer> autores = new HashMap<>();
        for( Livro l : jaConhece ){
            somar( generos, l.getGenero().toLowerCase() );
            somar( autores, l.getAutor().toLowerCase() );
        }

        // 4. Pontua os livros que ele ainda não conhece e que estão disponíveis
        List<Recomendacao> recomendacoes = new ArrayList<>();
        for( Livro l : bib.listarLivros() ){
            boolean indisponivel = l.getDisponiveis() <= 0 && !l.estaAVenda();
            if( jaConhece.contains(l) || indisponivel ){
                continue;
            }
            int doAutor = autores.getOrDefault( l.getAutor().toLowerCase(), 0 );
            int doGenero = generos.getOrDefault( l.getGenero().toLowerCase(), 0 );
            int pontos = doAutor * PESO_AUTOR + doGenero * PESO_GENERO;

            String motivo;
            if( doAutor > 0 ){
                motivo = "você já leu ou comprou obras de " + l.getAutor();
            } else if( doGenero > 0 ){
                motivo = "você gosta do gênero " + l.getGenero();
            } else if( popularidade.getOrDefault(l, 0) > 0 ){
                motivo = "procurado por outros usuários";
            } else {
                motivo = "novidade para você no acervo";
            }
            recomendacoes.add( new Recomendacao(l, pontos, popularidade.getOrDefault(l, 0), motivo) );
        }

        recomendacoes.sort( new ComparadorRecomendacoes() );

        if( recomendacoes.size() > MAX_RECOMENDACOES ){
            return new ArrayList<>( recomendacoes.subList(0, MAX_RECOMENDACOES) );
        }
        return recomendacoes;
    }

    private static <T> void somar( Map<T, Integer> mapa, T chave ) {
        mapa.put( chave, mapa.getOrDefault(chave, 0) + 1 );
    }
}
