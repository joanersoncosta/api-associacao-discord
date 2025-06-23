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
    private final AssociacaoService associacaoService;
	@Value("${aws.url}")
	private String urlInstancia;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        log.info("[inicia] MemberJoinListener - onGuildMemberJoin");
        Member member = event.getMember();
        User user = member.getUser();
		String discordId = user.getId();
        String username = user.getName();
        log.info("[Nome] {}, [IdDiscord] {}", username, discordId);
        associacaoService.associarUsuario(username, discordId);
        log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");
    }
    
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
