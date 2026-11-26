package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Scanner in = new Scanner(System.in);
    private static final Biblioteca bib = new Biblioteca();
    private static Usuario usuarioLogado = null;

    public static void main(String[] args) {
        carregarDadosExemplo();
        System.out.println("=== Sistema de Biblioteca ===");
        System.out.printf("Prazo: %d dias | Multa: R$ %.2f/dia | Máx. %d empréstimos%n",
                Emprestimo.PRAZO_DIAS, Emprestimo.MULTA_POR_DIA, Biblioteca.MAX_EMPRESTIMOS_POR_USUARIO);

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
            System.out.println("14 Pagar multa            15 Simular usuários     0  Trocar usuário (Logout)");
        } else {
            System.out.println("1  Listar catálogo        2  Buscar livro");
            System.out.println("6  Pegar livro emprestado 7  Devolver livro      8  Renovar meu empréstimo");
            System.out.println("9  Meus empréstimos       11 Agendar livro       14 Pagar minha multa");
            System.out.println("0  Trocar usuário (Logout)");
        }
    }

    private static void executar(int op) {
        if (op == 0) {
            System.out.println("Desconectando " + usuarioLogado.getNome() + "...");
            usuarioLogado = null;
            return;
        }

        if (!usuarioLogado.isAdmin() && (op == 3 || op == 4 || op == 5 || op == 10 || op == 12 || op == 13 || op == 15)) {
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
                Emprestimo e = bib.emprestar(lerInt("ID do livro: "), userId,
                        lerData("Data do empréstimo (dd/MM/aaaa, vazio = hoje): "));
                System.out.println("✔ " + e);
            }
            case 7 -> {
                BigDecimal multa = bib.devolver(lerInt("ID do empréstimo: "),
                        lerData("Data da devolução (dd/MM/aaaa, vazio = hoje): "));
                System.out.println("✔ Devolução registrada.");
                if (multa.signum() > 0) System.out.printf("⚠ Atraso! Multa gerada: R$ %.2f%n", multa);
            }
            case 8 -> {
                int id = lerInt("ID do empréstimo: ");
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
            default -> System.out.println("Opção inválida.");
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

        // Contas de exemplo para testar os dois perfis
        bib.cadastrarUsuario("Administrador", "admin@biblioteca.com", "1234", true);
        bib.cadastrarUsuario("Ana Souza", "ana@email.com", "1234", false);
        bib.cadastrarUsuario("Carlos Lima", "carlos@email.com", "1234", false);
    }
}