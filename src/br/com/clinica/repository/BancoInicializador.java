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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * Roda uma vez, quando o Tomcat liga o site:
 *   1. lê o usuário e a senha do MySQL em WEB-INF/banco.properties
 *   2. se o banco clinica_estetica ainda não existe, cria tudo rodando o WEB-INF/banco.sql
 */
@WebListener
public class BancoInicializador implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext site = evento.getServletContext();
        try {
            lerConfiguracao(site);
            if (bancoExiste()) {
                System.out.println("[Clínica] Banco encontrado, mantendo os dados.");
            } else {
                System.out.println("[Clínica] Banco não existe: criando com o banco.sql...");
                rodarScript(site);
                System.out.println("[Clínica] Banco criado com sucesso.");
            }
        } catch (Exception e) {
            System.out.println("==========================================================");
            System.out.println(" [Clínica] NÃO FOI POSSÍVEL PREPARAR O BANCO DE DADOS.");
            System.out.println(" - O MySQL está ligado?");
            System.out.println(" - O usuário e a senha em WEB-INF/banco.properties estão certos?");
            System.out.println(" Detalhe: " + e.getMessage());
            System.out.println("==========================================================");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        // nada para fazer quando o site desliga
    }

    private void lerConfiguracao(ServletContext site) throws Exception {
        InputStream arquivo = site.getResourceAsStream("/WEB-INF/banco.properties");
        if (arquivo == null) {
            return;   // sem arquivo: usa root sem senha
        }
        Properties config = new Properties();
        config.load(new InputStreamReader(arquivo, StandardCharsets.UTF_8));
        Banco.configurar(config.getProperty("usuario", "root").trim(), config.getProperty("senha", "").trim());
    }

    private boolean bancoExiste() throws Exception {
        try (Connection conexao = Banco.conectarSemBanco();
             Statement comando = conexao.createStatement();
             ResultSet resultado = comando.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.tables "
                             + "WHERE table_schema = 'clinica_estetica' AND table_name = 'servico'")) {
            resultado.next();
            return resultado.getInt(1) > 0;
        }
    }

    /** Lê o banco.sql e roda cada comando (eles terminam com ";"). Linhas que começam com "--" são comentários. */
    private void rodarScript(ServletContext site) throws Exception {
        InputStream arquivo = site.getResourceAsStream("/WEB-INF/banco.sql");
        BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo, StandardCharsets.UTF_8));

        try (Connection conexao = Banco.conectarSemBanco();
             Statement comando = conexao.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.trim().startsWith("--")) {
                    continue;
                }
                sql.append(linha).append("\n");
                if (linha.trim().endsWith(";")) {
                    comando.execute(sql.toString());
                    sql.setLength(0);
                }
            }
        }
        leitor.close();
    }
}
