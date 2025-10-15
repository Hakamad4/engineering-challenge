# Desafio Técnico — Motor de Pagamentos e Extratos Imobiliários

Essa é a etapa técnica do processo seletivo da Morus.
O objetivo é avaliar as suas habilidades de modelagem, design, implementação e testes.
Não se preocupe em entregar tudo completo, mas sim em mostrar o seu raciocínio e qualidade de código.

Esta é uma oportunidade para mostrar as suas habilidades técnicas e jeito de pensar.
Sinta-se à vontade para fazer perguntas, implementar melhorias e ser criativo.

Divirta-se! :)

## Contexto

A Morus processa pagamentos de aluguéis e taxas imobiliárias. Quando um pagamento é recebido, ele deve ser **distribuído
automaticamente** entre:

- **Morus** (taxa de plataforma),
- **Imobiliária (RealEstateAgency)**,
- **Proprietário do imóvel (PropertyOwner)**.

O objetivo é implementar o módulo que **processa pagamentos** e gera **lançamentos contábeis (Statements)** com
**contraprova (double-entry)**, garantindo integridade e possibilidade de processamento concorrente.
Caso tenha dúvidas de como funciona a mecanica contábil, pesquise sobre "double-entry accounting".

## O que deve ser desenvolvido

1. Receber um pagamento (valor total).
2. Calcular o split entre Morus, Imobiliária e Owner.
3. Gerar lançamentos contábeis de **débito e crédito** (com contrapartida).
4. Atualizar saldos de contas.
5. Garantir que o balanço esteja **zerado** (soma de débitos = créditos).
6. Permitir processamento simultâneo de múltiplos pagamentos.

## Exemplo contábil completo

Pagamento: **R$ 3.000,00** (Imobiliária 10%, Morus 2%, Owner 88%)

| Nº | Descrição                    | Conta Crédito      | Valor    | Descrição               |
|----|------------------------------|--------------------|----------|-------------------------|
| 1  | Origem externa               | Morus Recebimentos | 3.000,00 | Pagamento recebido      |
| 2  | Repasse de valores recebidos | Imobiliária        | 300,00   | Comissão imobiliária    |
| 3  | Repasse de valores recebidos | Proprietário       | 2.640,00 | Repasse ao proprietário |
| 4  | Repasse de valores recebidos | Morus Receita      | 60,00    | Taxa de plataforma      |

### No extrato, a mecanica deve ser da seguinte forma:

| Data       | Descrição               | Conta              | Valor     |
|------------|-------------------------|--------------------|-----------|
| 2024-01-01 | Pagamento recebido      | Morus Recebimentos | +3.000,00 |
| 2024-01-01 | Comissão imobiliária    | Morus Recebimentos | -300,00   |
| 2024-01-01 | Comissão imobiliária    | Imobiliária        | +300,00   |
| 2024-01-01 | Repasse ao proprietário | Morus Recebimentos | -2.640,00 |
| 2024-01-01 | Repasse ao proprietário | Proprietário       | +2.640,00 |
| 2024-01-01 | Taxa de plataforma      | Morus Recebimentos | -60,00    |
| 2024-01-01 | Taxa de plataforma      | Morus Receita      | +60,00    |

Sendo assim, o saldo da conta `Morus Recebimentos` volta a ser zero após o processamento do pagamento.

### Resumo do exemplo

- **Pagamento**: R$ 3.000,00
- **Split**:
    - Morus: 2% = R$ 60,00
    - Imobiliária: 10% = R$ 300,00
    - Proprietário: 88% = R$ 2.640,00
- **Lançamentos**: 7 (1 débito inicial + 3 créditos + 3 débitos de contrapartida)
- Caso o valor do pagamento seja negativo ou zero, o pagamento deve ser rejeitado com erro.

**Invariantes**: Total Débitos = Total Créditos = 3.000,00. Qualquer falha deve gerar rollback e `Payment = FAILED`.

## Entidades sugeridas

#### (Modelagem previamente definida, mas pode ser ajustada caso você ache necessário)

- `Account(id, name, balance, type[CLEARING_ACCOUNT, PLATFORM_REVENUE_ACCOUNT, REAL_ESTATE_AGENCY, PROPERTY_OWNER])`
- `RealEstateAgency(id, name, accountId, feePercentage)`
- `Owner(id, name, accountId)`
- `Payment(id, referenceId, amount, status[PENDING, COMPLETED, FAILED], createdAt)`
- `Statement(id, paymentId, accountId, counterpartyAccountId, type[DEBIT,CREDIT], amount, description, createdAt)`

## Regras de Split

- Taxa da MorusBank: 2% (fixo sobre o valor total)
- Taxa da Imobiliária: definida pelo campo `feePercentage`
- Repasse ao Proprietário: valor líquido restante

## Desafios técnicos

1. **Modelagem contábil coerente** (double-entry com contraprova).
2. **Processamento de pagamento** (cálculo de splits e geração de lançamentos).
3. **Balanço contábil** (débitos = créditos).
4. **Rollback em falhas** (pagamento `FAILED` e nenhum lançamento parcial).
5. **Testes automatizados** (cálculo, balanço e rollback).

## Desafios bônus

1. **Idempotência**: evitar reprocessamento do mesmo pagamento.
2. **Racing conditions**: garantir que atualizações concorrentes de saldo sejam seguras.
3. **Arredondamento**: distribuir centavos excedentes de forma justa.
4. **Logs e auditoria**: registrar operações e saldo final por conta.
5. **Performance em lote**: processar vários pagamentos mantendo integridade.
6. **Validação de ledger**: `validateLedger()` que prova Débitos = Créditos global.

## Tecnologias sugeridas

- Java 21 ou superior, Spring Boot 3.x, Maven, H2 e JUnit 5.
- É permitido o uso de bibliotecas adicionais, mas evite bibliotecas que abstraiam a lógica principal (ex: frameworks
  contábeis).
- Caso queira, use outras tecnologias ou outra linguagem, mas justifique as suas motivações no `README.md` do seu
  projeto.

## Entrega

- Crie um .zip e envie o seu código para o e-mail [contato@morusbank.com.br](mailto:contato@morusbank.com.br) (sem
  arquivos binários, somente o código-fonte).
- `README.md` descrevendo arquitetura e decisões.

## Informações importantes

- Prazo: 7 dias a partir do recebimento.
- Em caso de dúvidas, não hesite em perguntar através do e-mail contato@morusbank.com.br
- Seja criativo e mostre as suas habilidades. Caso não consiga completar tudo, não tem problema. Queremos ver o seu
  raciocínio e qualidade do código, não necessariamente tudo implementado. :)
- O uso de IA é permitido e incentivado, porém, lembre-se de que o código é seu e você deve entender claramente o que
  está a ser feito.

**Boa sorte!**

>
> Any fool can write code that a computer can understand. Good programmers write code that humans can understand.
> – Martin Fowler
>
