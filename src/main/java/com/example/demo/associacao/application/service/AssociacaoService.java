package com.example.demo.associacao.application.service;

public interface AssociacaoService {
	void associarUsuario(String username, String discordId, String token);
	String gerarOuObterLinkConvite(String nome);
}