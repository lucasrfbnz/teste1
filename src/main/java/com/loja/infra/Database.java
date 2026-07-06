package com.loja.infra;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private static final HikariDataSource ds;

    static {
        // lê .env automaticamente; se o arquivo não existir, usa variáveis do SO
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        String url      = env.get("DB_URL");
        String user     = env.get("DB_USER");
        String password = env.get("DB_PASSWORD");

        // Flyway: roda as migrations de src/main/resources/db/migration/ na inicialização
        Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        // HikariCP: cria um pool de até 10 conexões reutilizáveis
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection(); // pega uma conexão do pool (não cria uma nova)
    }
}
