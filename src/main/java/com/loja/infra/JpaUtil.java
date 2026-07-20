package com.loja.infra;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;

public class JpaUtil {

    private static final EntityManagerFactory emf;

    static {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();

        // exec:java isola o TCCL do Maven — Flyway 10 usa ServiceLoader com TCCL para
        // descobrir o plugin MySQL. Trocamos temporariamente pelo classloader da própria
        // classe, que tem acesso ao META-INF/services do flyway-mysql.
        // Flyway e Hibernate usam ServiceLoader com TCCL para descobrir plugins.
        // exec:java isola o TCCL do Maven, então trocamos temporariamente pelo
        // classloader da própria classe, que enxerga todos os META-INF/services.
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(JpaUtil.class.getClassLoader());
        try {
            Flyway.configure()
                  .dataSource(env.get("DB_URL"), env.get("DB_USER"), env.get("DB_PASSWORD"))
                  .locations("classpath:db/migration")
                  .load()
                  .migrate();

            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url",      env.get("DB_URL"));
            props.put("jakarta.persistence.jdbc.user",     env.get("DB_USER"));
            props.put("jakarta.persistence.jdbc.password", env.get("DB_PASSWORD"));

            emf = Persistence.createEntityManagerFactory("lojaPU", props);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    public static EntityManager getEntityManager() { return emf.createEntityManager(); }

    public static void close() { emf.close(); }
}
