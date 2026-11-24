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
 * Todo o SQL do site fica aqui (JDBC).
 *
 * Toda consulta segue o mesmo caminho:
 *   1. Banco.conectar()            -> abre a conexão
 *   2. consultar(conexao, sql, ...) -> roda o SELECT, trocando cada "?" por um valor (evita SQL injection)
 *   3. while (resultado.next())    -> passa por cada linha do resultado
 * O try ( ... ) fecha a conexão sozinho no final.
 */
public class ClinicaRepository {

    // ==================== Serviços ====================

    public List<Servico> listarServicos() {
        List<Servico> servicos = new ArrayList<>();
        try (Connection conexao = Banco.conectar()) {
            ResultSet resultado = consultar(conexao, "SELECT * FROM servico ORDER BY id_servico");
            while (resultado.next()) {
                servicos.add(new Servico(resultado.getInt("id_servico"), resultado.getString("nome_servico"),
                        resultado.getDouble("preco"), resultado.getString("descricao")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return servicos;
    }

    /** O serviço com esse id, ou null se não existir. */
    public Servico buscarServico(int id) {
        try (Connection conexao = Banco.conectar()) {
            ResultSet resultado = consultar(conexao, "SELECT * FROM servico WHERE id_servico = ?", id);
            if (resultado.next()) {
                return new Servico(resultado.getInt("id_servico"), resultado.getString("nome_servico"),
                        resultado.getDouble("preco"), resultado.getString("descricao"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public int contarProfissionais() {
        return Integer.parseInt(primeiroValor("SELECT COUNT(*) FROM profissional"));
    }


    // ==================== Clientes ====================

    /** O cliente com esse e-mail, ou null se não existir. */
    public Cliente buscarClientePorEmail(String email) {
        try (Connection conexao = Banco.conectar()) {
            ResultSet resultado = consultar(conexao, "SELECT * FROM cliente WHERE email = ?", email.toLowerCase());
            if (resultado.next()) {
                return new Cliente(resultado.getInt("id"), resultado.getString("nome_cliente"), resultado.getString("email"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /** Senha (criptografada) do cliente, ou null se ele não existe ou nunca criou senha. */
    public String buscarSenha(String email) {
        return primeiroValor("SELECT senha FROM cliente WHERE email = ?", email.toLowerCase());
    }

    public void definirSenha(String email, String senhaCriptografada) {
        executar("UPDATE cliente SET senha = ? WHERE email = ?", senhaCriptografada, email.toLowerCase());
    }

    /** Cadastra o cliente. Se o e-mail já existe, só atualiza o nome e os telefones. */
    public void salvarCliente(String nome, String whatsapp, String celular, String email) {
        whatsapp = vazioViraNull(whatsapp);
        celular = vazioViraNull(celular);
        email = email.toLowerCase();

        if (buscarClientePorEmail(email) == null) {
            executar("INSERT INTO cliente (nome_cliente, whatsapp, celular, email) VALUES (?, ?, ?, ?)",
                    nome, whatsapp, celular, email);
        } else {
            // COALESCE: se o telefone novo veio vazio, mantém o que já estava salvo
            executar("UPDATE cliente SET nome_cliente = ?, whatsapp = COALESCE(?, whatsapp), "
                    + "celular = COALESCE(?, celular) WHERE email = ?", nome, whatsapp, celular, email);
        }
    }


    // ==================== Agendamentos ====================

    /**
     * Quantos atendimentos já existem em cada horário entre duas datas.
     * Exemplo: { "2026-12-01 09:00" = 2, "2026-12-01 10:00" = 1 }
     */
    public Map<String, Integer> ocupacaoEntre(String dataInicio, String dataFim) {
        Map<String, Integer> ocupacao = new HashMap<>();
        try (Connection conexao = Banco.conectar()) {
            ResultSet resultado = consultar(conexao,
                    "SELECT CONCAT(data, ' ', TIME_FORMAT(hora, '%H:%i')) AS horario, COUNT(*) AS total "
                            + "FROM agendamento WHERE data BETWEEN ? AND ? GROUP BY data, hora",
                    dataInicio, dataFim);
            while (resultado.next()) {
                ocupacao.put(resultado.getString("horario"), resultado.getInt("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ocupacao;
    }

    /**
     * Grava o agendamento com o primeiro profissional livre naquele horário.
     * Devolve o nome do profissional, ou null se não sobrou nenhum livre.
     */
    public String registrarAgendamento(int idCliente, int idServico, String data, String hora) {
        try (Connection conexao = Banco.conectar()) {
            // 1. Procura um profissional que não tem agendamento nesse dia e hora
            ResultSet livre = consultar(conexao,
                    "SELECT * FROM profissional p WHERE NOT EXISTS (SELECT 1 FROM agendamento a "
                            + "WHERE a.id_profissional = p.id AND a.data = ? AND a.hora = ?) ORDER BY id LIMIT 1",
                    data, hora);
            if (!livre.next()) {
                return null;   // todos estão ocupados
            }

            // 2. Grava o agendamento com ele
            preparar(conexao, "INSERT INTO agendamento (id_cliente, id_profissional, id_servico, data, hora) "
                    + "VALUES (?, ?, ?, ?, ?)", idCliente, livre.getInt("id"), idServico, data, hora).executeUpdate();
            return livre.getString("nome_profissional");
        } catch (SQLIntegrityConstraintViolationException e) {
            return null;   // outra pessoa agendou o mesmo horário no mesmo instante
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Agendamento> listarAgendamentosDoCliente(int idCliente) {
        List<Agendamento> agendamentos = new ArrayList<>();
        try (Connection conexao = Banco.conectar()) {
            ResultSet resultado = consultar(conexao,
                    "SELECT a.data, TIME_FORMAT(a.hora, '%H:%i') AS hora, s.nome_servico, p.nome_profissional "
                            + "FROM agendamento a "
                            + "JOIN servico s ON s.id_servico = a.id_servico "
                            + "JOIN profissional p ON p.id = a.id_profissional "
                            + "WHERE a.id_cliente = ? ORDER BY a.data DESC, a.hora DESC",
                    idCliente);
            while (resultado.next()) {
                agendamentos.add(new Agendamento(resultado.getString("data"), resultado.getString("hora"),
                        resultado.getString("nome_servico"), resultado.getString("nome_profissional")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return agendamentos;
    }


    // ==================== Avaliações ====================

    public List<Avaliacao> listarAvaliacoes() {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        try (Connection conexao = Banco.conectar()) {
            ResultSet resultado = consultar(conexao,
                    "SELECT c.nome_cliente, s.nome_servico, av.nota, av.comentario FROM avaliacao av "
                            + "JOIN cliente c ON c.id = av.id_cliente "
                            + "JOIN servico s ON s.id_servico = av.id_servico "
                            + "ORDER BY av.id DESC");
            while (resultado.next()) {
                avaliacoes.add(new Avaliacao(abreviarNome(resultado.getString("nome_cliente")),
                        resultado.getString("nome_servico"), resultado.getInt("nota"), resultado.getString("comentario")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return avaliacoes;
    }

    /** Média das notas, ou null se ainda não há avaliações. */
    public Double mediaDasNotas() {
        String media = primeiroValor("SELECT AVG(nota) FROM avaliacao");
        if (media == null) {
            return null;
        }
        return Double.parseDouble(media);
    }

    public void registrarAvaliacao(int idCliente, int idServico, int nota, String comentario) {
        executar("INSERT INTO avaliacao (nota, comentario, id_servico, id_cliente) VALUES (?, ?, ?, ?)",
                nota, vazioViraNull(comentario), idServico, idCliente);
    }


    // ==================== Ajudantes ====================

    /** Prepara o SQL trocando cada "?" por um valor, na ordem. */
    private PreparedStatement preparar(Connection conexao, String sql, Object... valores) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(sql);
        for (int i = 0; i < valores.length; i++) {
            comando.setObject(i + 1, valores[i]);
        }
        return comando;
    }

    /** Roda um SELECT. */
    private ResultSet consultar(Connection conexao, String sql, Object... valores) throws SQLException {
        return preparar(conexao, sql, valores).executeQuery();
    }

    /** Roda um SELECT que devolve um valor só (ex.: COUNT ou AVG). Devolve null se não achar nada. */
    private String primeiroValor(String sql, Object... valores) {
        try (Connection conexao = Banco.conectar()) {
            ResultSet resultado = consultar(conexao, sql, valores);
            if (resultado.next()) {
                return resultado.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /** Roda um INSERT ou UPDATE. */
    private void executar(String sql, Object... valores) {
        try (Connection conexao = Banco.conectar()) {
            preparar(conexao, sql, valores).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** "Mariana Souza" vira "Mariana S." (não mostra o nome completo no site). */
    private String abreviarNome(String nomeCompleto) {
        String[] partes = nomeCompleto.trim().split(" ");
        if (partes.length == 1) {
            return partes[0];
        }
        return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
    }

    /** Campo opcional deixado em branco é salvo como NULL no banco. */
    private String vazioViraNull(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return texto;
    }
}
