package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Biblioteca {
    public static final int MAX_EMPRESTIMOS_POR_USUARIO = 3;

    private final Map<Integer, Livro> livros = new LinkedHashMap<>();
    private final Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<Integer, Emprestimo> emprestimos = new LinkedHashMap<>();
    private final Map<Integer, Agendamento> agendamentos = new LinkedHashMap<>();
    private final Map<Integer, Compra> compras = new LinkedHashMap<>();
    private final Financeiro financeiro = new Financeiro();
    private int proxLivro = 1, proxUsuario = 1, proxEmprestimo = 1, proxAgendamento = 1, proxCompra = 1;

  
    public Livro cadastrarLivro(String titulo, String autor, String genero, int ano, int copias) {
        if (titulo.isBlank() || autor.isBlank()) throw new BibliotecaException("Título e autor são obrigatórios.");
        if (copias < 1) throw new BibliotecaException("A quantidade de cópias deve ser pelo menos 1.");
        Livro l = new Livro(proxLivro++, titulo, autor, genero, ano, copias);
        livros.put(l.getId(), l);
        return l;
    }

    public List<Livro> listarLivros() { return new ArrayList<>(livros.values()); }

    public List<Livro> buscarLivros(String termo) {
        return livros.values().stream().filter(l -> l.contem(termo)).collect(Collectors.toList());
    }

    public Livro getLivro(int id) {
        Livro l = livros.get(id);
        if (l == null) throw new BibliotecaException("Livro " + id + " não encontrado.");
        return l;
    }

    
    public Usuario cadastrarUsuario(String nome, String email) {
        if (nome.isBlank()) throw new BibliotecaException("O nome é obrigatório.");
        Usuario u = new Usuario(proxUsuario++, nome, email);
        usuarios.put(u.getId(), u);
        return u;
    }

    public Usuario cadastrarUsuario(String nome, String email, String senha, boolean admin) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank())
            throw new BibliotecaException("Nome, e-mail e senha são obrigatórios.");
        boolean emailExiste = usuarios.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (emailExiste)
            throw new BibliotecaException("Já existe um usuário cadastrado com este e-mail.");
        Usuario u = new Usuario(proxUsuario++, nome, email, senha, admin);
        usuarios.put(u.getId(), u);
        return u;
    }

    public Usuario autenticar(String email, String senha) {
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.validarSenha(senha))
                .findFirst()
                .orElseThrow(() -> new BibliotecaException("E-mail ou senha inválidos."));
    }
    public List<Usuario> listarUsuarios() { return new ArrayList<>(usuarios.values()); }

    public Usuario getUsuario(int id) {
        Usuario u = usuarios.get(id);
        if (u == null) throw new BibliotecaException("Usuário " + id + " não encontrado.");
        return u;
    }

  
    public Emprestimo emprestar(int livroId, int usuarioId, LocalDate hoje) {
        Livro livro = getLivro(livroId);
        Usuario usuario = getUsuario(usuarioId);

        if (usuario.temMulta())
            throw new BibliotecaException(String.format(
                    "Usuário possui multa pendente de R$ %.2f. Quite antes de pegar novos livros.", usuario.getMultaPendente()));
        int limite = usuario.getNivel().getLimiteEmprestimos();
        if (emprestimosAtivosDe(usuario).size() >= limite)
            throw new BibliotecaException("Limite de " + limite + " empréstimos simultâneos atingido.");
        if (emprestimosAtivosDe(usuario).stream().anyMatch(e -> e.getLivro().equals(livro)))
            throw new BibliotecaException("O usuário já está com um exemplar deste livro.");

        Agendamento reservaDoUsuario = agendamentoAtivo(livro, usuario);
        long reservasDeOutros = agendamentos.values().stream()
                .filter(a -> a.estaAtivo() && a.getLivro().equals(livro) && !a.getUsuario().equals(usuario)).count();

        // Cópias disponíveis ficam guardadas para quem agendou antes.
        if (reservaDoUsuario == null && livro.getDisponiveis() - reservasDeOutros <= 0)
            throw new BibliotecaException("Sem cópias disponíveis (ou reservadas para outros). Faça um agendamento.");
        if (livro.getDisponiveis() <= 0)
            throw new BibliotecaException("Nenhuma cópia disponível no momento.");

        if (reservaDoUsuario != null) reservaDoUsuario.atender();
        livro.retirarCopia();
        Emprestimo e = new Emprestimo(proxEmprestimo++, livro, usuario, hoje);
        emprestimos.put(e.getId(), e);
        return e;
    }

    
    public BigDecimal devolver(int emprestimoId, LocalDate dataDevolucao) {
        Emprestimo e = getEmprestimo(emprestimoId);
        if (!e.estaAtivo()) throw new BibliotecaException("Este empréstimo já foi devolvido.");
        if (dataDevolucao.isBefore(e.getDataEmprestimo()))
            throw new BibliotecaException("A devolução não pode ser antes do empréstimo.");

        e.registrarDevolucao(dataDevolucao);
        e.getLivro().devolverCopia();
        BigDecimal multa = e.calcularMulta(dataDevolucao);
        if (multa.signum() > 0) e.getUsuario().adicionarMulta(multa);
        return multa;
    }

    public void renovar(int emprestimoId, LocalDate hoje) {
        Emprestimo e = getEmprestimo(emprestimoId);
        if (!e.estaAtivo()) throw new BibliotecaException("Este empréstimo já foi devolvido.");
        if (hoje.isAfter(e.getDataPrevista())) throw new BibliotecaException("Empréstimo em atraso não pode ser renovado.");
        if (e.getRenovacoes() >= Emprestimo.MAX_RENOVACOES) throw new BibliotecaException("Limite de renovações atingido.");
        boolean haFila = agendamentos.values().stream()
                .anyMatch(a -> a.estaAtivo() && a.getLivro().equals(e.getLivro()));
        if (haFila) throw new BibliotecaException("Há agendamentos para este livro; não é possível renovar.");
        e.renovar();
    }

    public Emprestimo getEmprestimo(int id) {
        Emprestimo e = emprestimos.get(id);
        if (e == null) throw new BibliotecaException("Empréstimo " + id + " não encontrado.");
        return e;
    }

    public List<Emprestimo> listarEmprestimos() { return new ArrayList<>(emprestimos.values()); }

    public List<Emprestimo> emprestimosAtivos() {
        return emprestimos.values().stream().filter(Emprestimo::estaAtivo).collect(Collectors.toList());
    }

    public List<Emprestimo> emprestimosAtivosDe(Usuario u) {
        return emprestimosAtivos().stream().filter(e -> e.getUsuario().equals(u)).collect(Collectors.toList());
    }

    public List<Emprestimo> emprestimosAtrasados(LocalDate hoje) {
        return emprestimosAtivos().stream().filter(e -> e.diasAtraso(hoje) > 0).collect(Collectors.toList());
    }

    
    public Agendamento agendar(int livroId, int usuarioId, LocalDate dataRetirada, LocalDate hoje) {
        Livro livro = getLivro(livroId);
        Usuario usuario = getUsuario(usuarioId);
        if (dataRetirada.isBefore(hoje)) throw new BibliotecaException("A data de retirada não pode estar no passado.");
        if (agendamentoAtivo(livro, usuario) != null)
            throw new BibliotecaException("O usuário já tem um agendamento ativo para este livro.");
        Agendamento a = new Agendamento(proxAgendamento++, livro, usuario, dataRetirada);
        agendamentos.put(a.getId(), a);
        return a;
    }

    public void cancelarAgendamento(int id) {
        Agendamento a = agendamentos.get(id);
        if (a == null) throw new BibliotecaException("Agendamento " + id + " não encontrado.");
        if (!a.estaAtivo()) throw new BibliotecaException("Este agendamento não está ativo.");
        a.cancelar();
    }

    public List<Agendamento> agendamentosAtivos() {
        return agendamentos.values().stream().filter(Agendamento::estaAtivo).collect(Collectors.toList());
    }

    private Agendamento agendamentoAtivo(Livro l, Usuario u) {
        return agendamentos.values().stream()
                .filter(a -> a.estaAtivo() && a.getLivro().equals(l) && a.getUsuario().equals(u))
                .findFirst().orElse(null);
    }

  
    public BigDecimal pagarMulta(int usuarioId) {
        Usuario u = getUsuario(usuarioId);
        if (!u.temMulta()) throw new BibliotecaException("Este usuário não tem multas pendentes.");
        BigDecimal valor = u.getMultaPendente();
        financeiro.registrarPagamentoMulta(u, valor, LocalDate.now());
        u.quitarMulta();
        return valor;
    }

    public Financeiro getFinanceiro() { return financeiro; }

    // Define o preço de venda e acrescenta exemplares. Se houver custo, lança a saída no caixa.
    public void abastecerVenda(int livroId, BigDecimal preco, int quantidade, BigDecimal custoUnitario, LocalDate data) {
        Livro livro = getLivro(livroId);
        if (preco == null || preco.signum() <= 0) throw new BibliotecaException("O preço de venda deve ser maior que zero.");
        if (quantidade < 0) throw new BibliotecaException("A quantidade não pode ser negativa.");
        if (custoUnitario == null || custoUnitario.signum() < 0)
            throw new BibliotecaException("O custo unitário não pode ser negativo.");

        BigDecimal custoTotal = custoUnitario.multiply(BigDecimal.valueOf(quantidade));
        if (custoTotal.signum() > 0) {
            financeiro.registrar(MovimentacaoFinanceira.Tipo.SAIDA, "Estoque",
                    String.format("Reposição de %d un. de %s", quantidade, livro.getTitulo()), custoTotal, data);
        }
        livro.abastecerVenda(preco, quantidade);
    }

    // Calcula a compra sem efetivar (para o usuário conferir o total antes de confirmar)
    public Compra orcarCompra(int usuarioId, List<Integer> livroIds, LocalDate data) {
        Usuario usuario = getUsuario(usuarioId);
        if (livroIds.isEmpty()) throw new BibliotecaException("Informe pelo menos um livro para comprar.");

        List<Livro> itens = new ArrayList<>();
        Map<Livro, Integer> quantidades = new HashMap<>();
        for (int livroId : livroIds) {
            Livro livro = getLivro(livroId);
            int quantidade = quantidades.getOrDefault(livro, 0) + 1;
            if (!livro.estaAVenda() || quantidade > livro.getEstoqueVenda())
                throw new BibliotecaException("Sem estoque para venda: " + livro.getTitulo() + ".");
            quantidades.put(livro, quantidade);
            itens.add(livro);
        }
        return new Compra(proxCompra, usuario, itens, data);
    }

    public Compra comprar(int usuarioId, List<Integer> livroIds, LocalDate data) {
        // orcarCompra valida tudo antes de alterar qualquer dado
        Compra compra = orcarCompra(usuarioId, livroIds, data);
        // O caixa não aceita lançamento de valor zero (descontos podem zerar a compra)
        if (compra.getTotal().signum() > 0) {
            financeiro.registrar(MovimentacaoFinanceira.Tipo.ENTRADA, "Venda",
                    String.format("Compra %d - %s", compra.getId(), compra.getUsuario().getNome()),
                    compra.getTotal(), data);
        }
        for (Livro livro : compra.getLivros()) {
            livro.venderExemplar();
        }
        compra.getUsuario().registrarCompra(compra.getLivros().size());
        compras.put(compra.getId(), compra);
        proxCompra++;
        return compra;
    }

    public List<Compra> listarCompras() { return new ArrayList<>(compras.values()); }

    public List<Compra> comprasDe(Usuario u) {
        List<Compra> doUsuario = new ArrayList<>();
        for (Compra c : compras.values()) {
            if (c.getUsuario().equals(u)) doUsuario.add(c);
        }
        return doUsuario;
    }
}
