package com.example.demo.associacao.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.demo.handler.APIException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
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
	private final JDA jda;
	private static final String ID_GUILD = "1374404660906950718";
	private static final String ID_CANAL_ONBOARDING = "1374404661670318143";
	private static final String ID_CARGO_ONBOARDING = "1387064641410044076";
	private static final String ID_CARGO_WAKANDER = "1387069524679065750";

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		log.info("[inicia] MemberJoinListener - onGuildMemberJoin");
		atribuiCargoOnboarding(event);
		enviaMensagem(event);
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
		guild.addRoleToMember(member, onboardingRole).queue();
	}

	private void enviaMensagem(GuildMemberJoinEvent event) {
		log.info("[inicia] MemberJoinListener - enviaMensagem");
		Member member = event.getMember();
		User user = member.getUser();
		String idDiscord = member.getId();
		String username = user.getName();
		log.info("[Nome] {}, [IdDiscord] {}", username, idDiscord);
		Guild guild = event.getGuild();
		TextChannel onboardingChannel = guild.getTextChannelById(ID_CANAL_ONBOARDING);
		String url = retornaUrl(username, idDiscord);
		onboardingChannel.sendMessage(member.getAsMention()
				+ ", Seja bem-vindo a Guild Wakanda!👋 \nClique no link a baixo e adicione o token que foi enviado no seu whatsapp, para validar sua entrada e liberar os módulos: \n\n"
				+ url).queue();
	}

	private String retornaUrl(String username, String idDiscord) {
		String url = urlInstancia
				+ String.format("/api/associacao/%s/%s/SEU-TOKEN-AQUIO/associar-discord", username, idDiscord);
		return url;
	}

	public void atualizaCargoParaWakander(String idDiscord) {
		atribuirNovoCargo(null, ID_CARGO_WAKANDER);
		Guild guild = jda.getGuildById(ID_GUILD);
		validaGuild(guild);
		Member member = guild.getMemberById(idDiscord);
		Role roleWakander = guild.getRoleById(ID_CARGO_WAKANDER);
		Role roleOnboarding = guild.getRoleById(ID_CARGO_ONBOARDING);
		validaRole(member, roleWakander);
		guild.addRoleToMember(member, roleWakander).queue();
		removeRoleOnboarding(guild, member, roleOnboarding);
		removeMensagensDoWakander(member);

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
