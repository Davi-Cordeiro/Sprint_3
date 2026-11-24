package br.com.clinica.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ajudantes usados por todos os Servlets: ler o formulário que o JavaScript enviou
 * e responder em JSON. Exemplo de JSON: {"nome":"Limpeza de pele","preco":150.0}
 */
public class Json {

    /** Valor de um campo do formulário, sem espaços nas pontas ("" se não veio). */
    public static String parametro(HttpServletRequest request, String nome) {
        String valor = request.getParameter(nome);
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }

    /** "3" vira 3. Texto vazio ou inválido vira 0. */
    public static int numero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Coloca o texto entre aspas e protege os caracteres especiais. Ex.: Olá -> "Olá" */
    public static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        valor = valor.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replaceAll("[\\x00-\\x1F]", " ");   // outros caracteres invisíveis viram espaço
        return "\"" + valor + "\"";
    }

    /** Junta itens que já estão em JSON numa lista: [item1,item2,...] */
    public static String lista(List<String> itens) {
        return "[" + String.join(",", itens) + "]";
    }

    /** Resposta de erro: {"erros":["mensagem 1","mensagem 2"]} */
    public static String erros(List<String> mensagens) {
        List<String> itens = new ArrayList<>();
        for (String mensagem : mensagens) {
            itens.add(texto(mensagem));
        }
        return "{\"erros\":" + lista(itens) + "}";
    }

    public static String erro(String mensagem) {
        return erros(Arrays.asList(mensagem));
    }

    /** Envia o JSON como resposta para o navegador. */
    public static void enviar(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
