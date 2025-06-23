package com.example.demo.associacao.application.service;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.demo.associacao.comunicacao.DiscordClient;
import com.example.demo.associacao.comunicacao.DiscordConviteRequest;
import com.example.demo.associacao.comunicacao.DiscordConviteResponse;
import com.example.demo.associacao.domain.AssociacaoDiscord;
import com.example.demo.associacao.handler.APIException;
import com.example.demo.associacao.infra.AssociacaoDiscordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class AssociacaoApplicationService implements AssociacaoService {
	private final AssociacaoDiscordRepository repository;
	private final DiscordClient discordClient;

	@Override
	public String gerarOuObterLinkConvite(String nome) {
		log.info("[inicia] AssociacaoApplicationService - gerarOuObterLinkConvite");
		AssociacaoDiscord associacao = repository.findByNomeUsuario(nome).orElseGet(new Supplier<AssociacaoDiscord>() {
			@Override
			public AssociacaoDiscord get() {
				AssociacaoDiscord nova = new AssociacaoDiscord(nome);
				return repository.save(nova);
			}
		});

		DiscordConviteRequest conviteRequest = new DiscordConviteRequest(1, true, 0);
		DiscordConviteResponse convite = discordClient.criaConviteDoCanalParaWakander(conviteRequest);

		associacao.editarToken(convite.getCode());
		repository.save(associacao);

		String url = "https://discord.gg/" + convite.getCode() + "?token=" + associacao.getToken();
		log.info("[url]: {}", url);
		log.info("[finaliza] AssociacaoApplicationService - gerarOuObterLinkConvite");
		return convite.getCode();
	}

	@Override
	public void associarUsuario(String username, String idDiscord) {
		log.info("[inicia] AssociacaoApplicationService - associarUsuario");
		AssociacaoDiscord associacao = repository.findByNomeUsuario(username).orElseGet(new Supplier<AssociacaoDiscord>() {
			@Override
			public AssociacaoDiscord get() {
				AssociacaoDiscord nova = new AssociacaoDiscord(username);
				return repository.save(nova);
			}
		});
		associacao.validaSeJaFoiAssociado();
		associacao.associar(idDiscord);
		repository.save(associacao);
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

}