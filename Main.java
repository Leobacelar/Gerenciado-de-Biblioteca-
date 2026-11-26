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

    public static void main(String[] args) {
        carregarDadosExemplo();
        System.out.println("=== Sistema de Biblioteca ===");
        System.out.printf("Prazo: %d dias | Multa: R$ %.2f/dia | Máx. %d empréstimos%n",
                Emprestimo.PRAZO_DIAS, Emprestimo.MULTA_POR_DIA, Biblioteca.MAX_EMPRESTIMOS_POR_USUARIO);

        int op;
        do {
            menu();
            op = lerInt("Escolha: ");
            try {
                executar(op);
            } catch (BibliotecaException e) {
                System.out.println("⚠ " + e.getMessage());
            }
        } while (op != 0);
        System.out.println("Até logo!");
    }

    private static void menu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1  Listar catálogo        2  Buscar livro        3  Cadastrar livro");
        System.out.println("4  Listar usuários        5  Cadastrar usuário");
        System.out.println("6  Emprestar livro        7  Devolver livro      8  Renovar empréstimo");
        System.out.println("9  Empréstimos ativos     10 Empréstimos atrasados");
        System.out.println("11 Agendar livro          12 Agendamentos ativos 13 Cancelar agendamento");
        System.out.println("14 Pagar multa            15 Simular usuários     0  Sair");
    }

    private static void executar(int op) {
        switch (op) {
            case 1 -> imprimir(bib.listarLivros());
            case 2 -> imprimir(bib.buscarLivros(lerTexto("Termo (título/autor/gênero): ")));
            case 3 -> {
                Livro l = bib.cadastrarLivro(lerTexto("Título: "), lerTexto("Autor: "), lerTexto("Gênero: "),
                        lerInt("Ano: "), lerInt("Cópias: "));
                System.out.println("✔ Cadastrado: " + l);
            }
            case 4 -> imprimir(bib.listarUsuarios());
            case 5 -> System.out.println("✔ Cadastrado: " + bib.cadastrarUsuario(lerTexto("Nome: "), lerTexto("E-mail: ")));
            case 6 -> {
                Emprestimo e = bib.emprestar(lerInt("ID do livro: "), lerInt("ID do usuário: "), lerData("Data do empréstimo (dd/MM/aaaa, vazio = hoje): "));
                System.out.println("✔ " + e);
            }
            case 7 -> {
                BigDecimal multa = bib.devolver(lerInt("ID do empréstimo: "), lerData("Data da devolução (dd/MM/aaaa, vazio = hoje): "));
                System.out.println("✔ Devolução registrada.");
                if (multa.signum() > 0) System.out.printf("⚠ Atraso! Multa gerada: R$ %.2f%n", multa);
            }
            case 8 -> {
                int id = lerInt("ID do empréstimo: ");
                bib.renovar(id, LocalDate.now());
                System.out.println("✔ Nova data de devolução: " + bib.getEmprestimo(id).getDataPrevista().format(FMT));
            }
            case 9 -> imprimir(bib.emprestimosAtivos());
            case 10 -> {
                LocalDate ref = lerData("Data de referência (vazio = hoje): ");
                List<Emprestimo> lista = bib.emprestimosAtrasados(ref);
                if (lista.isEmpty()) System.out.println("Nenhum empréstimo atrasado.");
                lista.forEach(e -> System.out.printf("%s | %d dia(s) de atraso | multa até agora: R$ %.2f%n",
                        e, e.diasAtraso(ref), e.calcularMulta(ref)));
            }
            case 11 -> {
                Agendamento a = bib.agendar(lerInt("ID do livro: "), lerInt("ID do usuário: "),
                        lerData("Data de retirada (dd/MM/aaaa): "), LocalDate.now());
                System.out.println("✔ " + a);
            }
            case 12 -> imprimir(bib.agendamentosAtivos());
            case 13 -> { bib.cancelarAgendamento(lerInt("ID do agendamento: ")); System.out.println("✔ Agendamento cancelado."); }
            case 14 ->
