package com.example.demo.associacao.application.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.demo.associacao.domain.AssociacaoDiscord;
import com.example.demo.associacao.infra.AssociacaoDiscordRepository;
import com.example.demo.comunicacao.infra.DiscordClient;
import com.example.demo.comunicacao.infra.DiscordConviteRequest;
import com.example.demo.comunicacao.infra.DiscordConviteResponse;
import com.example.demo.handler.APIException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class AssociacaoApplicationService implements AssociacaoService {
	private final AssociacaoDiscordRepository repository;
	private final DiscordClient discordClient;
	private final DiscordService discordService;
	
	@Override
	public String gerarOuObterLinkConvite() {
		log.info("[inicia] AssociacaoApplicationService - gerarOuObterLinkConvite");
		DiscordConviteRequest conviteRequest = new DiscordConviteRequest(1, true, 0);
		DiscordConviteResponse convite = discordClient.criaConviteDoCanalParaWakander(conviteRequest);
		String url = "https://discord.gg/" + convite.getCode();
		repository.save(new AssociacaoDiscord(convite.getCode()));
		log.info("[url]: {}", url);
		log.info("[finaliza] AssociacaoApplicationService - gerarOuObterLinkConvite");
		return "URL:   " + url + "         -         Token:   " + convite.getCode();
	}

	@Override
	public void associarUsuario(String username, String idDiscord, String token) {
		log.info("[inicia] AssociacaoApplicationService - associarUsuario");
		AssociacaoDiscord associacao = repository.findByToken(token)
				.orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Usuario não encontrado!"));
		associacao.validaSeJaFoiAssociado();
		associacao.associar(username, idDiscord);
		repository.save(associacao);
		discordService.atualizaCargoParaWakander(idDiscord);
		log.info("[finaliza] AssociacaoApplicationService - associarUsuario");
	}

	@Override
	public AssociacaoDiscord buscaPorToken(String token) {
		log.info("[inicia] AssociacaoApplicationService - buscaPorToken");
		AssociacaoDiscord associacao = repository.findByToken(token)
				.orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Associacao não encontrada"));
		return associacao;
	}

	@Override
	public List<AssociacaoDiscord> lista() {
		return repository.findAll();
	}

	@Override
	public void deleteAll() {
		log.info("[inicia] AssociacaoApplicationService - deleteAll");
		repository.deleteAll();
		log.info("[finaliza] AssociacaoApplicationService - deleteAll");
	}

	@Override
	public void desassociar(String token) {
		AssociacaoDiscord associacao = buscaPorToken(token);
		associacao.desassociar();
		repository.save(associacao);
	}

}