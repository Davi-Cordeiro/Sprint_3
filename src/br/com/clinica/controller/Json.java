package br.com.clinica.controller;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ajudante para montar as respostas em JSON que o JavaScript lê.
 * Exemplo de JSON: {"nome": "Limpeza de pele", "preco": 150.0}
 */
public class Json {

    /** Coloca o texto entre aspas e protege os caracteres especiais. Ex.: Olá -> "Olá" */
    public static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        StringBuilder json = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            if (c == '"') {
                json.append("\\\"");
            } else if (c == '\\') {
                json.append("\\\\");
            } else if (c == '\n') {
                json.append("\\n");
            } else if (c == '\r') {
                json.append("\\r");
            } else if (c < 32) {
                json.append(' ');   // outros caracteres de controle viram espaço
            } else {
                json.append(c);
            }
        }
        json.append('"');
        return json.toString();
    }

    /** Junta itens que já estão em JSON numa lista: [item1,item2,...] */
    public static String lista(List<String> itens) {
        return "[" + String.join(",", itens) + "]";
    }

    /** Resposta de erro: {"erros": ["mensagem 1", "mensagem 2"]} */
    public static String erros(List<String> mensagens) {
        List<String> itens = new ArrayList<>();
        for (String mensagem : mensagens) {
            itens.add(texto(mensagem));
        }
        return "{\"erros\":" + lista(itens) + "}";
    }

    public static String erro(String mensagem) {
        List<String> mensagens = new ArrayList<>();
        mensagens.add(mensagem);
        return erros(mensagens);
    }

    /** Envia o JSON como resposta para o navegador. */
    public static void enviar(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
