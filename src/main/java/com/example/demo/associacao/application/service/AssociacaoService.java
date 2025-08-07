package com.example.demo.associacao.application.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;

import com.example.demo.associacao.application.api.TokenResponse;
import com.example.demo.associacao.domain.AssociacaoDiscord;

public interface AssociacaoService {
	void associarUsuario(String nome, String IdDiscord, String token);
	TokenResponse gerarOuObterLinkConvite();
	AssociacaoDiscord buscaPorToken(String token);
	List<AssociacaoDiscord> lista();
	void deleteAll();
	void desassociar(String token);
	void substituirCargoDefaultPorWakander(String idDiscord);
	CompletableFuture<Integer> contaQuantosSemCargoExistem();
}