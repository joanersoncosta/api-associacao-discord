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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Log4j2
@Component
@RequiredArgsConstructor
public class MemberJoinListener extends ListenerAdapter {
	@Value("${aws.url}")
	private String urlInstancia;
	private static final String ID_CANAL_ONBOARDING = "1374404661670318143";
	private static final String ID_CARGO_ONBOARDING = "1387064641410044076";

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		log.info("[inicia] MemberJoinListener - onGuildMemberJoin");
		atribuiCargoOnboarding(event);
		publicaMensagem(event);
		log.info("[finaliza] MemberJoinListener - onGuildMemberJoin");
	}

	private void atribuiCargoOnboarding(GuildMemberJoinEvent event) {
		log.info("[inicia] MemberJoinListener - atribuiCargoOnboarding");
		atribuirNovoCargo(event, ID_CARGO_ONBOARDING);
	}

	private void atribuirNovoCargo(GuildMemberJoinEvent event, String cargo) {
		log.info("[inicia] MemberJoinListener - atribuirNovoCargo");
		Guild guild = event.getGuild();
		Member member = event.getMember();
		Role onboardingRole = guild.getRoleById(cargo);
		validaRole(member, onboardingRole);
		guild.addRoleToMember(member, onboardingRole).queue();
	}

	private void publicaMensagem(GuildMemberJoinEvent event) {
		log.info("[inicia] MemberJoinListener - publicaMensagem");
		Member member = event.getMember();
		User user = member.getUser();
		Guild guild = event.getGuild();
		validaGuild(guild);
		TextChannel onboardingChannel = guild.getTextChannelById(ID_CANAL_ONBOARDING);
		String url = retornaUrl(user.getName(), member.getId());
		onboardingChannel.sendMessage(member.getAsMention()
				+ ", Seja bem-vindo a Guild Wakanda!👋 \nClique no link a baixo e adicione o token que foi enviado no seu whatsapp, para validar sua entrada e liberar os módulos: \n\n"
				+ url).queue();
	}

	private String retornaUrl(String username, String idDiscord) {
		String url = urlInstancia
				+ String.format("/api/associacao/%s/%s/SEU-TOKEN-AQUI/associar-discord", username, idDiscord);
		return url;
	}

	private void validaRole(Member member, Role roleWakander) {
		if (member == null || roleWakander == null) {
			throw APIException.build(HttpStatus.NOT_FOUND, "Membro ou cargo não encontrado!");
		}
	}

	private void validaGuild(Guild guild) {
		if (guild == null) {
			throw APIException.build(HttpStatus.NOT_FOUND, "Servidor não encontrado!");
		}
	}

}
