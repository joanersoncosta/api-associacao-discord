package com.example.demo.associacao.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class MemberJoinListener extends ListenerAdapter {
	private final AssociacaoService associacaoService;
	private final WebClient webClient;
	@Value("${aws.url}")
	private String urlInstancia;

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		log.info("[inicia] MemberJoinListener - onGuildMemberJoin");
		Member member = event.getMember();
		User user = member.getUser();

		String username = user.getName();
		String discordId = user.getId();

		String token = associacaoService.gerarOuObterLinkConvite(username);
		associarContaDiscord(username, discordId, token);
		log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");
	}

	public void associarContaDiscord(String username, String discordId, String token) {
		String uri = String.format(urlInstancia + "/api/associacao/%s/%s/%s/associar-discord", username,
				discordId, token);
		try {
			String response = webClient.patch()
					.uri(uri).retrieve()
					.onStatus(status -> status.isError(),
							clientResponse -> Mono.error(new RuntimeException("Erro HTTP: " + clientResponse.statusCode())))
					.bodyToMono(String.class).block();
			log.info("✅ Associação feita com sucesso: {}", response);

		} catch (WebClientResponseException e) {
			String errorMessage = e.getResponseBodyAsString();
			log.error("❌ Erro HTTP ao associar conta Discord: {}", errorMessage);
			throw new RuntimeException("Erro ao associar conta: " + e.getMessage(), e);

		} catch (Exception e) {
			log.error("❌ Erro inesperado ao associar conta Discord: {}", e.getMessage());
			throw new RuntimeException("Erro inesperado ao associar conta.", e);
		}
	}

}
