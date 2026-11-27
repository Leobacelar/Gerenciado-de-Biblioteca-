# Sistema de Biblioteca (Java)

Aplicação de console em Java 17+ (sem dependências externas).

## Como executar
```
# na raiz do repositório
javac -encoding UTF-8 -d out *.java
java -cp out biblioteca.Main
```

## Regras
- Prazo de empréstimo: 7 dias; 1 renovação (não vale se atrasado ou com agendamento na fila)
- Multa: R$ 1,50 por dia de atraso, somada ao usuário na devolução
- Usuário com multa pendente não pode pegar novos livros até pagar
- Máximo de empréstimos simultâneos conforme o nível de fidelidade (3, 4 ou 5)
- Agendamento reserva uma cópia para quem agendou
- Na devolução e no empréstimo o administrador pode informar uma data (útil para testar multas); o leitor usa sempre a data de hoje
- Leitor só devolve e renova os próprios empréstimos

## Contas de exemplo
| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | admin@biblioteca.com | 1234 |
| Leitor | ana@email.com | 1234 |
| Leitor | carlos@email.com | 1234 |

## Compras e benefícios
O estoque de venda é separado das cópias de empréstimo.

| Nível | Livros comprados | Desconto | Empréstimos simultâneos |
|---|---|---|---|
| Bronze | 0 | 0% | 3 |
| Prata | 3 | 5% | 4 |
| Ouro | 6 | 10% | 5 |

- Compras com 3 livros ou mais ganham R$ 10,00 de desconto
- O desconto usa o nível que o usuário tinha antes da compra

## Recomendações
Até 3 livros que o usuário ainda não pegou nem comprou: mesmo autor vale 3 pontos e mesmo gênero vale 2, por livro do histórico. Empate é decidido pela popularidade e depois pelo título.

## Financeiro (administrador)
Vendas e multas pagas entram no caixa automaticamente; reposição de estoque com custo sai do caixa. Há resumo, filtros e lançamento manual.

## Limitações
Os dados ficam só em memória e são perdidos ao encerrar o programa. As senhas ficam em texto puro.
