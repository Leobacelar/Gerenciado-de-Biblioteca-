package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulador {
    private static final String[] NOMES = {
        "Lucas", "Mariana", "Gabriel", "Beatriz", "Rafael",
        "Camila", "Matheus", "Larissa", "Pedro", "Juliana",
        "Felipe", "Amanda", "Gustavo", "Fernanda", "Bruno"
    };

    private static final String[] SOBRENOMES = {
        "Silva", "Santos", "Oliveira", "Souza", "Rodrigues",
        "Ferreira", "Alves", "Pereira", "Lima", "Gomes",
        "Costa", "Ribeiro", "Martins", "Carvalho", "Almeida"
    };

    private static final String[] DOMINIOS = {
        "gmail.com", "hotmail.com", "outlook.com", "yahoo.com.br", "estudante.edu.br"
    };

    private static final Random random = new Random();

    public static void executarSimulacao(Biblioteca bib, int qtdUsuarios, int qtdAcoes) {
        System.out.println("\n=== INICIANDO SIMULAÇÃO RANDÔMICA ===");

        // 1. Criar usuários aleatórios
        int usuariosCriados = 0;
        for (int i = 0; i < qtdUsuarios; i++) {
            String nome = NOMES[random.nextInt(NOMES.length)] + " " + SOBRENOMES[random.nextInt(SOBRENOMES.length)];
            String emailBase = nome.toLowerCase().replace(" ", ".") + (10 + random.nextInt(90));
            String email = emailBase + "@" + DOMINIOS[random.nextInt(DOMINIOS.length)];

            try {
                // Construtor completo: recusa e-mail repetido
                Usuario u = bib.cadastrarUsuario(nome, email, "1234", false);
                usuariosCriados++;
                System.out.println("[+] Usuário gerado: " + u.getNome() + " (" + u.getEmail() + ")");
            } catch (BibliotecaException e) {
                System.out.println("[!] Falha ao gerar usuário: " + e.getMessage());
            }
        }

        List<Usuario> usuarios = bib.listarUsuarios();
        List<Livro> livros = bib.listarLivros();

        if (usuarios.isEmpty() || livros.isEmpty()) {
            System.out.println("⚠ É necessário ter usuários e livros cadastrados para simular ações.");
            return;
        }

        System.out.println("\n--- Simulando " + qtdAcoes + " movimentações aleatórias ---");
        int sucessos = 0;
        int bloqueiosRegra = 0;

        // 2. Simular ações aleatórias na biblioteca
        for (int i = 1; i <= qtdAcoes; i++) {
            Usuario u = usuarios.get(random.nextInt(usuarios.size()));
            Livro l = livros.get(random.nextInt(livros.size()));
            int acao = random.nextInt(6); // 0=Emprestar, 1=Devolver, 2=Renovar, 3=Agendar, 4=Pagar Multa, 5=Comprar

            try {
                switch (acao) {
                    case 0 -> { // Emprestar (data retroativa aleatória entre hoje e 12 dias atrás para gerar atrasos)
                        LocalDate dataEmp = LocalDate.now().minusDays(random.nextInt(13));
                        Emprestimo emp = bib.emprestar(l.getId(), u.getId(), dataEmp);
                        System.out.printf("(%d) Empréstimo: %s pegou '%s' em %s%n",
                                i, u.getNome(), l.getTitulo(), dataEmp);
                        sucessos++;
                    }
                    case 1 -> { // Devolver um empréstimo ativo aleatório
                        List<Emprestimo> ativos = bib.emprestimosAtivos();
                        if (ativos.isEmpty()) continue;
                        Emprestimo emp = ativos.get(random.nextInt(ativos.size()));
                        BigDecimal multa = bib.devolver(emp.getId(), LocalDate.now());
                        System.out.printf("(%d) Devolução: %s devolveu '%s' | Multa: R$ %.2f%n",
                                i, emp.getUsuario().getNome(), emp.getLivro().getTitulo(), multa);
                        sucessos++;
                    }
                    case 2 -> { // Renovar um empréstimo ativo
                        List<Emprestimo> ativos = bib.emprestimosAtivos();
                        if (ativos.isEmpty()) continue;
                        Emprestimo emp = ativos.get(random.nextInt(ativos.size()));
                        bib.renovar(emp.getId(), LocalDate.now());
                        System.out.printf("(%d) Renovação: %s renovou '%s'%n",
                                i, emp.getUsuario().getNome(), emp.getLivro().getTitulo());
                        sucessos++;
                    }
                    case 3 -> { // Agendar livro para daqui a 1 a 5 dias
                        LocalDate retirada = LocalDate.now().plusDays(1 + random.nextInt(5));
                        bib.agendar(l.getId(), u.getId(), retirada, LocalDate.now());
                        System.out.printf("(%d) Agendamento: %s agendou '%s' para %s%n",
                                i, u.getNome(), l.getTitulo(), retirada);
                        sucessos++;
                    }
                    case 4 -> { // Quitar multa pendente
                        BigDecimal pago = bib.pagarMulta(u.getId());
                        System.out.printf("(%d) Pagamento: %s quitou multa de R$ %.2f%n",
                                i, u.getNome(), pago);
                        sucessos++;
                    }
                    case 5 -> { // Comprar de 1 a 3 livros
                        List<Integer> ids = new ArrayList<>();
                        int quantidade = 1 + random.nextInt(3);
                        for (int j = 0; j < quantidade; j++) {
                            ids.add(livros.get(random.nextInt(livros.size())).getId());
                        }
                        Compra c = bib.comprar(u.getId(), ids, LocalDate.now());
                        System.out.printf("(%d) Compra: %s comprou %d livro(s) por R$ %.2f%n",
                                i, u.getNome(), ids.size(), c.getTotal());
                        sucessos++;
                    }
                }
            } catch (BibliotecaException e) {
                // Regras da biblioteca funcionando (ex: limite de 3 livros, usuário com multa, sem cópias)
                bloqueiosRegra++;
                System.out.printf("(%d) Bloqueado pela regra [%s]: %s%n", i, u.getNome(), e.getMessage());
            }
        }

        System.out.println("\n=== RESUMO DA SIMULAÇÃO ===");
        System.out.println("Novos usuários cadastrados: " + usuariosCriados);
        System.out.println("Operações concluídas com sucesso: " + sucessos);
        System.out.println("Operações barradas pelas regras da biblioteca: " + bloqueiosRegra);
        System.out.printf("Saldo do caixa: R$ %.2f%n", bib.getFinanceiro().saldoCaixa());
    }
}