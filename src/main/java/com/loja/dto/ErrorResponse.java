package com.loja.dto;

/** Formato único e previsível de erro — todo endpoint usa este mesmo record. */
public record ErrorResponse(int status, String erro, String mensagem) {}
