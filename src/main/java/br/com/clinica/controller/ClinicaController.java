package br.com.clinica.controller;

import br.com.clinica.model.Cliente;
import br.com.clinica.model.Servico;
import br.com.clinica.repository.ClinicaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dados gerais do site: informações da clínica, procedimentos e avaliações.
 * Cada método responde a um endereço /api/... e devolve JSON para o JavaScript.
 */
@RestController
public class ClinicaController {

    private final ClinicaRepository repo;

    // Valores do application.yml (bloco "clinica")
    @Value("${clinica.nome}")       private String nome;
    @Value("${clinica.descricao}")  private String descricao;
    @Value("${clinica.whatsapp}")   private String whatsapp;
    @Value("${clinica.endereco}")   private String endereco;
    @Value("${clinica.cidade}")     private String cidade;
    @Value("${clinica.mapa-busca}") private String mapaBusca;

    public ClinicaController(ClinicaRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/clinica")
    public Map<String, String> clinica() {
        Map<String, String> dados = new HashMap<>();
        dados.put("nome", nome);
        dados.put("descricao", descricao);
        dados.put("whatsapp", whatsapp);
        dados.put("endereco", endereco);
        dados.put("cidade", cidade);
        dados.put("mapaBusca", mapaBusca);
        return dados;
    }

    @GetMapping("/api/servicos")
    public List<Servico> servicos() {
        return repo.listarServicos();
    }

    @GetMapping("/api/servicos/{id}")
    public ResponseEntity<Object> servico(@PathVariable int id) {
        Servico servico = repo.buscarServico(id);
        if (servico == null) {
            return ResponseEntity.status(404).body(Map.of("erros", List.of("Procedimento não encontrado.")));
        }
        return ResponseEntity.ok(servico);
    }

    @GetMapping("/api/avaliacoes")
    public Map<String, Object> avaliacoes() {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("media", repo.mediaDasNotas());   // null quando ainda não há avaliações
        resposta.put("avaliacoes", repo.listarAvaliacoes());
        return resposta;
    }

    /** Recebe do JavaScript: nome, email, idServico, nota e comentario. */
    @PostMapping("/api/avaliacoes")
    public ResponseEntity<Object> avaliar(@RequestBody Map<String, String> dados) {
        String nomeCliente = dados.getOrDefault("nome", "").trim();
        String email = dados.getOrDefault("email", "").trim();
        String comentario = dados.getOrDefault("comentario", "").trim();
        int idServico = paraNumero(dados.get("idServico"));
        int nota = paraNumero(dados.get("nota"));

        // Confere os dados
        List<String> erros = new ArrayList<>();
        if (nomeCliente.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }
        if (repo.buscarServico(idServico) == null) {
            erros.add("Escolha o procedimento que você fez.");
        }
        if (nota < 1 || nota > 5) {
            erros.add("Escolha uma nota de 1 a 5 estrelas.");
        }
        if (comentario.length() > 500) {
            erros.add("O comentário pode ter no máximo 500 caracteres.");
        }
        if (!erros.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("erros", erros));
        }

        // Se a pessoa ainda não é cliente, cadastra só com nome e e-mail
        Cliente cliente = repo.buscarClientePorEmail(email);
        if (cliente == null) {
            repo.salvarCliente(nomeCliente, null, null, email);
            cliente = repo.buscarClientePorEmail(email);
        }

        repo.registrarAvaliacao(cliente.getId(), idServico, nota, comentario);
        return ResponseEntity.ok(Map.of("mensagem", "Avaliação registrada."));
    }

    /** "3" vira 3. Texto vazio ou inválido vira 0. */
    private int paraNumero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
