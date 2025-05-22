package com.example.demo.associacao.comunicacao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class ComunicacaoInfraClient implements ComunicacaoClient {
	private WebClient webClient;
	@Value("${aws.url}")
	private String urlInstancia;
	
    public ComunicacaoInfraClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

	@Override
	public void associarContaDiscord(String username, String discordId, String token) {
		String uri = String.format(urlInstancia + "/api/associacao/%s/%s/%s/associar-discord", username,
				discordId, token);
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
}