package com.example.demo.associacao.application.service;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
@RequiredArgsConstructor
public class MemberJoinListener extends ListenerAdapter {
    private final AssociacaoService associacaoService;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        User user = member.getUser();

        String username = user.getName();
        String discordId = user.getId();

        String token = associacaoService.gerarOuObterLinkConvite(username);

        String url = String.format(
            "https://localhost:8080/api/associacao/%s/%s/associar-discord?token=%s",
            username, discordId, token
        );

        String mensagem = "Olá " + username + "! 👋\nClique aqui para associar sua conta: " + url;

        user.openPrivateChannel()
            .flatMap(channel -> channel.sendMessage(mensagem))
            .queue(
                success -> System.out.println("Mensagem enviada para " + username),
                error -> System.err.println("Erro ao enviar DM para " + username)
            );
    }


}
