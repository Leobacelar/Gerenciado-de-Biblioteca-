package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Scanner in = new Scanner(System.in);
    private static final Biblioteca bib = new Biblioteca();
    private static Usuario usuarioLogado = null;
    private static final Set<Integer> OPCOES_ADMIN =
            new HashSet<>(List.of(3, 4, 5, 10, 12, 13, 15, 19, 20));

    public static void main(String[] args) {
        carregarDadosExemplo();
        System.out.println("=== Sistema de Biblioteca ===");
        System.out.printf("Prazo: %d dias | Multa: R$ %.2f/dia | Máx. %d a %d empréstimos (conforme o nível)%n",
                Emprestimo.PRAZO_DIAS, Emprestimo.MULTA_POR_DIA,
                NivelFidelidade.BRONZE.getLimiteEmprestimos(), NivelFidelidade.OURO.getLimiteEmprestimos());

        while (true) {
            if (usuarioLogado == null) {
                if (!telaLogin()) break;
            } else {
                menu();
                int op = lerInt("Escolha: ");
                try {
                    executar(op);
                } catch (BibliotecaException e) {
                    System.out.println("⚠ " + e.getMessage());
                }
            }
        }
        System.out.println("Até logo!");
    }

    private static boolean telaLogin() {
        System.out.println("\n=== ACESSO AO SISTEMA ===");
        System.out.println("1 - Entrar (Login)");
        System.out.println("2 - Criar conta de Leitor");
        System.out.println("0 - Sair do programa");
        int op = lerInt("Escolha: ");

        try {
            if (op == 1) {
                String email = lerTexto("E-mail: ");
                String senha = lerTexto("Senha: ");
                usuarioLogado = bib.autenticar(email, senha);
                System.out.printf("✔ Bem-vindo(a), %s! [Perfil: %s]%n",
                        usuarioLogado.getNome(), usuarioLogado.isAdmin() ? "ADMINISTRADOR" : "LEITOR");
            } else if (op == 2) {
                String nome = lerTexto("Seu nome: ");
                String email = lerTexto("Seu e-mail: ");
                String senha = lerTexto("Crie uma senha: ");
                usuarioLogado = bib.cadastrarUsuario(nome, email, senha, false);
                System.out.println("✔ Conta criada e conectada com sucesso!");
            } else if (op == 0) {
                return false;
            } else {
                System.out.println("Opção inválida.");
            }
        } catch (BibliotecaException e) {
            System.out.println("⚠ " + e.getMessage());
        }
        return true;
    }

    private static void menu() {
        System.out.printf("\n--- MENU (%s | %s) ---%n",
                usuarioLogado.getNome(), usuarioLogado.isAdmin() ? "ADMIN" : "LEITOR");

        if (usuarioLogado.isAdmin()) {
            System.out.println("1  Listar catálogo        2  Buscar livro        3  Cadastrar livro");
            System.out.println("4  Listar usuários        5  Cadastrar usuário");
            System.out.println("6  Emprestar livro        7  Devolver livro      8  Renovar empréstimo");
            System.out.println("9  Empréstimos ativos     10 Empréstimos atrasados");
            System.out.println("11 Agendar livro          12 Agendamentos ativos 13 Cancelar agendamento");
            System.out.println("14 Pagar multa            15 Simular usuários");
            System.out.println("16 Vender livros          17 Benefícios/compras   18 Recomendações");
            System.out.println("19 Estoque de venda       20 Financeiro           0  Trocar usuário (Logout)");
        } else {
            System.out.println("1  Listar catálogo        2  Buscar livro");
            System.out.println("6  Pegar livro emprestado 7  Devolver livro      8  Renovar meu empréstimo");
            System.out.println("9  Meus empréstimos       11 Agendar livro       14 Pagar minha multa");
            System.out.println("16 Comprar livros         17 Meus benefícios      18 Recomendações para mim");
            System.out.println("0  Trocar usuário (Logout)");
        }
    }

    private static void executar(int op) {
        if (op == 0) {
            System.out.println("Desconectando " + usuarioLogado.getNome() + "...");
            usuarioLogado = null;
            return;
        }

        if (!usuarioLogado.isAdmin() && OPCOES_ADMIN.contains(op)) {
            throw new BibliotecaException("Acesso negado: opção exclusiva para administradores.");
        }

        switch (op) {
            case 1 -> imprimir(bib.listarLivros());
            case 2 -> imprimir(bib.buscarLivros(lerTexto("Termo (título/autor/gênero): ")));
            case 3 -> {
                Livro l = bib.cadastrarLivro(lerTexto("Título: "), lerTexto("Autor: "), lerTexto("Gênero: "),
                        lerInt("Ano: "), lerInt("Cópias: "));
                System.out.println("✔ Cadastrado: " + l);
            }
            case 4 -> imprimir(bib.listarUsuarios());
            case 5 -> {
                String nome = lerTexto("Nome: ");
                String email = lerTexto("E-mail: ");
                String senha = lerTexto("Senha: ");
                boolean isAdm = lerTexto("É administrador? (s/n): ").equalsIgnoreCase("s");
                System.out.println("✔ Cadastrado: " + bib.cadastrarUsuario(nome, email, senha, isAdm));
            }
            case 6 -> {
                int userId = usuarioLogado.isAdmin() ? lerInt("ID do usuário: ") : usuarioLogado.getId();
                int livroId = lerInt("ID do livro: ");
                Emprestimo e = bib.emprestar(livroId, userId,
                        lerDataOperacao("Data do empréstimo (dd/MM/aaaa, vazio = hoje): "));
                System.out.println("✔ " + e);
            }
            case 7 -> {
                int id = lerIdEmprestimoPermitido();
                BigDecimal multa = bib.devolver(id,
                        lerDataOperacao("Data da devolução (dd/MM/aaaa, vazio = hoje): "));
                System.out.println("✔ Devolução registrada.");
                if (multa.signum() > 0) System.out.printf("⚠ Atraso! Multa gerada: R$ %.2f%n", multa);
            }
            case 8 -> {
                int id = lerIdEmprestimoPermitido();
                bib.renovar(id, LocalDate.now());
                System.out.println("✔ Nova data de devolução: " + bib.getEmprestimo(id).getDataPrevista().format(FMT));
            }
            case 9 -> {
                if (usuarioLogado.isAdmin()) imprimir(bib.emprestimosAtivos());
                else imprimir(bib.emprestimosAtivosDe(usuarioLogado));
            }
            case 10 -> {
                LocalDate ref = lerData("Data de referência (vazio = hoje): ");
                List<Emprestimo> lista = bib.emprestimosAtrasados(ref);
                if (lista.isEmpty()) System.out.println("Nenhum empréstimo atrasado.");
                lista.forEach(e -> System.out.printf("%s | %d dia(s) de atraso | multa até agora: R$ %.2f%n",
                        e, e.diasAtraso(ref), e.calcularMulta(ref)));
            }
            case 11 -> {
                int userId = usuarioLogado.isAdmin() ? lerInt("ID do usuário: ") : usuarioLogado.getId();
                Agendamento a = bib.agendar(lerInt("ID do livro: "), userId,
                        lerData("Data de retirada (dd/MM/aaaa): "), LocalDate.now());
                System.out.println("✔ " + a);
            }
            case 12 -> imprimir(bib.agendamentosAtivos());
            case 13 -> {
                bib.cancelarAgendamento(lerInt("ID do agendamento: "));
                System.out.println("✔ Agendamento cancelado.");
            }
            case 14 -> {
                int userId = usuarioLogado.isAdmin() ? lerInt("ID do usuário: ") : usuarioLogado.getId();
                System.out.printf("✔ Multa de R$ %.2f quitada.%n", bib.pagarMulta(userId));
            }
            case 15 -> Simulador.executarSimulacao(
                    bib,
                    lerInt("Quantos usuários aleatórios gerar? "),
                    lerInt("Quantas ações aleatórias simular? ")
            );
            case 16 -> comprarLivros();
            case 17 -> mostrarBeneficios();
            case 18 -> {
                Usuario u = escolherUsuario();
                System.out.println("Recomendações para " + u.getNome() + ":");
                imprimir(Recomendador.recomendar(bib, u));
            }
            case 19 -> {
                int livroId = lerInt("ID do livro: ");
                System.out.println(bib.getLivro(livroId));
                BigDecimal preco = lerValor("Preço de venda (R$): ");
                int quantidade = lerInt("Exemplares a acrescentar (0 = só mudar o preço): ");
                BigDecimal custo = lerValor("Custo por exemplar (R$, 0 = doação/sem custo): ");
                bib.abastecerVenda(livroId, preco, quantidade, custo, LocalDate.now());
                System.out.println("✔ " + bib.getLivro(livroId));
            }
            case 20 -> menuFinanceiro();
            default -> System.out.println("Opção inválida.");
        }
    }

    // Admin escolhe qualquer usuário; leitor só opera na própria conta
    private static Usuario escolherUsuario() {
        if (usuarioLogado.isAdmin()) return bib.getUsuario(lerInt("ID do usuário: "));
        return usuarioLogado;
    }

    // Leitor só pode devolver/renovar os próprios empréstimos
    private static int lerIdEmprestimoPermitido() {
        int id = lerInt("ID do empréstimo: ");
        Emprestimo e = bib.getEmprestimo(id);
        if (!usuarioLogado.isAdmin() && !e.getUsuario().equals(usuarioLogado)) {
            throw new BibliotecaException("Acesso negado: este empréstimo é de outro usuário.");
        }
        return id;
    }

    // Só o admin informa a data (para testar multas); leitor sempre usa a data de hoje
    private static LocalDate lerDataOperacao(String msg) {
        if (usuarioLogado.isAdmin()) return lerData(msg);
        return LocalDate.now();
    }

    private static void comprarLivros() {
        Usuario comprador = escolherUsuario();

        System.out.println("\n--- Livros à venda ---");
        List<Livro> aVenda = new ArrayList<>();
        for (Livro l : bib.listarLivros()) {
            if (l.estaAVenda()) aVenda.add(l);
        }
        imprimir(aVenda);
        if (aVenda.isEmpty()) return;

        List<Integer> ids = new ArrayList<>();
        int id = lerInt("ID do livro (0 para finalizar): ");
        while (id != 0) {
            ids.add(id);
            id = lerInt("ID do próximo livro (0 para finalizar): ");
        }
        if (ids.isEmpty()) {
            System.out.println("Compra cancelada.");
            return;
        }

        System.out.println(bib.orcarCompra(comprador.getId(), ids, LocalDate.now()));
        if (!lerTexto("Confirmar compra? (s/n): ").equalsIgnoreCase("s")) {
            System.out.println("Compra cancelada.");
            return;
        }
        NivelFidelidade antes = comprador.getNivel();
        bib.comprar(comprador.getId(), ids, LocalDate.now());
        System.out.println("✔ Compra realizada!");
        if (comprador.getNivel() != antes) {
            System.out.println("★ Novo nível: " + comprador.getNivel());
        }
    }

    private static void mostrarBeneficios() {
        Usuario u = escolherUsuario();
        NivelFidelidade nivel = u.getNivel();
        System.out.println("\n--- Benefícios de " + u.getNome() + " ---");
        System.out.println("Livros comprados: " + u.getLivrosComprados());
        System.out.println("Nível atual: " + nivel);
        if (nivel.temProximo()) {
            int faltam = nivel.proximo().getLivrosNecessarios() - u.getLivrosComprados();
            System.out.println("Faltam " + faltam + " livro(s) para: " + nivel.proximo());
        } else {
            System.out.println("Você está no nível máximo.");
        }
        System.out.printf("Bônus: R$ %.2f de desconto em compras com %d livros ou mais.%n",
                Compra.VALOR_BONUS, Compra.LIVROS_PARA_BONUS);
        System.out.println("\nHistórico de compras:");
        imprimir(bib.comprasDe(u));
    }

    private static void menuFinanceiro() {
        Financeiro fin = bib.getFinanceiro();
        while (true) {
            System.out.println("\n--- FINANCEIRO ---");
            System.out.println("1  Resumo do caixa        2  Todas as movimentações");
            System.out.println("3  Filtrar por tipo       4  Filtrar por categoria");
            System.out.println("5  Filtrar por período    6  Lançar receita/despesa");
            System.out.println("0  Voltar");
            int op = lerInt("Escolha: ");
            // Erro de regra mantém o usuário dentro do menu financeiro
            try {
                switch (op) {
                    case 0 -> { return; }
                    case 1 -> {
                        Financeiro.Resumo r = fin.gerarResumo(bib.listarUsuarios());
                        System.out.printf("Entradas:          R$ %.2f%n", r.totalEntradas());
                        System.out.printf("Saídas:            R$ %.2f%n", r.totalSaidas());
                        System.out.printf("Saldo do caixa:    R$ %.2f%n", r.saldoCaixa());
                        System.out.printf("Multas a receber:  R$ %.2f%n", r.multasPendentes());
                        System.out.println("Movimentações:     " + r.quantidadeMovimentacoes());
                    }
                    case 2 -> imprimir(fin.listarMovimentacoes());
                    case 3 -> imprimir(fin.filtrarPorTipo(lerTipo()));
                    case 4 -> imprimir(fin.filtrarPorCategoria(lerTexto("Categoria (Venda, Multa, Estoque...): ")));
                    case 5 -> imprimir(fin.filtrarPorPeriodo(
                            lerData("Data inicial (dd/MM/aaaa): "),
                            lerData("Data final (dd/MM/aaaa): ")));
                    case 6 -> {
                        MovimentacaoFinanceira mov = fin.registrar(lerTipo(),
                                lerTexto("Categoria: "), lerTexto("Descrição: "), lerValor("Valor (R$): "),
                                lerData("Data (dd/MM/aaaa, vazio = hoje): "));
                        System.out.println("✔ " + mov);
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (BibliotecaException e) {
                System.out.println("⚠ " + e.getMessage());
            }
            if (!in.hasNextLine()) return;
        }
    }

    private static MovimentacaoFinanceira.Tipo lerTipo() {
        String s = lerTexto("Tipo (E = entrada, S = saída): ");
        if (s.equalsIgnoreCase("e")) return MovimentacaoFinanceira.Tipo.ENTRADA;
        if (s.equalsIgnoreCase("s")) return MovimentacaoFinanceira.Tipo.SAIDA;
        throw new BibliotecaException("Tipo inválido. Digite E ou S.");
    }

    // Aceita 10, 10,5 ou 10.50
    private static BigDecimal lerValor(String msg) {
        while (true) {
            String s = lerTexto(msg);
            if (s.matches("\\d{1,7}([.,]\\d{1,2})?")) return new BigDecimal(s.replace(",", "."));
            System.out.println("Valor inválido. Exemplo: 29,90");
            if (!in.hasNextLine()) return BigDecimal.ZERO;
        }
    }

    private static <T> void imprimir(List<T> itens) {
        if (itens.isEmpty()) System.out.println("(nenhum registro)");
        itens.forEach(System.out::println);
    }

    private static String lerTexto(String msg) {
        System.out.print(msg);
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    private static int lerInt(String msg) {
        while (true) {
            String s = lerTexto(msg);
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.println("Digite um número válido."); }
            if (!in.hasNextLine()) return 0;
        }
    }

    private static LocalDate lerData(String msg) {
        while (true) {
            String s = lerTexto(msg);
            if (s.isEmpty() && msg.contains("vazio")) return LocalDate.now();
            try { return LocalDate.parse(s, FMT); }
            catch (DateTimeParseException e) { System.out.println("Data inválida. Use dd/MM/aaaa."); }
            if (!in.hasNextLine()) return LocalDate.now();
        }
    }

    private static void carregarDadosExemplo() {
        bib.cadastrarLivro("Dom Casmurro", "Machado de Assis", "Romance", 1899, 2);
        bib.cadastrarLivro("O Cortiço", "Aluísio Azevedo", "Romance", 1890, 1);
        bib.cadastrarLivro("Capitães da Areia", "Jorge Amado", "Romance", 1937, 3);
        bib.cadastrarLivro("Clean Code", "Robert C. Martin", "Tecnologia", 2008, 2);
        bib.cadastrarLivro("Memórias Póstumas de Brás Cubas", "Machado de Assis", "Romance", 1881, 2);
        bib.cadastrarLivro("O Programador Pragmático", "Andrew Hunt", "Tecnologia", 1999, 1);

        // Estoque inicial de venda (sem custo, para o caixa começar zerado)
        String[] precos = {"29.90", "24.90", "34.90", "89.90", "32.90", "99.90"};
        for (int i = 0; i < precos.length; i++) {
            bib.abastecerVenda(i + 1, new BigDecimal(precos[i]), 5, BigDecimal.ZERO, LocalDate.now());
        }

        // Contas de exemplo para testar os dois perfis
        bib.cadastrarUsuario("Administrador", "admin@biblioteca.com", "1234", true);
        bib.cadastrarUsuario("Ana Souza", "ana@email.com", "1234", false);
        bib.cadastrarUsuario("Carlos Lima", "carlos@email.com", "1234", false);
    }
}