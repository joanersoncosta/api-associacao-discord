package com.example.demo.associacao.comunicacao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DiscordClient implements DiscordService {
    @Value("${discord.channel.id}")
    private String idChannel;

    @Value("${discord.bot.token}")
    private String botToken;

    private final WebClient webClient;

    public DiscordClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public DiscordConviteResponse criaConviteDoCanalParaWakander(DiscordConviteRequest conviteRequest) {
        String url = "https://discord.com/api/v10/channels/" + idChannel + "/invites";
        DiscordConviteResponse response;
        try {
            response = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bot " +botToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(conviteRequest)
                    .retrieve()
                    .bodyToMono(DiscordConviteResponse.class)
                    .block();
            log.info("Convite criado com sucesso!");
        } catch (WebClientResponseException e) {
            String errorMessage = e.getResponseBodyAsString();
            log.error("[error] Erro ao criar convite: {}", errorMessage);
            throw new RuntimeException("Erro ao criar convite: " + e.getMessage());
        } catch (Exception e) {
            log.error("[error] Erro inesperado ao criar convite: {}", e.getMessage());
            throw new RuntimeException("Erro inesperado ao criar convite.");
        }
        return response;
    }
} 