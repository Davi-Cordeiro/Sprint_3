package br.com.clinica.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre a conexão com o MySQL.
 * O usuário e a senha vêm do arquivo WEB-INF/banco.properties
 * (quem lê o arquivo é o BancoInicializador, quando o Tomcat liga o site).
 */
public class Banco {

    private static final String SERVIDOR = "jdbc:mysql://localhost:3306/";
    private static final String OPCOES = "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";

    private static String usuario = "root";
    private static String senha = "";

    public static void configurar(String novoUsuario, String novaSenha) {
        usuario = novoUsuario;
        senha = novaSenha;
    }

    /** Conexão com o banco do site (clinica_estetica). */
    public static Connection conectar() throws SQLException {
        return abrir(SERVIDOR + "clinica_estetica" + OPCOES);
    }

    /** Conexão com o MySQL sem escolher banco (usada para criar o banco na primeira vez). */
    public static Connection conectarSemBanco() throws SQLException {
        return abrir(SERVIDOR + OPCOES);
    }

    private static Connection abrir(String url) throws SQLException {
        try {
            // Carrega o driver do MySQL (o arquivo .jar que está em WEB-INF/lib)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver do MySQL não encontrado. Confira o .jar em WEB-INF/lib.", e);
        }
        return DriverManager.getConnection(url, usuario, senha);
    }
}
