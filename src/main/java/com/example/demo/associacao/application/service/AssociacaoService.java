package com.example.demo.associacao.application.service;

import com.example.demo.associacao.domain.AssociacaoDiscord;

public interface AssociacaoService {
	void associarUsuario(String username, String discordId, String token);
	String gerarOuObterLinkConvite(String nome);
	AssociacaoDiscord buscaPorToken(String token);
}