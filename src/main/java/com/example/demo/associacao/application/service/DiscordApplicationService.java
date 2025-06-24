package com.example.demo.associacao.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.demo.handler.APIException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Log4j2
@Component
@RequiredArgsConstructor
public class DiscordApplicationService implements DiscordService {
	private final JDA jda;
	private static final String ID_GUILD = "1374404660906950718";
	private static final String ID_CANAL_ONBOARDING = "1374404661670318143";
	private static final String ID_CARGO_ONBOARDING = "1387064641410044076";
	private static final String ID_CARGO_WAKANDER = "1387069524679065750";

	public void atualizaCargoParaWakander(String idDiscord) {
		log.info("[inicia] DiscordApplicationService - atualizaCargoParaWakander");
		Guild guild = jda.getGuildById(ID_GUILD);
		validaGuild(guild);
		atualizaCargo(idDiscord, guild);
	}

	private void atualizaCargo(String idDiscord, Guild guild) {
		guild.retrieveMemberById(idDiscord).queue(member -> {
			validaSeMembroExiste(member);
			Role roleWakander = guild.getRoleById(ID_CARGO_WAKANDER);
			Role roleOnboarding = guild.getRoleById(ID_CARGO_ONBOARDING);
			validaRole(roleWakander, roleOnboarding);
			removeRoleOnboarding(guild, member, roleOnboarding);
			guild.addRoleToMember(member, roleWakander).queue();
			removeMensagensDoWakander(member);
		}, failure -> {
			log.warn("❌ Falha ao buscar membro com ID {}: {}", idDiscord, failure.getMessage());
			throw APIException.build(HttpStatus.NOT_FOUND, "Membro não encontrado!");
		});
	}

	private void validaSeMembroExiste(Member member) {
		if (member == null) {
			log.warn("❌ Membro ainda é null após retrieveMemberById");
			throw APIException.build(HttpStatus.NOT_FOUND, "Membro não encontrado!");
		}
	}

	private void removeMensagensDoWakander(Member member) {
		TextChannel onboardingChannel = jda.getTextChannelById(ID_CANAL_ONBOARDING);
		if (onboardingChannel != null) {
			onboardingChannel.getHistory().retrievePast(100).queue(messages -> {
				messages.stream().filter(msg -> msg.getMentions().getUsers().contains(member.getUser()))
						.forEach(msg -> msg.delete().queue());
			});
		}
	}

	private void removeRoleOnboarding(Guild guild, Member member, Role roleOnboarding) {
		if (roleOnboarding != null && member.getRoles().contains(roleOnboarding)) {
			guild.removeRoleFromMember(member, roleOnboarding).queue();
		}
	}

	private void validaRole(Role... roleWakander) {
		if (roleWakander == null) {
			throw APIException.build(HttpStatus.NOT_FOUND, "Membro ou cargo não encontrado!");
		}
	}

	private void validaGuild(Guild guild) {
		if (guild == null) {
			throw APIException.build(HttpStatus.NOT_FOUND, "Servidor não encontrado!");
		}
	}

}
