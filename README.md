# Sistema de Pedidos

Projeto Java 21 demonstrando **agregação** e **composição** com MySQL via JDBC.

## Pré-requisitos

- Java 21
- Maven 3.6+
- Docker Desktop

## Configuração

Copie o arquivo de exemplo e preencha com suas credenciais:

```bash
cp .env.example .env
```

Edite `.env` com os valores reais (o arquivo nunca vai para o Git).

## Subir o banco de dados

```bash
docker-compose up -d
```

Aguarde o status `healthy`:

```bash
docker-compose ps
```

## Rodar a aplicação

```bash
mvn exec:java "-Dexec.mainClass=com.loja.main.Main"
```

O Flyway cria as tabelas automaticamente na primeira execução.

## Parar o banco

```bash
docker-compose down
```

Para apagar os dados também:

```bash
docker-compose down -v
```
