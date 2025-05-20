package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.example.demo.associacao.application.service.MemberJoinListener;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Configuration
@RequiredArgsConstructor
public class DiscordBotConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    private final MemberJoinListener memberJoinListener;

    @PostConstruct
    public void startBot() throws Exception {
        JDABuilder.createDefault(botToken)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(memberJoinListener)
            .build();
    }
}