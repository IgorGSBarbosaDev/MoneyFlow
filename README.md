# MoneyFlow API 💰

API RESTful para gestão financeira pessoal, permitindo controle completo de receitas, despesas, orçamentos e análise de gastos.

## 📋 Sobre o Projeto

O MoneyFlow resolve um problema comum: pessoas têm dificuldade para controlar gastos, criar orçamentos realistas e entender para onde vai o dinheiro. Diferente de aplicativos existentes que são complexos demais ou caros, esta API oferece uma solução simples, completa e gratuita.

### Principais Funcionalidades

- Registro e gerenciamento de receitas e despesas
- Categorização inteligente de gastos
- Definição de metas de economia por categoria
- Relatórios detalhados de gastos mensais
- Alertas automáticos quando categorias excedem o orçamento
- Análises comparativas entre períodos
- Filtros avançados por data, categoria e tipo

## 🚀 Tecnologias

- **Java 17** - Linguagem principal
- **Spring Boot 3** - Framework base
- **Spring Security + JWT** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados relacional
- **Docker + Docker Compose** - Containerização
- **Swagger/OpenAPI** - Documentação interativa da API
- **Spring Scheduler** - Alertas e tarefas automáticas

## 📐 Arquitetura

O projeto demonstra boas práticas como:

- Relacionamentos complexos (Usuário → Categorias → Transações)
- Agregações e estatísticas com queries otimizadas
- DTOs bem estruturados para separação de camadas
- Validações robustas de regras de negócio
- Tratamento centralizado de exceções
- Queries otimizadas com JPA e JPQL


## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/moneyflow/
│   │       ├── config/          # Configurações (Security, Swagger)
│   │       ├── controller/      # Controllers REST
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── model/           # Entidades JPA
│   │       ├── repository/      # Repositórios JPA
│   │       ├── service/         # Lógica de negócio
│   │       ├── exception/       # Tratamento de exceções
│   │       └── scheduler/       # Tarefas agendadas
│   └── resources/
│       ├── application.yml      # Configurações da aplicação
│       └── db/migration/        # Scripts SQL Flyway
└── test/                        # Testes unitários e integração
```

