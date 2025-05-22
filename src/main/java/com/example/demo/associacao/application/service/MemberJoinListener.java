package com.example.demo.associacao.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.demo.associacao.comunicacao.ComunicacaoClient;

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
	private final ComunicacaoClient client;
    
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		log.info("[inicia] MemberJoinListener - onGuildMemberJoin");
		Member member = event.getMember();
		User user = member.getUser();

		String username = user.getName();
		String discordId = user.getId();

		String token = associacaoService.gerarOuObterLinkConvite(username);
		client.associarContaDiscord(username, discordId, token);
		log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");
	}

}
