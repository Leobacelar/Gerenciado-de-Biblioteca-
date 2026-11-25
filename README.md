# Sistema de Biblioteca (Java)

Aplicação de console em Java 17+ (sem dependências externas).

## Como executar
```
cd biblioteca
javac -d out src/biblioteca/*.java
java -cp out biblioteca.Main
```

## Regras
- Prazo de empréstimo: 7 dias; 1 renovação (não vale se atrasado ou com agendamento na fila)
- Multa: R$ 1,50 por dia de atraso, somada ao usuário na devolução
- Usuário com multa pendente não pode pegar novos livros até pagar
- Máximo de 3 empréstimos simultâneos por usuário
- Agendamento reserva uma cópia para quem agendou
- Na devolução e no empréstimo dá para informar uma data (útil para testar multas)
