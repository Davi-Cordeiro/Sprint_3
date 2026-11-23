package br.com.clinica.repository;

import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Avaliacao;
import br.com.clinica.model.Cliente;
import br.com.clinica.model.Servico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Todo o acesso ao banco MySQL fica aqui (JDBC puro).
 *
 * Como funciona cada consulta:
 *   1. Banco.conectar()              -> abre a conexão
 *   2. conexao.prepareStatement(sql) -> prepara o SQL; os "?" são trocados com setString/setInt
 *   3. executeQuery()                -> roda o SELECT e devolve um ResultSet
 *   4. while (resultado.next())      -> passa por cada linha do resultado
 * O try ( ... ) fecha a conexão sozinho no final.
 */
public class ClinicaRepository {

    // ==================== Serviços ====================

    public List<Servico> listarServicos() {
        String sql = "SELECT id_servico, nome_servico, preco, descricao FROM servico ORDER BY id_servico";
        List<Servico> servicos = new ArrayList<>();

        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                servicos.add(new Servico(
                        resultado.getInt("id_servico"),
                        resultado.getString("nome_servico"),
                        resultado.getDouble("preco"),
                        resultado.getString("descricao")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar os serviços", e);
        }
        return servicos;
    }

    /** Devolve o serviço ou null se o id não existir. */
    public Servico buscarServico(int id) {
        String sql = "SELECT id_servico, nome_servico, preco, descricao FROM servico WHERE id_servico = ?";

        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            ResultSet resultado = comando.executeQuery();
            if (resultado.next()) {
                return new Servico(
                        resultado.getInt("id_servico"),
                        resultado.getString("nome_servico"),
                        resultado.getDouble("preco"),
                        resultado.getString("descricao"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o serviço", e);
        }
        return null;
    }

    public int contarProfissionais() {
        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement("SELECT COUNT(*) AS total FROM profissional");
             ResultSet resultado = comando.executeQuery()) {

            resultado.next();
            return resultado.getInt("total");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar os profissionais", e);
        }
    }


    // ==================== Clientes ====================

    /** Devolve o cliente com esse e-mail ou null se não existir. */
    public Cliente buscarClientePorEmail(String email) {
        String sql = "SELECT id, nome_cliente, email FROM cliente WHERE email = ?";

        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, email.toLowerCase());
            ResultSet resultado = comando.executeQuery();
            if (resultado.next()) {
                return new Cliente(
                        resultado.getInt("id"),
                        resultado.getString("nome_cliente"),
                        resultado.getString("email"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o cliente", e);
        }
        return null;
    }

    /** Senha (criptografada) do cliente, ou null se ele não existe ou nunca criou senha. */
    public String buscarSenha(String email) {
        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement("SELECT senha FROM cliente WHERE email = ?")) {

            comando.setString(1, email.toLowerCase());
            ResultSet resultado = comando.executeQuery();
            if (resultado.next()) {
                return resultado.getString("senha");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar a senha", e);
        }
        return null;
    }

    public void definirSenha(String email, String senhaCriptografada) {
        executar("UPDATE cliente SET senha = ? WHERE email = ?", senhaCriptografada, email.toLowerCase());
    }

    /**
     * Se o e-mail já está cadastrado, atualiza o nome e os telefones.
     * Se não está, cadastra um cliente novo.
     */
    public void salvarCliente(String nome, String whatsapp, String celular, String email) {
        if (buscarClientePorEmail(email) == null) {
            executar("INSERT INTO cliente (nome_cliente, whatsapp, celular, email) VALUES (?, ?, ?, ?)",
                    nome, vazioViraNull(whatsapp), vazioViraNull(celular), email.toLowerCase());
        } else {
            // COALESCE: se o telefone novo veio vazio, mantém o que já estava salvo
            executar("UPDATE cliente SET nome_cliente = ?, "
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

        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, dataInicio);
            comando.setString(2, dataFim);
            ResultSet resultado = comando.executeQuery();
            while (resultado.next()) {
                ocupacao.put(resultado.getString("horario"), resultado.getInt("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar a ocupação da agenda", e);
        }
        return ocupacao;
    }

    /**
     * Grava o agendamento com o primeiro profissional livre naquele horário.
     * Devolve o nome do profissional, ou null se não sobrou nenhum livre.
     */
    public String registrarAgendamento(int idCliente, int idServico, String data, String hora) {
        String sqlLivre = "SELECT id, nome_profissional FROM profissional p "
                + "WHERE NOT EXISTS (SELECT 1 FROM agendamento a "
                + "                  WHERE a.id_profissional = p.id AND a.data = ? AND a.hora = ?) "
                + "ORDER BY id LIMIT 1";
        String sqlInserir = "INSERT INTO agendamento (id_cliente, id_profissional, id_servico, data, hora) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexao = Banco.conectar()) {
            // 1. Procura um profissional livre
            PreparedStatement procurar = conexao.prepareStatement(sqlLivre);
            procurar.setString(1, data);
            procurar.setString(2, hora);
            ResultSet livre = procurar.executeQuery();
            if (!livre.next()) {
                return null;   // todos os profissionais estão ocupados
            }
            int idProfissional = livre.getInt("id");
            String nomeProfissional = livre.getString("nome_profissional");

            // 2. Grava o agendamento
            PreparedStatement inserir = conexao.prepareStatement(sqlInserir);
            inserir.setInt(1, idCliente);
            inserir.setInt(2, idProfissional);
            inserir.setInt(3, idServico);
            inserir.setString(4, data);
            inserir.setString(5, hora);
            inserir.executeUpdate();

            return nomeProfissional;
        } catch (SQLIntegrityConstraintViolationException e) {
            // Outra pessoa agendou o mesmo horário no mesmo instante
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gravar o agendamento", e);
        }
    }

    public List<Agendamento> listarAgendamentosDoCliente(int idCliente) {
        String sql = "SELECT DATE_FORMAT(a.data, '%Y-%m-%d') AS data, TIME_FORMAT(a.hora, '%H:%i') AS hora, "
                + "       s.nome_servico, p.nome_profissional "
                + "FROM agendamento a "
                + "JOIN servico s ON s.id_servico = a.id_servico "
                + "JOIN profissional p ON p.id = a.id_profissional "
                + "WHERE a.id_cliente = ? "
                + "ORDER BY a.data DESC, a.hora DESC";
        List<Agendamento> agendamentos = new ArrayList<>();

        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, idCliente);
            ResultSet resultado = comando.executeQuery();
            while (resultado.next()) {
                agendamentos.add(new Agendamento(
                        resultado.getString("data"),
                        resultado.getString("hora"),
                        resultado.getString("nome_servico"),
                        resultado.getString("nome_profissional")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar os agendamentos", e);
        }
        return agendamentos;
    }


    // ==================== Avaliações ====================

    public List<Avaliacao> listarAvaliacoes() {
        String sql = "SELECT c.nome_cliente, s.nome_servico, av.nota, av.comentario "
                + "FROM avaliacao av "
                + "JOIN cliente c ON c.id = av.id_cliente "
                + "JOIN servico s ON s.id_servico = av.id_servico "
                + "ORDER BY av.id DESC";
        List<Avaliacao> avaliacoes = new ArrayList<>();

        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                avaliacoes.add(new Avaliacao(
                        abreviarNome(resultado.getString("nome_cliente")),
                        resultado.getString("nome_servico"),
                        resultado.getInt("nota"),
                        resultado.getString("comentario")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar as avaliações", e);
        }
        return avaliacoes;
    }

    /** Média das notas, ou null se ainda não há avaliações. */
    public Double mediaDasNotas() {
        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(
                     "SELECT COUNT(*) AS total, AVG(nota) AS media FROM avaliacao");
             ResultSet resultado = comando.executeQuery()) {

            resultado.next();
            if (resultado.getInt("total") == 0) {
                return null;
            }
            return resultado.getDouble("media");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular a média", e);
        }
    }

    public void registrarAvaliacao(int idCliente, int idServico, int nota, String comentario) {
        executar("INSERT INTO avaliacao (nota, comentario, id_servico, id_cliente) VALUES (?, ?, ?, ?)",
                nota, vazioViraNull(comentario), idServico, idCliente);
    }


    // ==================== Ajudantes ====================

    /** Roda um INSERT ou UPDATE. Os valores entram no lugar dos "?", na ordem. */
    private void executar(String sql, Object... valores) {
        try (Connection conexao = Banco.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            for (int i = 0; i < valores.length; i++) {
                comando.setObject(i + 1, valores[i]);
            }
            comando.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gravar no banco", e);
        }
    }

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
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return texto;
    }
}
