# 💸 Morus Engineering Challenge — Payments & Statements API

Bem-vindo ao desafio técnico da Morus!  
Aqui você vai implementar a lógica central de **processamento de pagamentos e repasses automáticos** de uma plataforma
que conecta **imobiliárias, proprietários e locatários**.

O desafio mede a sua capacidade de projetar um sistema **consistente, transacional e idempotente**, capaz de lidar com
múltiplos participantes recebendo partes de um mesmo pagamento.

---
## 🔍 Visão Geral do Projeto
Para esse projeto optei trocar para o mysql, só por motivos de familiaridade.
Eu tentei deixar o mais simples possivel o projeto, sem gerar muita complexidade mas deixando o codigo mais claro possivel também.
Por isso para a atomicidade, eu usei apenas o @Transacional, mas em um caso real, eu poderia usar o Kafta para receber
os statements e processar os pagamentos, assim poderia criar uma deadletter queue para receber os pagamentos com falha
e processar depois ou realizar estorno.

A api deveria estar autenticada e autorizada, para isso eu teria usado o spring security e o JWT,
assim gerando um contexto do usuario que está realizando o pagamento e a partir disso eu pegaria
as informações do propertyOwner e realEstateAgency do contexto ao inves de receber da api.
Acredito que o pagamento seja só uma ponta da empresa, então creio que teriamos um Api Gateway para
fazer o roteamento dos serviços. (A não ser que a estrutura seja algo como um monolito, o que apesar de muitos julgarem,
acho que é uma forte solução para empresas que estão no inicio).

Usei a aquitetura em camadas, pois acho que é uma arquitetura mais padrão.

E por ultimo, no build, eu usaria o native build, para redução do cold start, assim poderia ser utilizado um lambda
ou até mesmo um container que não precisasse ficar rodando 24h.

---

## 🧩 Contexto

A Morus atua como uma fintech do setor imobiliário e uma de suas funcionalidades é receber pagamentos de aluguel
feitos por locatários e repassar automaticamente os valores para os proprietários e imobiliárias.
Quando um locatário paga um aluguel, esse valor precisa ser **recebido pela plataforma** e **repassado automaticamente**
para os participantes envolvidos na operação em uma conta digital da Morus.

- **Imobiliária** – recebe uma taxa de administração (por exemplo, 10%)
- **Proprietário do imóvel (Owner)** – recebe o valor líquido do aluguel
- **Morus (plataforma)** – retém uma pequena taxa de serviço (por exemplo, 2%)

Sua missão é construir a API que processa esse pagamento e registra os **lançamentos (Statements)** que representam o
repasse entre contas internas.

---

## 🏗️ Base do Projeto

O projeto inicial já contém:

- `Account`: representa uma conta digital (com `id`, `name`, `type`, `balance`, etc.);
    - Cada conta tem um `AccountType` representando o tipo de participante:
        - `PLATFORM_REVENUE` (conta da Morus)
        - `REAL_ESTATE_AGENCY` (conta da imobiliária)
        - `PROPERTY_OWNER` (conta do proprietário)
- `Payment`: representa o pagamento recebido;
- `PropertyOwner`: representa o proprietário do imóvel;
- `RealEstateAgency`: representa a imobiliária;
- `Statement`: representa um lançamento no extrato de uma conta;

Você pode criar novas classes, DTOs, atributos, eventos ou estratégias conforme achar necessário, desde que mantenha a
coesão e clareza do projeto.
Não se limite ao código inicial — sinta-se livre para refatorar e melhorar a estrutura do projeto caso julgue
necessário.

---

## 🚀 Desafio

Implementar um endpoint `/payments` que recebe um pagamento e gera os lançamentos (Statements) necessários para
distribuir o valor entre os participantes, garantindo:

- **Idempotência**: o mesmo pagamento não pode ser processado mais de uma vez.
- **Atomicidade**: se qualquer parte do processo falhar, todo o pagamento deve ser revertido.
- **Integridade**: A soma de todos os lançamentos deve ser igual ao valor do pagamento.
- **Rastreamento**: Cada lançamento no extrato deve referenciar o pagamento original para auditoria.

### Regras de negócio

1. A plataforma Morus **recebe o pagamento** de R$ 3.000,00.
2. O sistema deve **distribuir automaticamente** o valor entre as contas dos participantes:
    - Exemplo: para um pagamento de R$ 3.000,00:
        - **10%** para a conta da Imobiliária. (Esse percentual está definido no cadastro da
          imobiliária);
        - **2%** para a conta da Morus. (Essa taxa é fixa para todos os pagamentos);
        - **88%** para a conta do Proprietário do imóvel;
3. Cada repasse deve ser registrado no extrato (Statement) com a identificação do pagamento recebido.
4. O conjunto de lançamentos deve manter **integridade total** (A soma de todos os lançamentos deve ser igual ao valor
   do pagamento).
5. O mesmo pagamento não pode ser processados duas vezes (**idempotência obrigatória**).
6. A operação deve ser **atômica** — se um repasse falhar, todo o pagamento é revertido.

---

### 💰 Exemplo de comportamento esperado

**Requisição**

```bash
POST /payments
{
  "externalReference": "PAY-202501",
  "amount": 3000.00,
  "realEstateAgencyId": "IMB-001",
  "propertyOwnerId": "P-001",
  "description": "Pagamento de aluguel de janeiro"
}
```

**Resposta**

```bash
HTTP 201 Created
{
  "externalReference": "PAY-202501",
  "amount": 3000.00,
  "realEstateAgencyId": "IMB-001",
  "propertyOwnerId": "P-001",
  "description": "Pagamento de aluguel de janeiro",
  "createdAt": "2025-01-15T10:32:11"
}
```

**Representação simbólica dos lançamentos gerados**

| payment    | account          |  amount | description                          | createdAt           |
|------------|------------------|--------:|--------------------------------------|---------------------|
| PAY-202501 | IMB-001          |  300.00 | Recebimento de taxa de administração | 2025-01-15T10:32:11 |
| PAY-202501 | P-001            | 2640.00 | Repasse de aluguel                   | 2025-01-15T10:32:11 |
| PAY-202501 | PLATFORM_REVENUE |   60.00 | Receita da plataforma                | 2025-01-15T10:32:11 |

Ao final do processo, o extrato de cada conta deve refletir os lançamentos e os seus respectivos saldos atualizados.

---

## ⚙️ Requisitos obrigatórios

1. Implementar o endpoint `/payments` com a lógica de distribuição e geração dos lançamentos.
2. Criar `Statement` para todos os repasses e taxas.
3. Garantir **idempotência** (mesmo pagamento não gera lançamentos duplicados).
4. Garantir **atomicidade** (rollback total em caso de falha).
5. Implementar testes automatizados com no mínimo 85% de cobertura (casos de sucesso e falha).
6. Explicar decisões técnicas no `README.md`.

---

## 🧠 Requisitos bônus (não obrigatórios, porém, se implementados, serão considerados na avaliação)

- **Endpoint `/statements/{accountId}`:** retornar o extrato completo de uma conta.
- **Criação de um endpoint para simular saques:** permitir que uma conta faça o cashout de um valor, gerando um
  lançamento negativo e refletindo no saldo da conta.
- **Validações adicionais:** verificar se as contas existem, se o valor é positivo, se o proprietário está vinculado à
  imobiliária, etc.
- **Tratamento de erros:** respostas claras e apropriadas para falhas.
- **Testes de integração:** além dos testes unitários.
- **Paginação e filtros** nos extratos.
- **Documentação da API:** usando Swagger/OpenAPI.
- **Autenticação e autorização:** proteger os endpoints.
- **Métricas:** expor métricas de pagamentos processados, falhas, etc
- **Processamento assíncronos:** Processar pagamentos de forma assíncrona.

---

## 🧪 Testes

Crie testes cobrindo:

- Pagamento processado com sucesso;
- Tentativa de duplicação (idempotência);
- Falha durante o processo (rollback total);

---

## 🧰 Tecnologias sugeridas

- Java 21+
- Spring Boot 3+
- Maven
- H2 Database (ou outro relacional)
- JUnit / Mockito
- Docker

Você pode usar Lombok, Flyway, Docker Compose ou qualquer ferramenta útil — apenas documente as suas escolhas.

---

## ▶️ Como executar

```bash
# Clonar o repositório
git clone https://github.com/morusbank/engineering-challenge.git
cd engineering-challenge

# Executar a aplicação
mvn spring-boot:run
```

---

## 📦 Entrega

1. Crie um zip da pasta do projeto (sem a pasta `target` e arquivos desnecessários) e envie para o e-mail
   [contato@morusbank.com.br](mailto:contato@morusbank.com.br) informando no assunto "Desafio Técnico -
   Engenharia - [Seu Nome]".
2. Inclua no seu `README.md`:
    - Visão geral do projeto com uma breve descrição da sua solução e principais decisões;
    - Os passos necessários para rodar a aplicação e validar os cenários solicitados no desafio;
    - Quaisquer melhorias que você faria se tivesse mais tempo.

---

## 💬 Dicas finais

* O objetivo não é complexidade — é **clareza e robustez**.
* Queremos ver como você estrutura um fluxo de negócio crítico com simplicidade e coesão.
* Um código limpo e transacional vale mais do que qualquer abstração sofisticada.
* O uso de padrões de projeto (Design Patterns) é bem-vindo, mas **não exagere**.
* Seja criativo e mostre as suas habilidades. Caso não consiga completar tudo, não tem problema. Queremos ver o seu
  raciocínio e qualidade do código, não esperamos uma solução perfeita.
* O uso de IA é permitido e incentivado, porém, lembre-se de que o código é seu e você deve entender claramente o que
  está implementado.

Você tem **7 dias corridos** para completar o desafio a partir do recebimento deste e-mail, mas se terminar antes,
sinta-se à vontade para enviar.

---

> Caso opte por implementar o desafio utilizando outra linguagem, não tem problema, mas **explique as suas escolhas**
> no README do seu projeto e coloque as instruções de como executar a aplicação.

**Boa sorte!**  
A equipe da Morus está animada para ver como você transforma lógica de repasse em um sistema confiável e elegante 🚀

>
> Any fool can write code that a computer can understand. Good programmers write code that humans can understand.
> – Martin Fowler
>
