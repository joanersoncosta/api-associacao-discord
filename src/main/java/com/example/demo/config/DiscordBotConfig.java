package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.example.demo.associacao.application.service.MemberJoinListener;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class DiscordBotConfig {

	@Value("${discord.bot.token}")
	private String botToken;
	private final MemberJoinListener memberJoinListener;

    @PostConstruct
    public void startBot() throws Exception {
        log.info("[start] DiscordBotConfig - startBot");
        JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(memberJoinListener)
                .build();
        log.debug("[finish] DiscordBotConfig - startBot");
    }
    
//	@PostConstruct
//	public void startBot() {
//		log.info("[start] DiscordBotConfig - startBot");
//		JDABuilder.createDefault(botToken)
//	    .enableIntents(GatewayIntent.GUILD_MEMBERS)
//	    .addEventListeners(memberJoinListener)
//	    .addEventListeners(new ListenerAdapter() {
//	        @Override
//	        public void onReady(ReadyEvent event) {
//	            log.info("JDA iniciado com sucesso");
//	        }
//	    })
//	    .build();
//	}
}