<%--
  Funções e dados usados por TODAS as páginas.
  Cada página inclui este arquivo logo na 2ª linha:
      <%@ include file="/WEB-INF/includes/funcoes.jsp" %>

  Partes:
    1. Dados da clínica
    2. Banco de dados (conexão, SELECT, INSERT/UPDATE e criação automática do banco)
    3. Agenda (horários de atendimento e vagas)
    4. Campos dos formulários
    5. Formatação (preço, datas, estrelas, proteger o HTML)
    6. Senha (criptografia)
--%>
<%@ page pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.ArrayList, java.util.Map, java.util.HashMap, java.util.Locale,
                 java.util.Properties, java.util.Base64,
                 java.io.InputStream, java.io.InputStreamReader, java.io.BufferedReader,
                 java.sql.Connection, java.sql.DriverManager, java.sql.PreparedStatement, java.sql.ResultSet,
                 java.sql.ResultSetMetaData, java.sql.SQLException, java.sql.SQLIntegrityConstraintViolationException,
                 java.time.LocalDate, java.time.LocalTime, java.time.LocalDateTime, java.time.YearMonth,
                 java.time.DayOfWeek, java.time.format.DateTimeFormatter,
                 java.text.NumberFormat, java.net.URLEncoder,
                 java.security.SecureRandom, java.security.MessageDigest,
                 javax.crypto.SecretKeyFactory, javax.crypto.spec.PBEKeySpec" %>
<%!
    // =====================================================================
    // 1. Dados da clínica (altere aqui)
    // =====================================================================

    static final String CLINICA_NOME = "Clínica Bella Estética";
    static final String CLINICA_DESCRICAO = "Cuidamos da sua beleza e do seu bem-estar com tratamentos faciais, "
            + "corporais e capilares realizados por profissionais especializados.";
    static final String CLINICA_WHATSAPP = "5511951772722";   // (11) 95177-2722 -> 55 + DDD + número
    static final String CLINICA_ENDERECO = "Av. Paulista, 1000 – Bela Vista";
    static final String CLINICA_CIDADE = "São Paulo – SP, 01310-100";
    static final String CLINICA_MAPA = "Avenida Paulista, 1000, São Paulo";   // o que é buscado no Google Maps


    // =====================================================================
    // 2. Banco de dados (MariaDB/MySQL do XAMPP)
    //
    //    consultar(...)    -> roda um SELECT e devolve as linhas
    //    primeiroValor(...) -> roda um SELECT que devolve um valor só (COUNT, AVG...)
    //    executar(...)     -> roda um INSERT ou UPDATE
    //
    //    Cada "?" do SQL é trocado por um valor, na ordem (isso evita SQL injection).
    //    "site" é o objeto "application" do JSP (usado para achar os arquivos dentro do WEB-INF).
    // =====================================================================

    static final String SERVIDOR = "jdbc:mariadb://localhost:3306/";
    static final String OPCOES = "?allowPublicKeyRetrieval=true";   // permite também entrar num MySQL 8

    /** Abre uma conexão com o banco clinica_estetica. Na primeira vez, cria o banco se ele ainda não existe. */
    Connection conectar(ServletContext site) throws Exception {
        Class.forName("org.mariadb.jdbc.Driver");   // driver do MariaDB (o .jar em WEB-INF/lib)

        // Usuário e senha do banco ficam em WEB-INF/banco.properties
        Properties config = new Properties();
        config.load(new InputStreamReader(site.getResourceAsStream("/WEB-INF/banco.properties"), "UTF-8"));
        String usuario = config.getProperty("usuario", "root").trim();
        String senha = config.getProperty("senha", "").trim();

        try {
            if (site.getAttribute("bancoPronto") == null) {
                criarBancoSeNaoExistir(site, usuario, senha);
                site.setAttribute("bancoPronto", "sim");   // não precisa conferir de novo
            }
            return DriverManager.getConnection(SERVIDOR + "clinica_estetica" + OPCOES, usuario, senha);
        } catch (SQLException e) {
            throw new Exception("Não consegui entrar no banco de dados. O MySQL está ligado no XAMPP? "
                    + "O usuário e a senha em WEB-INF/banco.properties estão certos? Detalhe: " + e.getMessage(), e);
        }
    }

    /** Se a tabela servico não existe, roda o WEB-INF/banco.sql (cria o banco, as tabelas e os dados de exemplo). */
    void criarBancoSeNaoExistir(ServletContext site, String usuario, String senha) throws Exception {
        synchronized (site) {   // se duas pessoas abrirem o site ao mesmo tempo, só uma cria o banco
            // Conecta sem escolher o banco, porque ele pode ainda não existir
            try (Connection conexao = DriverManager.getConnection(SERVIDOR + OPCOES, usuario, senha)) {
                ResultSet resultado = conexao.createStatement().executeQuery(
                        "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'clinica_estetica' AND table_name = 'servico'");
                resultado.next();
                if (resultado.getInt(1) > 0) {
                    return;   // o banco já existe: mantém os dados
                }

                System.out.println("[Clínica] Banco não existe: criando com o banco.sql...");
                InputStream arquivo = site.getResourceAsStream("/WEB-INF/banco.sql");
                BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo, "UTF-8"));
                String sql = "";
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    if (linha.trim().startsWith("--")) {
                        continue;   // comentário
                    }
                    sql += linha + "\n";
                    if (linha.trim().endsWith(";")) {   // fim de um comando
                        conexao.createStatement().execute(sql);
                        sql = "";
                    }
                }
                leitor.close();
                System.out.println("[Clínica] Banco criado com sucesso.");
            }
        }
    }

    /** Prepara o SQL trocando cada "?" por um valor, na ordem. */
    PreparedStatement preparar(Connection conexao, String sql, Object... valores) throws SQLException {
        PreparedStatement comando = conexao.prepareStatement(sql);
        for (int i = 0; i < valores.length; i++) {
            comando.setObject(i + 1, valores[i]);
        }
        return comando;
    }

    /**
     * Roda um SELECT. Cada linha do resultado vira um Map: nome da coluna -> valor.
     * Exemplo: linha.get("nome_servico") -> "Limpeza de pele"
     */
    List<Map<String, String>> consultar(ServletContext site, String sql, Object... valores) throws Exception {
        List<Map<String, String>> linhas = new ArrayList<Map<String, String>>();
        try (Connection conexao = conectar(site)) {
            ResultSet resultado = preparar(conexao, sql, valores).executeQuery();
            ResultSetMetaData colunas = resultado.getMetaData();
            while (resultado.next()) {
                Map<String, String> linha = new HashMap<String, String>();
                for (int i = 1; i <= colunas.getColumnCount(); i++) {
                    linha.put(colunas.getColumnLabel(i), resultado.getString(i));
                }
                linhas.add(linha);
            }
        }
        return linhas;
    }

    /** Roda um SELECT que devolve um valor só (ex.: COUNT ou AVG). Devolve null se não achar nada. */
    String primeiroValor(ServletContext site, String sql, Object... valores) throws Exception {
        try (Connection conexao = conectar(site)) {
            ResultSet resultado = preparar(conexao, sql, valores).executeQuery();
            if (resultado.next()) {
                return resultado.getString(1);
            }
        }
        return null;
    }

    /** Roda um INSERT ou UPDATE. */
    void executar(ServletContext site, String sql, Object... valores) throws Exception {
        try (Connection conexao = conectar(site)) {
            preparar(conexao, sql, valores).executeUpdate();
        }
    }

    /** O procedimento com esse id, ou null se não existir. */
    Map<String, String> buscarServico(ServletContext site, int id) throws Exception {
        List<Map<String, String>> linhas = consultar(site, "SELECT * FROM servico WHERE id_servico = ?", id);
        if (linhas.isEmpty()) {
            return null;
        }
        return linhas.get(0);
    }

    /** O cliente com esse e-mail, ou null se não existir. */
    Map<String, String> buscarCliente(ServletContext site, String email) throws Exception {
        List<Map<String, String>> linhas = consultar(site, "SELECT * FROM cliente WHERE email = ?", email.toLowerCase());
        if (linhas.isEmpty()) {
            return null;
        }
        return linhas.get(0);
    }

    /** Cadastra o cliente. Se o e-mail já existe, só atualiza o nome e os telefones. */
    void salvarCliente(ServletContext site, String nome, String whatsapp, String celular, String email) throws Exception {
        // Telefone em branco é salvo como NULL
        if (whatsapp.isEmpty()) {
            whatsapp = null;
        }
        if (celular.isEmpty()) {
            celular = null;
        }
        email = email.toLowerCase();

        if (buscarCliente(site, email) == null) {
            executar(site, "INSERT INTO cliente (nome_cliente, whatsapp, celular, email) VALUES (?, ?, ?, ?)",
                    nome, whatsapp, celular, email);
        } else {
            // COALESCE: se o telefone novo veio vazio, mantém o que já estava salvo
            executar(site, "UPDATE cliente SET nome_cliente = ?, whatsapp = COALESCE(?, whatsapp), "
                    + "celular = COALESCE(?, celular) WHERE email = ?", nome, whatsapp, celular, email);
        }
    }

    /** Guarda na sessão quem fez login (usado em conta.jsp e cadastro.jsp). */
    void guardarLogin(HttpServletRequest request, Map<String, String> cliente) {
        request.changeSessionId();   // troca o código da sessão ao entrar (proteção contra roubo de sessão)
        HttpSession sessao = request.getSession();
        sessao.setAttribute("usuarioId", Integer.parseInt(cliente.get("id")));
        sessao.setAttribute("usuarioNome", cliente.get("nome_cliente"));
        sessao.setAttribute("usuarioEmail", cliente.get("email"));
    }


    // =====================================================================
    // 3. Agenda
    //    - Seg a sex: 9h às 18h (pausa 12h-13h). Sábado: 9h às 13h. Domingo: fechado.
    //    - Um horário tem vaga enquanto houver algum profissional livre nele.
    //    - Dá para agendar do mês atual até 2 meses à frente.
    // =====================================================================

    static final int MESES_A_FRENTE = 2;
    static final String[] HORARIOS_SEMANA = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
    static final String[] HORARIOS_SABADO = {"09:00", "10:00", "11:00", "12:00"};
    static final String[] SEM_HORARIOS = {};

    /** Último mês em que já dá para agendar. */
    YearMonth ultimoMesLiberado() {
        return YearMonth.now().plusMonths(MESES_A_FRENTE);
    }

    /** Horários de atendimento do dia. Nenhum se for domingo, dia que já passou ou depois do período liberado. */
    String[] horariosDoDia(LocalDate dia) {
        if (dia.isBefore(LocalDate.now()) || dia.isAfter(ultimoMesLiberado().atEndOfMonth())) {
            return SEM_HORARIOS;
        }
        if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return SEM_HORARIOS;
        }
        if (dia.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return HORARIOS_SABADO;
        }
        return HORARIOS_SEMANA;
    }

    /**
     * Quantos atendimentos já existem em cada horário entre duas datas.
     * Exemplo: { "2026-12-01 09:00" = 2, "2026-12-01 10:00" = 1 }
     */
    Map<String, Integer> ocupacaoEntre(ServletContext site, LocalDate inicio, LocalDate fim) throws Exception {
        Map<String, Integer> ocupacao = new HashMap<String, Integer>();
        List<Map<String, String>> linhas = consultar(site,
                "SELECT CONCAT(data, ' ', TIME_FORMAT(hora, '%H:%i')) AS horario, COUNT(*) AS total "
                + "FROM agendamento WHERE data BETWEEN ? AND ? GROUP BY data, hora",
                inicio.toString(), fim.toString());
        for (Map<String, String> linha : linhas) {
            ocupacao.put(linha.get("horario"), Integer.parseInt(linha.get("total")));
        }
        return ocupacao;
    }

    int contarProfissionais(ServletContext site) throws Exception {
        return Integer.parseInt(primeiroValor(site, "SELECT COUNT(*) FROM profissional"));
    }

    /** O horário ainda tem vaga? (ainda não passou e tem profissional livre) */
    boolean horarioLivre(LocalDate dia, String hora, Map<String, Integer> ocupacao, int profissionais) {
        boolean jaPassou = dia.atTime(LocalTime.parse(hora)).isBefore(LocalDateTime.now());
        Integer ocupados = ocupacao.get(dia + " " + hora);
        if (ocupados == null) {
            ocupados = 0;
        }
        return !jaPassou && ocupados < profissionais;
    }

    /** O dia tem pelo menos um horário livre? */
    boolean diaTemVaga(LocalDate dia, Map<String, Integer> ocupacao, int profissionais) {
        for (String hora : horariosDoDia(dia)) {
            if (horarioLivre(dia, hora, ocupacao, profissionais)) {
                return true;
            }
        }
        return false;
    }


    // =====================================================================
    // 4. Campos dos formulários e do endereço da página
    // =====================================================================

    /** Valor do campo, sem espaços nas pontas ("" se não veio). */
    String texto(HttpServletRequest request, String nome) {
        String valor = request.getParameter(nome);
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }

    /** Campo com número. Ex.: "3" vira 3. Vazio ou inválido vira 0. */
    int numero(HttpServletRequest request, String nome) {
        try {
            return Integer.parseInt(texto(request, nome));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Campo com data no formato "2026-12-01". Vazio ou inválido vira null. */
    LocalDate dataDoCampo(HttpServletRequest request, String nome) {
        try {
            return LocalDate.parse(texto(request, nome));
        } catch (Exception e) {
            return null;
        }
    }


    // =====================================================================
    // 5. Formatação
    // =====================================================================

    static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    /**
     * Protege o texto antes de colocar no HTML. Assim, se alguém digitar "<script>" num comentário,
     * aparece o texto na tela em vez de virar código. Texto null vira "".
     */
    String html(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** Prepara um texto para ir dentro de um link. Ex.: "Olá mundo" -> "Ol%C3%A1%20mundo" */
    String url(String texto) throws Exception {
        return URLEncoder.encode(texto, "UTF-8").replace("+", "%20");
    }

    /** "150.00" -> "R$ 150,00" */
    String preco(String valor) {
        return NumberFormat.getCurrencyInstance(BRASIL).format(Double.parseDouble(valor));
    }

    /** 4.666 -> "4,7" */
    String media(double valor) {
        return String.format(BRASIL, "%.1f", valor);
    }

    /** "2026-12-01" -> "01/12/2026" */
    String dataCurta(String data) {
        return LocalDate.parse(data).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** "2026-12-01" -> "Terça-feira, 1 de dezembro" */
    String dataPorExtenso(String data) {
        String texto = LocalDate.parse(data).format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", BRASIL));
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    /** 2026-12 -> "Dezembro de 2026" */
    String mesPorExtenso(YearMonth mes) {
        String texto = mes.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", BRASIL));
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    /** 4 -> "★★★★☆" */
    String estrelas(int nota) {
        String texto = "";
        for (int i = 1; i <= 5; i++) {
            if (i <= nota) {
                texto += "★";
            } else {
                texto += "☆";
            }
        }
        return texto;
    }

    /** "Mariana Souza" -> "Mariana" */
    String primeiroNome(String nome) {
        return nome.trim().split(" ")[0];
    }

    /** "Mariana Souza" -> "Mariana S." (não mostra o nome completo no site) */
    String abreviarNome(String nome) {
        String[] partes = nome.trim().split(" ");
        if (partes.length == 1) {
            return partes[0];
        }
        return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
    }


    // =====================================================================
    // 6. Senha (PBKDF2, já vem no Java)
    //    A senha nunca é salva como texto: no banco fica "sal:resultado".
    //    O "sal" é um valor aleatório diferente para cada pessoa, assim duas senhas
    //    iguais ficam diferentes no banco.
    // =====================================================================

    String criptografarSenha(String senha) throws Exception {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal) + ":" + calcularSenha(senha, sal);
    }

    /** A senha digitada é a mesma que foi salva? */
    boolean senhaConfere(String senhaDigitada, String senhaSalva) throws Exception {
        if (senhaSalva == null || !senhaSalva.contains(":")) {
            return false;
        }
        String[] partes = senhaSalva.split(":");
        byte[] sal = Base64.getDecoder().decode(partes[0]);
        String resultado = calcularSenha(senhaDigitada, sal);
        return MessageDigest.isEqual(resultado.getBytes(), partes[1].getBytes());
    }

    String calcularSenha(String senha, byte[] sal) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, 100000, 256);
        SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return Base64.getEncoder().encodeToString(fabrica.generateSecret(spec).getEncoded());
    }
%>
<%
    // Roda no começo de toda página:
    request.setCharacterEncoding("UTF-8");   // para ler os acentos dos formulários

    // Quem fez login (null se ninguém entrou). Quem guarda é a função guardarLogin.
    Integer usuarioId = (Integer) session.getAttribute("usuarioId");
    String usuarioNome = (String) session.getAttribute("usuarioNome");
    String usuarioEmail = (String) session.getAttribute("usuarioEmail");

    // Erros dos formulários. A página adiciona com erros.add("...") e o erros.jsp mostra.
    List<String> erros = new ArrayList<String>();
%>
