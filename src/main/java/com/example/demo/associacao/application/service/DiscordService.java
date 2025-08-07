package com.example.demo.associacao.application.service;

import java.util.concurrent.CompletableFuture;

public interface DiscordService {
	void atualizaCargoParaWakander(String idDiscord);
	void substituirCargoDefaultPorWakander(String idDiscord);
	CompletableFuture<Integer> contaQuantosSemCargoExistem();
}