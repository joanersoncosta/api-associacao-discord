package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import com.example.demo.associacao.application.service.MemberJoinListener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Slf4j
@Configuration
public class DiscordBotConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    @Bean
    JDA jda(MemberJoinListener memberJoinListener) throws Exception {
        log.info("[start] Aguardando 5 segundos antes de conectar ao Discord...");
        Thread.sleep(5000);
        log.info("[start] Iniciando o JDA...");
        JDA jda = JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(memberJoinListener)
                .addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onReady(ReadyEvent event) {
                        log.info("✅ JDA iniciado com sucesso - Conectado como: {}", event.getJDA().getSelfUser().getAsTag());
                    }
                })
                .build();
        jda.awaitReady();
        return jda;
    }
}