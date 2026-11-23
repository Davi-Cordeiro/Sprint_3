package br.com.clinica.controller;

import br.com.clinica.model.Cliente;
import br.com.clinica.repository.ClinicaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Login dos clientes.
 * - A senha nunca é salva como texto: vai para o banco criptografada com BCrypt.
 * - Quem fez login fica guardado na sessão (HttpSession) com o nome "usuario".
 */
@RestController
public class ContaController {

    private final ClinicaRepository repo;
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public ContaController(ClinicaRepository repo) {
        this.repo = repo;
    }

    /** Quem está logado e seus agendamentos. Se ninguém estiver logado: {"cliente": null} */
    @GetMapping("/api/conta")
    public Map<String, Object> minhaConta(HttpSession sessao) {
        Cliente usuario = (Cliente) sessao.getAttribute("usuario");

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("cliente", usuario);
        if (usuario != null) {
            resposta.put("agendamentos", repo.listarAgendamentosDoCliente(usuario.getId()));
        }
        return resposta;
    }

    /** Recebe do JavaScript: email e senha. */
    @PostMapping("/api/conta/entrar")
    public ResponseEntity<Object> entrar(@RequestBody Map<String, String> dados, HttpServletRequest request) {
        String email = dados.getOrDefault("email", "").trim();
        String senha = dados.getOrDefault("senha", "");

        String senhaSalva = repo.buscarSenha(email);
        if (senhaSalva == null || !bcrypt.matches(senha, senhaSalva)) {
            return ResponseEntity.status(401).body(Map.of("erros", List.of("E-mail ou senha incorretos.")));
        }

        Cliente cliente = repo.buscarClientePorEmail(email);
        guardarNaSessao(request, cliente);
        return ResponseEntity.ok(cliente);
    }

    /** Recebe do JavaScript: nome, whatsapp, email, senha e confirmacao. */
    @PostMapping("/api/conta/cadastro")
    public ResponseEntity<Object> cadastrar(@RequestBody Map<String, String> dados, HttpServletRequest request) {
        String nome = dados.getOrDefault("nome", "").trim();
        String whatsapp = dados.getOrDefault("whatsapp", "").trim();
        String email = dados.getOrDefault("email", "").trim();
        String senha = dados.getOrDefault("senha", "");
        String confirmacao = dados.getOrDefault("confirmacao", "");

        // Confere os dados
        List<String> erros = new ArrayList<>();
        if (nome.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }
        if (senha.length() < 6 || senha.length() > 72) {
            erros.add("A senha precisa ter entre 6 e 72 caracteres.");
        }
        if (!senha.equals(confirmacao)) {
            erros.add("As senhas não são iguais.");
        }
        if (erros.isEmpty() && repo.buscarSenha(email) != null) {
            erros.add("Já existe uma conta com esse e-mail. Faça login.");
        }
        if (!erros.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("erros", erros));
        }

        // Se a pessoa já agendou antes (cliente sem senha), aproveita o mesmo cadastro
        repo.salvarCliente(nome, whatsapp, null, email);
        repo.definirSenha(email, bcrypt.encode(senha));

        Cliente cliente = repo.buscarClientePorEmail(email);
        guardarNaSessao(request, cliente);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping("/api/conta/sair")
    public Map<String, String> sair(HttpSession sessao) {
        sessao.invalidate();
        return Map.of("mensagem", "Você saiu da conta.");
    }

    private void guardarNaSessao(HttpServletRequest request, Cliente cliente) {
        HttpSession sessao = request.getSession();
        request.changeSessionId();   // troca o código da sessão ao entrar (proteção contra roubo de sessão)
        sessao.setAttribute("usuario", cliente);
    }
}
