# Serviço de solicitação de acesso <h5>`Access Request Service`</h5>

Serviço de controle de acessos corporativos com autenticação JWT, solicitações de acesso, renovação e histórico, pronto para execução local com Docker e com suíte de testes e cobertura configuradas.

<div align="center"> 
  <img src="https://github.com/acrisiopb/access-request-service/blob/main/Img/Access.gif" alt="Apresentação">
</div> 

## Descrição do Projeto
- API REST para gerenciar:
  - Usuários, módulos e acessos ativos
  - Solicitações de acesso (criação, cancelamento, renovação)
  - Regras de negócio aplicadas às solicitações
  - Autenticação e autorização via JWT
- Foco em testes unitários e de integração com cobertura mínima de 90% (JaCoCo).

## Tecnologias Utilizadas e Versões

<div align="center"> 
  <img src="https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white" alt="Badge Spring">
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=Swagger&logoColor=white" alt="Badge Spring">
   <img src="https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens" alt="">
   <img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="">
   <img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white" alt="">
   <img src="https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white" alt="">
   <img src="https://img.shields.io/badge/JUnit5-f5f5f5?style=for-the-badge&logo=junit5&logoColor=dc524a" alt="">
   <img src="https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white" alt="">
   <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white" alt="">
   <img src="https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white" alt="">
</div>
<hr>
<div  align="center">
 Java 21 (Temurin)
- Spring Boot 3.3.5
- Spring Web 6.1.14
- Spring Security 6.3.4 (JWT)
- Lombok
- Jakarta Validation
- H2 (perfil de teste)
- PostgreSQL 17 (Docker Compose)
- Maven Wrapper (wrapperVersion 3.3.4)
- JUnit 5.10.5
- Mockito
- Swagger 
- JaCoCo 0.8.11
- Nginx (load balancer)
</div>

## Pré-requisitos
- Docker
  https://www.docker.com/products/docker-desktop/
- Docker Compose
- Opcional para execução sem Docker: Java 21 e Maven Wrapper

## Executar Localmente com Docker
1. Build e subir ambiente completo (PostgreSQL + 3 instâncias da API atrás do Nginx):
   - Windows:  
   Build\
   `docker compose up -d --build`\
   Start\
   `docker compose up -d`

2. Acesso à API via Nginx em `http://localhost:8080/swagger-ui/index.html#/`
 OBS: Aguarde alguns segundos caso seja a primeira vez que executou o  build, normalmente ele informa na tela 502 Bad Gateway, mais isso é porque aplicação ainda está sendo executada, depois recarregue a pagina.  
3. Perfis:
   - App usa perfil `postgres` quando executada via Compose (variáveis `DB_URL`, `DB_USER`, `DB_PASS`)

## Executar Sem Docker (opcional)
- Windows: `mvn spring-boot:run`\

  OBS: Se estiver executando sem Docker, você pode abrir sua IDE e rodar o projeto normalmente com o comando informado. O banco será criado no H2 temporariamente.

- Porta padrão: `http://localhost:8080/`

## Executar os Testes
- Windows: `mvn -q test`

OBS: os testes de controllers exercitam cenários de erro (401/404/422). O handler de exceções loga esses eventos; isso é esperado e não indica falha.

## Visualizar Relatório de Cobertura
1. Gerar relatório Jacoco:
   - Windows:\
     OBS: abra o terminal na sua IDE e cole o codigo 
    `mvn -q jacoco:report`
2. Abrir:
   - Windows:\
    OBS: Com sua IDE aberta procure o index.html na sua pasta `Target` do projeto ->  
   `.\target\site\jacoco\index.html`

## Credenciais para Teste
- Usuário (perfil de teste / H2):
  - Email: `test@admin.com`
  - Senha: `test123`
- Usuários adicionais (perfil dev / Postgres) estão em `src/main/resources/import-dev.sql`.


<h1>Prévia - Captura de Tela</h1>

 Img Swagger I |  Img Swagger II   |   Img  Swagger III                                                    |
|:-----:|:-----------------------------------------------------------------------------------------------------------------:|:----------------------------------------------------------------------------------------------------------------------:|
  <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988141/Swagger_I_nxbbej.png" alt="1" width="400" /> | <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988141/Swagger_II_tvep4d.png" alt="2" width="400" /> | <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988141/Swagger_III_fqeyuu.png" alt="3" width="400" /> |

 Img  Swagger IV | Img Swagger V  |    Img Relatorio Jacoco VI                                              |
|:-----:|:-----------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|
  <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988142/Swagger_IV_gayn7b.png" alt="4" width="400" /> |                                           <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988142/Swagger_V_wuqqo3.png" alt="5" width="400" />                                            | <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988144/JACOCO_klzxad.png" alt="6" width="400" /> |


 Img Docker VII  | Img Diagrama VIII   |   Img Diagrama IX
|:-----:|:----------------------------------------------------------------------------------------------------------------:|:------------------:| 
<img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988141/docker_iliq4g.png" alt="7" width="400" /> |                                        <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988140/Diagrama_I_qkapp6.png" alt="8" width="400" />                                        | <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988141/Diagrama_II_g9wznd.png" alt="8" width="400" /> 

Img Diagrama X  |  Img Diagrama XI |  Img Diagrama XII
|:-----:|:----------------------------------------------------------------------------------------------------------------:|:------------------:| 
<img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988141/Diagrama_III_fhkw2c.png" alt="7" width="400" />          | <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988141/Diagrama_IV_rsyzg4.png" alt="7" width="400" />                     |<img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988142/Diagrama_V_lysahh.png" alt="7" width="400" />  
Img Diagrama XIII  |  Img Diagrama XIV 
<img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988142/Diagrama_VI_vn2xog.png" alt="7" width="400" />  |    <img src="https://res.cloudinary.com/dyk1w5pnr/image/upload/v1763988144/Diagrama_VII_tunhmn.png" alt="7" width="400" /> 
 
## Exemplos de Requisições
Autenticar e obter token:
```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@corp.com","password":"alice123"}'
```
Usar token (`Authorization: Bearer <token>`):
- Listar módulos:
```bash
curl -s http://localhost:8080/modules -H "Authorization: Bearer <token>"
```
- Criar solicitação de acesso:
```bash
curl -s -X POST http://localhost:8080/request \
  -H "Authorization: Bearer <token>" -H 'Content-Type: application/json' \
  -d '{"moduleIds":[10],"justification":"Justificativa detalhada","urgent":true}'
```
- Cancelar solicitação:
```bash
curl -s -X POST http://localhost:8080/request/cancel \
  -H "Authorization: Bearer <token>" -H 'Content-Type: application/json' \
  -d '{"id":123,"reason":"Motivo válido"}'
```
- Renovar solicitação:
```bash
curl -s -X POST http://localhost:8080/request/renew \
  -H "Authorization: Bearer <token>" -H 'Content-Type: application/json' \
  -d '{"id":123}'
```
- Filtrar solicitações:
```bash
curl -s "http://localhost:8080/request/filter?status=CANCELED&size=10" \
  -H "Authorization: Bearer <token>"
```
- Acessos:
```bash
# Listar
curl -s http://localhost:8080/access -H "Authorization: Bearer <token>"
# Buscar por ID
curl -s -X POST http://localhost:8080/access/find \
  -H "Authorization: Bearer <token>" -H 'Content-Type: application/json' \
  -d '{"id":50}'
# Renovar acesso
curl -s -X POST http://localhost:8080/access/renew \
  -H "Authorization: Bearer <token>" -H 'Content-Type: application/json' \
  -d '{"id":50}'
# Revogar acesso
curl -s -X POST http://localhost:8080/access/revoke \
  -H "Authorization: Bearer <token>" -H 'Content-Type: application/json' \
  -d '{"id":50}'
```

## Arquitetura da Solução
- Camadas:
  - Controller (`com.acrisio.accesscontrol.api.controller`): endpoints REST, autenticação via JWT, integração com `CurrentUserProvider` para identificar usuário do token.
  - Service (`com.acrisio.accesscontrol.service`): regras de negócio (criação/cancelamento/renovação de solicitações, renovação/revogação de acessos), uso de `AccessRequestRule` para validar cenários.
  - Domain/Model (`com.acrisio.accesscontrol.domain.model`): entidades JPA (`User`, `Module`, `Access`, `AccessRequest`, `RequestHistory`).
  - Repository (`com.acrisio.accesscontrol.domain.repository`): Spring Data JPA.
  - Infra (`com.acrisio.accesscontrol.infrastructure`): segurança JWT, especificações de consulta, i18n utilitário.
- Persistência:
  - Testes: H2 in-memory
  - Produção local: PostgreSQL via Docker Compose
- Deploy local: Nginx balanceando 3 instâncias da aplicação (`app1`, `app2`, `app3`), todas conectadas ao mesmo Postgres.



  <h3>Aviso Legal</h3> 
 <div  align="center">
  <picture>
    <!-- Imagem para o modo escuro -->
    <source srcset="https://github.com/acrisiopb/acrisiopb/blob/main/IMG/acrisioBlack.gif" media="(prefers-color-scheme: dark)">
    <!-- Imagem para o modo claro -->
    <source srcset="https://github.com/acrisiopb/acrisiopb/blob/main/IMG/acrisiowhite.gif" media="(prefers-color-scheme: light)">
    <img src="https://github.com/acrisiopb/acrisiopb/blob/main/IMG/acrisioBlack.gif" alt="capa GitHub Acrísio">
  </picture>
   <p>Desenvolvido por Acrísio Cruz. Todos os direitos reservados © 2025.</p>
</div>

