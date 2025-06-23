package com.example.demo.associacao.application.service;

import java.util.List;

import com.example.demo.associacao.domain.AssociacaoDiscord;

public interface AssociacaoService {
	void associarUsuario(String nome, String IdDiscord);
	String gerarOuObterLinkConvite(String nome);
	AssociacaoDiscord buscaPorToken(String token);
	List<AssociacaoDiscord> lista();
}