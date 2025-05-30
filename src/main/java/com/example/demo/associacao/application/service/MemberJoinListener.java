package com.example.demo.associacao.application.service;

import org.springframework.beans.factory.annotation.Autowired;
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
//    private final AssociacaoService associacaoService;
	@Value("${aws.url}")
	private String urlInstancia;
    private WebClient webClient;

    @Autowired
    public MemberJoinListener(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.build();
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        log.info("[inicia] MemberJoinListener - onGuildMemberJoin");

        Member member = event.getMember();
        User user = member.getUser();

        String username = user.getName();
        associarContaDiscord(username);
//        associacaoService.associarUsuario(username);
        log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");
    }
    
	public void associarContaDiscord(String username) {
		String uri = String.format(urlInstancia + "/api/associacao/%s/associar-discord", username);
		try {
			String response = webClient.patch()
					.uri(uri).retrieve()
					.onStatus(status -> 
						status.isError(),
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
//        String url = urlInstancia + String.format(
//            "/api/associacao/%s/%s/%s/associar-discord",
//            username, discordId, token
//        );
//
//        try {
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//
//            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenAccept(response -> log.info("Resposta da API: {}", response.body()))
//                .exceptionally(ex -> {
//                    log.error("Erro ao chamar API: {}", ex.getMessage());
//                    return null;
//                });
//        } catch (Exception e) {
//            log.error("Erro ao montar requisição HTTP", e);
//        }
//
//        String mensagem = "Olá " + username + "! 👋\nClique aqui para associar sua conta: " + url;
//        user.openPrivateChannel()
//            .flatMap(channel -> channel.sendMessage(mensagem))
//            .queue(
//                success -> log.info("Mensagem enviada para {}", username),
//                error -> log.warn("Erro ao enviar DM para {}: {}", username, error.getMessage())
//            );
//
//        log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");

//	@Override
//	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
//		log.info("[inicia] MemberJoinListener - onGuildMemberJoin");
//		Member member = event.getMember();
//		User user = member.getUser();
//
//		String username = user.getName();
//		String discordId = user.getId();
//
//		String token = associacaoService.gerarOuObterLinkConvite(username);
//		associacaoService.associarUsuario(username, discordId, token);
////		client.associarContaDiscord(username, discordId, token);
//		log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");
//	}

}
