package com.example.demo.config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import com.example.demo.associacao.application.service.MemberJoinListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DiscordBotConfig implements ApplicationListener<ApplicationReadyEvent> {
    private final MemberJoinListener memberJoinListener;

    @Value("${discord.bot.token}")
    private String botToken;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        log.info("[start] DiscordBotConfig - onApplicationEvent");
        log.debug("Aguardando 5 segundos para iniciar o bot Discord...");

        CompletableFuture.runAsync(() -> {
            try {
                createJdaInstance();
            } catch (Exception e) {
                log.error("[ERRO] Falha ao criar e iniciar o bot Discord (JDA): {}", e.getMessage(), e);
            }
        }, CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS));
    }

    private void createJdaInstance() {
        log.debug("[start] DiscordBotConfig - createJdaInstance");

        JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(memberJoinListener)
                .addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onReady(ReadyEvent event) {
                        log.info("✅ JDA iniciado com sucesso - Conectado como: {}", event.getJDA().getSelfUser().getAsTag());
                    }
                })
                .build();
    }
}