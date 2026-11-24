package br.com.clinica.repository;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Conexão com o banco MariaDB (o banco que vem no XAMPP).
 *
 * Quando o Tomcat liga o site, o método contextInitialized roda uma vez:
 *   1. lê o usuário e a senha do banco em WEB-INF/banco.properties
 *   2. se o banco clinica_estetica ainda não existe, cria tudo rodando o WEB-INF/banco.sql
 */
@WebListener
public class Banco implements ServletContextListener {

    private static final String SERVIDOR = "jdbc:mariadb://localhost:3306/";
    private static final String OPCOES = "?allowPublicKeyRetrieval=true";   // permite também entrar num MySQL 8

    private static String usuario = "root";
    private static String senha = "";

    /** Abre uma conexão com o banco do site. */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(SERVIDOR + "clinica_estetica" + OPCOES, usuario, senha);
    }

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext site = evento.getServletContext();
        try {
            Class.forName("org.mariadb.jdbc.Driver");   // driver do MariaDB (o .jar em WEB-INF/lib)
            lerConfiguracao(site);

            // Conecta no MariaDB sem escolher banco, porque ele pode ainda não existir
            try (Connection conexao = DriverManager.getConnection(SERVIDOR + OPCOES, usuario, senha);
                 Statement comando = conexao.createStatement()) {

                ResultSet resultado = comando.executeQuery("SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'clinica_estetica' AND table_name = 'servico'");
                resultado.next();

                if (resultado.getInt(1) > 0) {
                    System.out.println("[Clínica] Banco encontrado, mantendo os dados.");
                } else {
                    System.out.println("[Clínica] Banco não existe: criando com o banco.sql...");
                    rodarScript(site, comando);
                    System.out.println("[Clínica] Banco criado com sucesso.");
                }
            }
        } catch (Exception e) {
            System.out.println("[Clínica] NÃO FOI POSSÍVEL PREPARAR O BANCO DE DADOS.");
            System.out.println("[Clínica] O MySQL/MariaDB está ligado no XAMPP? O usuário e a senha em WEB-INF/banco.properties estão certos?");
            System.out.println("[Clínica] Detalhe: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        // nada para fazer quando o site desliga
    }

    private void lerConfiguracao(ServletContext site) throws Exception {
        InputStream arquivo = site.getResourceAsStream("/WEB-INF/banco.properties");
        if (arquivo != null) {
            Properties config = new Properties();
            config.load(new InputStreamReader(arquivo, StandardCharsets.UTF_8));
            usuario = config.getProperty("usuario", "root").trim();
            senha = config.getProperty("senha", "").trim();
        }
    }

    /** Roda cada comando do banco.sql (eles terminam com ";"). Linhas que começam com "--" são comentários. */
    private void rodarScript(ServletContext site, Statement comando) throws Exception {
        InputStream arquivo = site.getResourceAsStream("/WEB-INF/banco.sql");
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo, StandardCharsets.UTF_8))) {
            String sql = "";
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.trim().startsWith("--")) {
                    continue;
                }
                sql += linha + "\n";
                if (linha.trim().endsWith(";")) {
                    comando.execute(sql);
                    sql = "";
                }
            }
        }
    }
}
