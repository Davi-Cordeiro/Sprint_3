package br.com.clinica.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Roda quando o site liga. Se o banco ainda não tem as tabelas,
 * executa o database/banco.sql para criar tudo com os dados de exemplo.
 */
@Component
public class BancoInicializador {

    public BancoInicializador(DataSource dataSource, JdbcTemplate jdbc) {
        int tabelas;
        try {
            tabelas = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema = DATABASE() AND table_name = 'servico'",
                    Integer.class);
        } catch (Exception e) {
            System.out.println("==========================================================");
            System.out.println(" NÃO FOI POSSÍVEL CONECTAR AO MYSQL.");
            System.out.println(" Confira o usuário e a senha no arquivo mysql.properties.");
            System.out.println("==========================================================");
            throw e;
        }

        if (tabelas > 0) {
            System.out.println("Banco já existe, mantendo os dados.");
            return;
        }

        System.out.println("Banco vazio: criando as tabelas com o banco.sql...");
        ResourceDatabasePopulator script = new ResourceDatabasePopulator(new ClassPathResource("banco.sql"));
        script.setSqlScriptEncoding("UTF-8");
        script.execute(dataSource);
    }
}
