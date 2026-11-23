package br.com.clinica.repository;

import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Avaliacao;
import br.com.clinica.model.Cliente;
import br.com.clinica.model.Servico;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Todo o acesso ao banco MySQL fica aqui, com SQL escrito à mão.
 *
 * O JdbcTemplate roda o SQL. Nas consultas, o trecho (rs, linha) -> new Classe(...)
 * diz como transformar cada linha do resultado em um objeto Java.
 */
@Repository
public class ClinicaRepository {

    private final JdbcTemplate jdbc;

    public ClinicaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    // ==================== Serviços ====================

    public List<Servico> listarServicos() {
        String sql = "SELECT id_servico, nome_servico, preco, descricao FROM servico ORDER BY id_servico";
        return jdbc.query(sql, (rs, linha) -> new Servico(
                rs.getInt("id_servico"),
                rs.getString("nome_servico"),
                rs.getDouble("preco"),
                rs.getString("descricao")));
    }

    /** Devolve o serviço ou null se o id não existir. */
    public Servico buscarServico(int id) {
        String sql = "SELECT id_servico, nome_servico, preco, descricao FROM servico WHERE id_servico = ?";
        List<Servico> encontrados = jdbc.query(sql, (rs, linha) -> new Servico(
                rs.getInt("id_servico"),
                rs.getString("nome_servico"),
                rs.getDouble("preco"),
                rs.getString("descricao")), id);

        if (encontrados.isEmpty()) {
            return null;
        }
        return encontrados.get(0);
    }

    public int contarProfissionais() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM profissional", Integer.class);
    }


    // ==================== Clientes ====================

    /** Devolve o cliente com esse e-mail ou null se não existir. */
    public Cliente buscarClientePorEmail(String email) {
        String sql = "SELECT id, nome_cliente, email FROM cliente WHERE email = ?";
        List<Cliente> encontrados = jdbc.query(sql, (rs, linha) -> new Cliente(
                rs.getInt("id"),
                rs.getString("nome_cliente"),
                rs.getString("email")), email.toLowerCase());

        if (encontrados.isEmpty()) {
            return null;
        }
        return encontrados.get(0);
    }

    /** Senha (criptografada) do cliente, ou null se ele nunca criou senha. */
    public String buscarSenha(String email) {
        List<String> senhas = jdbc.queryForList(
                "SELECT senha FROM cliente WHERE email = ?", String.class, email.toLowerCase());

        if (senhas.isEmpty()) {
            return null;
        }
        return senhas.get(0);
    }

    public void definirSenha(String email, String senhaCriptografada) {
        jdbc.update("UPDATE cliente SET senha = ? WHERE email = ?", senhaCriptografada, email.toLowerCase());
    }

    /**
     * Se o e-mail já está cadastrado, atualiza o nome e os telefones.
     * Se não está, cadastra um cliente novo.
     */
    public void salvarCliente(String nome, String whatsapp, String celular, String email) {
        if (buscarClientePorEmail(email) == null) {
            jdbc.update("INSERT INTO cliente (nome_cliente, whatsapp, celular, email) VALUES (?, ?, ?, ?)",
                    nome, vazioViraNull(whatsapp), vazioViraNull(celular), email.toLowerCase());
        } else {
            // COALESCE: se o telefone novo veio vazio, mantém o que já estava salvo
            jdbc.update("UPDATE cliente SET nome_cliente = ?, "
                            + "whatsapp = COALESCE(?, whatsapp), celular = COALESCE(?, celular) "
                            + "WHERE email = ?",
                    nome, vazioViraNull(whatsapp), vazioViraNull(celular), email.toLowerCase());
        }
    }


    // ==================== Agendamentos ====================

    /**
     * Quantos atendimentos já existem em cada horário entre duas datas.
     * Exemplo de resultado: { "2026-12-01 09:00" = 2, "2026-12-01 10:00" = 1 }
     */
    public Map<String, Integer> ocupacaoEntre(String dataInicio, String dataFim) {
        String sql = "SELECT CONCAT(data, ' ', TIME_FORMAT(hora, '%H:%i')) AS horario, COUNT(*) AS total "
                + "FROM agendamento WHERE data BETWEEN ? AND ? GROUP BY data, hora";

        Map<String, Integer> ocupacao = new HashMap<>();
        List<Map<String, Object>> linhas = jdbc.queryForList(sql, dataInicio, dataFim);
        for (Map<String, Object> linha : linhas) {
            String horario = (String) linha.get("horario");
            int total = ((Number) linha.get("total")).intValue();
            ocupacao.put(horario, total);
        }
        return ocupacao;
    }

    /**
     * Grava o agendamento com o primeiro profissional livre naquele horário.
     * Devolve o nome do profissional, ou null se não sobrou nenhum livre.
     */
    public String registrarAgendamento(int idCliente, int idServico, String data, String hora) {
        String sqlLivre = "SELECT id FROM profissional p "
                + "WHERE NOT EXISTS (SELECT 1 FROM agendamento a "
                + "                  WHERE a.id_profissional = p.id AND a.data = ? AND a.hora = ?) "
                + "ORDER BY id LIMIT 1";
        List<Integer> livres = jdbc.queryForList(sqlLivre, Integer.class, data, hora);
        if (livres.isEmpty()) {
            return null;
        }
        int idProfissional = livres.get(0);

        try {
            jdbc.update("INSERT INTO agendamento (id_cliente, id_profissional, id_servico, data, hora) "
                    + "VALUES (?, ?, ?, ?, ?)", idCliente, idProfissional, idServico, data, hora);
        } catch (DuplicateKeyException e) {
            // Outra pessoa agendou o mesmo horário no mesmo instante
            return null;
        }

        return jdbc.queryForObject("SELECT nome_profissional FROM profissional WHERE id = ?",
                String.class, idProfissional);
    }

    public List<Agendamento> listarAgendamentosDoCliente(int idCliente) {
        String sql = "SELECT DATE_FORMAT(a.data, '%Y-%m-%d') AS data, TIME_FORMAT(a.hora, '%H:%i') AS hora, "
                + "       s.nome_servico, p.nome_profissional "
                + "FROM agendamento a "
                + "JOIN servico s ON s.id_servico = a.id_servico "
                + "JOIN profissional p ON p.id = a.id_profissional "
                + "WHERE a.id_cliente = ? "
                + "ORDER BY a.data DESC, a.hora DESC";
        return jdbc.query(sql, (rs, linha) -> new Agendamento(
                rs.getString("data"),
                rs.getString("hora"),
                rs.getString("nome_servico"),
                rs.getString("nome_profissional")), idCliente);
    }


    // ==================== Avaliações ====================

    public List<Avaliacao> listarAvaliacoes() {
        String sql = "SELECT c.nome_cliente, s.nome_servico, av.nota, av.comentario "
                + "FROM avaliacao av "
                + "JOIN cliente c ON c.id = av.id_cliente "
                + "JOIN servico s ON s.id_servico = av.id_servico "
                + "ORDER BY av.id DESC";
        return jdbc.query(sql, (rs, linha) -> new Avaliacao(
                abreviarNome(rs.getString("nome_cliente")),
                rs.getString("nome_servico"),
                rs.getInt("nota"),
                rs.getString("comentario")));
    }

    /** Média das notas, ou null se ainda não há avaliações. */
    public Double mediaDasNotas() {
        return jdbc.queryForObject("SELECT AVG(nota) FROM avaliacao", Double.class);
    }

    public void registrarAvaliacao(int idCliente, int idServico, int nota, String comentario) {
        jdbc.update("INSERT INTO avaliacao (nota, comentario, id_servico, id_cliente) VALUES (?, ?, ?, ?)",
                nota, vazioViraNull(comentario), idServico, idCliente);
    }


    // ==================== Ajudantes ====================

    /** "Mariana Souza" vira "Mariana S." (não mostra o nome completo no site). */
    private String abreviarNome(String nomeCompleto) {
        String[] partes = nomeCompleto.trim().split(" ");
        if (partes.length == 1) {
            return partes[0];
        }
        String ultimoNome = partes[partes.length - 1];
        return partes[0] + " " + ultimoNome.charAt(0) + ".";
    }

    /** Campo opcional deixado em branco é salvo como NULL no banco. */
    private String vazioViraNull(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto;
    }
}
