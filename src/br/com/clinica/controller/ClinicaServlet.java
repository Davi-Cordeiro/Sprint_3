package br.com.clinica.controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Endereço api/clinica: dados da clínica mostrados no site (nome, endereço, WhatsApp...). */
@WebServlet("/api/clinica")
public class ClinicaServlet extends HttpServlet {

    // ======== Dados da clínica (altere aqui) ========
    private static final String NOME = "Clínica Bella Estética";
    private static final String DESCRICAO = "Cuidamos da sua beleza e do seu bem-estar com tratamentos faciais, "
            + "corporais e capilares realizados por profissionais especializados.";
    private static final String WHATSAPP = "5511951772722";   // (11) 95177-2722 -> 55 + DDD + número
    private static final String ENDERECO = "Av. Paulista, 1000 – Bela Vista";
    private static final String CIDADE = "São Paulo – SP, 01310-100";
    private static final String MAPA_BUSCA = "Avenida Paulista, 1000, São Paulo";
    // =================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String json = "{\"nome\":" + Json.texto(NOME)
                + ",\"descricao\":" + Json.texto(DESCRICAO)
                + ",\"whatsapp\":" + Json.texto(WHATSAPP)
                + ",\"endereco\":" + Json.texto(ENDERECO)
                + ",\"cidade\":" + Json.texto(CIDADE)
                + ",\"mapaBusca\":" + Json.texto(MAPA_BUSCA) + "}";
        Json.enviar(response, json);
    }
}
