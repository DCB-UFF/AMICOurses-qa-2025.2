# Guia de Testes - AMICOurses QA

## Visão Geral

Este documento descreve a estratégia de testes para o projeto **AMICOurses**, incluindo testes de integração, testes de performance e testes de sistema (E2E com Selenium).

O projeto utiliza três estratégias principais de teste:
- **Testes de Integração (API)**: Via Postman
- **Testes de Performance**: Via Postman
- **Testes de Sistema (E2E)**: Via Selenium com Java/JUnit5

---

## Estrutura de Testes

```
AMICOurses-qa-2025.2/
├── Testes de Integração.postman_collection.json    # Testes de integração da API
├── Testes de Performance.postman_collection.json    # Testes de performance
│
└── AMICOServer/src/test/java/com/example/demo/e2e/
    ├── ElastestBaseTest.java                        # Classe base para E2E
    ├── TestE2EFront.java                           # Testes de sistema (UI)
    │
    └── api/
        ├── ElasTestBase.java                       # Classe base para testes API
        ├── TestAPI.java                            # Testes API via MockMvc
        ├── TestAPIRestTemplate.java                # Testes API via RestTemplate
        └── SSLClientFactory.java                   # Factory para SSL/HTTPS
```

---

## 1. Testes de Integração - Postman

### Arquivo: `Testes de Integração.postman_collection.json`

Testes de integração sequenciais que validam fluxos completos da API REST.

### Estrutura dos Testes

#### 1.1 Cadastro (POST /api/users)

Objetivo: Validar criação de novo usuário

Endpoint: POST {{baseUrl}}/api/users

Validações:
- Status 201 (Created)
- Username retornado corretamente

---

#### 1.2 Acesso - Dados de Login (GET /api/logIn)

Objetivo: Validar autenticação do usuário

Validações:
- Status 200 (OK)

---

#### 1.3 Acesso - Perfil (GET /api/users/myProfile)

Objetivo: Recuperar dados do perfil do usuário logado

Validações:
- Status 200 (OK)
- Dados completos do perfil retornados

---

#### 1.4 Cursos - Listar (GET /api/courses/)

Objetivo: Listar todos os cursos com paginação

Validações:
- Status 200 (OK)
- Array `content` não vazio
- Estrutura paginada correta

---

#### 1.5 Cursos - Obter Um (GET /api/courses/id/{courseID})

Objetivo: Recuperar detalhes de um curso específico

Validações:
- Status 200 (OK)
- Dados corretos (ID = 1, Name = "Introduction to AI")

---

#### 1.6 Cursos - Criar (POST /api/courses/)

Objetivo: Criar novo curso (requer permissão admin)

Validações:
- Status 201 (Created)
- ID do novo curso retornado

---

#### 1.7 Cursos - Validar Criação (GET /api/courses/id/{{newCourseId}}/)

Objetivo: Verificar se o curso foi criado corretamente

Validações:
- Status 200 (OK)
- Nome do curso matches o criado

---

#### 1.8 Cursos - Deletar (DELETE /api/courses/id/{{newCourseId}}/)

Objetivo: Remover curso criado

Validações:
- Status 200 (OK)

---

#### 1.9 Cursos - Confirmar Exclusão (GET /api/courses/id/{{newCourseId}}/)

Objetivo: Validar que o curso foi removido

Validações:
- Status 404 (Not Found)

---

#### 1.10 Logout (GET /logOut)

Objetivo: Finalizar sessão do usuário

---

### Como Executar Testes de Integração

Pré-requisitos:
- Postman instalado
- Servidor rodando em http://localhost:8000

Executar a collection `Testes de Integração.postman_collection.json` com baseUrl configurada como `http://localhost:8000`. Os testes devem retornar PASS/FAIL.

---

## 2. Testes de Performance - Postman

### Arquivo: `Testes de Performance.postman_collection.json`

Testes similares aos de integração, mas com foco em avaliar tempo de resposta e carga.

### Características

O arquivo utiliza:
- Usuários dinâmicos com credenciais únicas por execução
- Cursos dinâmicos com variáveis randomizadas
- Métricas automáticas de Response Time, Size e Status Code

### Métricas de Performance

Postman fornece automaticamente:
- Response Time: Tempo de resposta (ms)
- Size: Tamanho da resposta (bytes)
- Status Code: Código HTTP da resposta

Métricas Esperadas:
- Response Time: < 200ms (ideal), 200-500ms (alerta), > 500ms (crítico)
- CPU Usage: < 50% (ideal), 50-80% (alerta), > 80% (crítico)
- Memory: < 60% (ideal), 60-80% (alerta), > 80% (crítico)
- Error Rate: 0% (ideal), < 1% (alerta), > 1% (crítico)

### Como Executar Testes de Performance

Executar a collection `Testes de Performance.postman_collection.json` via Postman UI ou CLI (Newman). Configurar iterações e delays conforme necessário. Os resultados incluem Response Time, Size e Status Code de cada requisição.

---

## 3. Testes de Sistema (E2E) - Selenium

### Arquivo Base: `AMICOServer/src/test/java/com/example/demo/e2e/`

Testes end-to-end que validam a interface do usuário e fluxos completos.

### Classes Principais

#### 3.1 ElastestBaseTest.java

Classe base para todos os testes E2E com Selenium.

Responsabilidades:
- Configurar driver (Chrome/Firefox)
- Inicializar WebDriver
- Gerenciar ciclo de vida (BeforeEach/AfterEach)
- Definir URL da aplicação

Navegadores Suportados:
- Chrome (default)
- Firefox
- Remoto via ElasTest EUS

---

#### 3.2 TestE2EFront.java

Testes end-to-end da interface frontend.

**Teste 1: checkCreateCourse()**

Objetivo: Validar criação e exclusão de curso via UI

Fluxo:
- Login como admin (admin/pass)
- Navegar para Management > Add Course
- Preencher formulário com dados
- Submeter formulário
- Validar criação em tabela
- Deletar curso criado
- Confirmar exclusão
- Logout

---

**Teste 2: checkDownload()**

Objetivo: Validar download de arquivo de curso

Fluxo:
- Login como estudante (amico/pass)
- Navegar para perfil
- Acessar primeiro curso inscrito
- Selecionar primeira disciplina
- Validar download bem-sucedido
- Limpar pasta de downloads
- Logout

---

**Teste 3: checkShowProfile()**

Objetivo: Validar acesso a perfil

Fluxo:
- Tentar acessar perfil (/profile/amico)
- Validar redirecionamento (página não exibida)

---

#### 3.3 Métodos Utilitários Disponíveis

- goToPage(): Ir para página inicial
- goToPage(String page): Ir para página específica
- waitUntil(): Aguardar condição com timeout
- getVisibleElement(): Obter elemento visível
- sendKeysAfterClear(): Limpar e escrever
- sleep(): Aguardar em millisegundos
- cleanDownloadFolder(): Limpar downloads
- checkDownloadFile(): Validar download

---

#### 3.4 TestAPI.java e TestAPIRestTemplate.java

Testes unitários da API via MockMvc e RestTemplate.

TestAPI.java valida endpoints individuais usando MockMvc.
TestAPIRestTemplate.java testa via RestTemplate com suporte a SSL.

---

### Como Executar Testes E2E

Pré-requisitos:
- Java 8+
- Maven 3.6+
- Chrome ou Firefox instalado
- WebDriver (chromedriver ou geckodriver)

Os testes podem ser executados via Maven, IDE ou em ambiente remoto com variáveis de ambiente específicas. WebDriver Manager (incluído no pom.xml) gerencia automaticamente os drivers.

---

## Matriz de Testes

| Tipo | Ferramenta | Arquivo | Casos | Cobertura |
|------|-----------|---------|-------|-----------|
| Integração | Postman | Testes de Integração.postman_collection.json | 10 | API REST |
| Performance | Postman | Testes de Performance.postman_collection.json | 10 | Tempo de Resposta |
| E2E | Selenium | TestE2EFront.java | 3 | UI/Frontend |
| Unitária (API) | MockMvc | TestAPI.java | 1 | Controllers |
| Integração (API) | RestTemplate | TestAPIRestTemplate.java | 1 | Endpoints |

---

## Usuários de Teste

| Username | Password | Tipo | Permissões |
|----------|----------|------|------------|
| admin | pass | Admin | Criar cursos, gerenciar usuários |
| amico | pass | Estudante | Inscrever em cursos, submeter práticas |
| amicoteacher | pass | Professor | Corrigir práticas |
| IntegrationUser | pass123 | Estudante | (Criado durante teste) |
| PerfUser_* | pass123 | Estudante | (Criado dinamicamente) |

---

## Troubleshooting

**Problema: Porta 8000 já está em uso**

Liberar porta:
```bash
lsof -i :8000
kill -9 <PID>
```

Ou mudar porta em application.properties:
```
server.port=8001
```

---

**Problema: WebDriver não encontrado**

WebDriver Manager já está incluído no pom.xml. Se necessário, baixe manualmente:
- Chrome: https://chromedriver.chromium.org/
- Firefox: https://github.com/mozilla/geckodriver/releases

---

**Problema: Element not found em testes E2E**

Aumentar timeout na espera ou verificar seletores dos elementos.

---

**Problema: Download não funciona**

Verificar pasta de download configurada no teste.

---

## Padrões e Boas Práticas

Nomeação de Testes:
- Usar nomes descritivos que indiquem o que está sendo testado
- Exemplo: checkCreateCourse_WhenAdminLogsIn_ShouldCreateSuccessfully()

Assertions:
- Usar assertions claras e com mensagens de erro explicativas

Waits:
- Usar waits explícitos (waitUntil) em vez de sleep

Limpeza de Recursos:
- Implementar limpeza automática em @AfterEach
- Limpar dados de teste (downloads, registros)

---

## Pipeline de CI/CD

Execução em pipeline:

```yaml
test:
  script:
    # Testes unitários
    - mvn test
    
    # Testes E2E
    - mvn test -Dgroups=e2e
    
    # Testes Postman
    - newman run "Testes de Integração.postman_collection.json"
    - newman run "Testes de Performance.postman_collection.json"
```

---

## Checklist de Testes Antes do Deploy

- Todos os testes de integração passando
- Testes de performance dentro dos limites
- Testes E2E executados com sucesso
- Sem erros críticos nos logs
- Cobertura de testes > 70%
- Documentação atualizada
- Dados de teste limpos

---

## Recursos Adicionais

- Documentação Postman: https://learning.postman.com/
- Selenium Documentation: https://www.selenium.dev/documentation/
- JUnit 5 Guide: https://junit.org/junit5/docs/current/user-guide/
- API.md: Documentação da API REST

---

Versão: 1.0
Última Atualização: Dezembro 2025
Mantido por: DCB-UFF
