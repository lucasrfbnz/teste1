package com.loja.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.loja.dto.ErrorResponse;
import com.loja.exception.ConflictException;
import com.loja.exception.NotFoundException;
import com.loja.exception.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseController implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    protected static final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // ensina o Jackson a ler LocalDate

    /** Cada controller implementa só isto; o try/catch abaixo cuida dos erros. */
    protected abstract void rotear(HttpExchange ex) throws IOException;

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            rotear(ex);
        } catch (NotFoundException e) {
            enviarErro(ex, 404, "Not Found", e.getMessage());
        } catch (ConflictException e) {
            enviarErro(ex, 409, "Conflict", e.getMessage());
        } catch (ValidationException | IllegalStateException | IllegalArgumentException e) {
            enviarErro(ex, 400, "Bad Request", e.getMessage());
        } catch (Throwable e) {
            log.error("Erro inesperado em {}", ex.getRequestURI(), e);
            enviarErro(ex, 500, "Internal Server Error", "Erro inesperado no servidor.");
        } finally {
            ex.close();
        }
    }

    /** Lê o corpo da requisição e desserializa no record informado. */
    protected <T> T lerCorpo(HttpExchange ex, Class<T> tipo) {
        try (InputStream is = ex.getRequestBody()) {
            return json.readValue(is, tipo);
        } catch (IOException e) {
            throw new ValidationException("Corpo da requisição não é um JSON válido.");
        }
    }

    /** Serializa o objeto e envia com o status informado. */
    protected void enviarJson(HttpExchange ex, int status, Object corpo) throws IOException {
        byte[] bytes = json.writeValueAsBytes(corpo);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    /** 204 No Content: resposta sem corpo. O -1 diz "não há body". */
    protected void enviarVazio(HttpExchange ex, int status) throws IOException {
        ex.sendResponseHeaders(status, -1);
    }

    protected void enviarErro(HttpExchange ex, int status, String erro, String msg) throws IOException {
        enviarJson(ex, status, new ErrorResponse(status, erro, msg));
    }

    /** Extrai o id de "/clientes/42" -> 42. Retorna null para "/clientes". */
    protected Integer idDoCaminho(HttpExchange ex) {
        String[] partes = ex.getRequestURI().getPath().split("/");
        if (partes.length < 3 || partes[2].isBlank()) return null;
        try {
            return Integer.parseInt(partes[2]);
        } catch (NumberFormatException e) {
            throw new ValidationException("O id informado no caminho não é um número.");
        }
    }

    protected void exigirId(Integer id, String recurso) {
        if (id == null) throw new ValidationException("Informe o id no caminho: /" + recurso + "/{id}");
    }
}
