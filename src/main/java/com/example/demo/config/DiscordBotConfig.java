package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.example.demo.associacao.application.service.MemberJoinListener;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@Log4j2
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class DiscordBotConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    private final MemberJoinListener memberJoinListener;

    @Async
    @PostConstruct
    public void startBot() {
        log.info("[start] DiscordBotConfig - startBot");
        log.info("[token] {}", botToken);

        JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(memberJoinListener)
                .buildAsync()
                .thenAccept(jda -> log.info("JDA iniciado com sucesso"))
                .exceptionally(error -> {
                    log.error("Erro ao iniciar JDA", error);
                    return null;
                });
    }
}
}
