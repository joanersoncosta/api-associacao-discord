package com.example.demo.associacao.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.demo.handler.APIException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Log4j2
@Component
@RequiredArgsConstructor
public class MemberJoinListener extends ListenerAdapter {
    @Value("${aws.url}")
    private String urlInstancia;
    private static final String ID_BOT = "1374448871836745853";
    private static final String ID_CANAL_ONBOARDING = "1374404661670318143";
    private static final String ID_CARGO_ONBOARDING = "1387064641410044076";

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        log.info("[inicia] MemberJoinListener - onGuildMemberJoin");
        atribuiCargoInicial(event);
        enviaMensagemDeBoasVindas(event);
        log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");
    }

    private void atribuiCargoInicial(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        Role roleOnboarding = guild.getRoleById(ID_CARGO_ONBOARDING);

        validarMembroECargo(member, roleOnboarding);
        adicionarCargo(member, roleOnboarding);
    }

    private void enviaMensagemDeBoasVindas(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        User user = member.getUser();
        Guild guild = event.getGuild();

        validarGuild(guild);
        String url = gerarUrlValidacao(user.getName(), member.getId());

        enviarMensagemPrivadaOuFallback(member, user, guild, url);
    }

    private void adicionarCargo(Member member, Role cargo) {
        member.getGuild().addRoleToMember(member, cargo).queue();
    }

    private void enviarMensagemPrivadaOuFallback(Member member, User user, Guild guild, String url) {
        TextChannel canalOnboarding = guild.getTextChannelById(ID_CANAL_ONBOARDING);

        user.openPrivateChannel().queue(
            privateChannel -> enviarMensagemPrivada(privateChannel, user, url, member, canalOnboarding),
            failure -> notificarFalhaNaDM(member, url, canalOnboarding)
        );
    }

    private void enviarMensagemPrivada(PrivateChannel channel, User user, String url, Member member, TextChannel canalOnboarding) {
        String mensagemPrivada = String.format(
                "👋 Olá %s!\n\nClique no link abaixo e adicione o token que foi enviado no seu WhatsApp para validar sua entrada e liberar os módulos:\n\n%s",
                user.getName(), url);
        channel.sendMessage(mensagemPrivada).queue();

        canalOnboarding.sendMessage(String.format("%s, Seja bem-vindo à Guild Wakanda!👋 \n" +
                "Foi enviada uma mensagem no seu privado para validar sua entrada.\n" +
                "Caso não encontre, acesse: https://discord.com/channels/@me/%s", member.getAsMention(), ID_BOT))
                .queue();
    }

    private void notificarFalhaNaDM(Member member, String url, TextChannel canalOnboarding) {
        canalOnboarding.sendMessage(String.format(
                "%s, Seja bem-vindo à Guild Wakanda!👋 \n" +
                "❌ Não conseguimos mandar a URL de validação no seu privado.\n\n" +
                "👉 Clique no link abaixo e adicione o token Discord que foi enviado no seu WhatsApp para validar sua entrada e liberar os módulos:\n\n%s",
                member.getAsMention(), url)).queue();
    }

    private String gerarUrlValidacao(String username, String idDiscord) {
        return String.format("%s/api/formulario/%s/%s//associar-discord", urlInstancia, username, idDiscord);
    }

    private void validarMembroECargo(Member member, Role role) {
        if (member == null || role == null) {
            throw APIException.build(HttpStatus.NOT_FOUND, "Membro ou cargo não encontrado!");
        }
    }

    private void validarGuild(Guild guild) {
        if (guild == null) {
            throw APIException.build(HttpStatus.NOT_FOUND, "Servidor não encontrado!");
        }
    }
}
