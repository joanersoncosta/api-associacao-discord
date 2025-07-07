package com.example.demo.associacao.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.demo.handler.APIException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Log4j2
@Component
@RequiredArgsConstructor
public class DiscordApplicationService implements DiscordService {
	private final JDA jda;
	private static final String ID_GUILD = "1374404660906950718";
    private static final String ID_CANAL_VALIDACAO = "1374404661670318143";
    private static final String ID_CANAL_INICIAR_VALIDACAO = "1391784645535993919";
	private static final String ID_CARGO_MEMBRO_VALIDACAO = "1387064641410044076";
	private static final String ID_CARGO_WAKANDER = "1387069524679065750";
    private static final String ID_CARGO_FALHA = "1391761026772369448";

    @PostConstruct
    public void enviarMensagemInicialComBotao() {
        TextChannel canal = jda.getTextChannelById(ID_CANAL_INICIAR_VALIDACAO);
        if (canal != null) {
            canal.getIterableHistory().takeAsync(10).thenAccept(messages -> {
                boolean jaTemMensagem = messages.stream().anyMatch(msg -> msg.getAuthor().isBot());
                if (!jaTemMensagem) {
                    canal.sendMessage("\uD83D\uDC4B Seja bem-vindo(a) à Guild Wakanda!\n\n" +
                            "Para liberar seu acesso, clique no botão abaixo para iniciar sua validação:")
                        .setActionRow(Button.primary("validar:botao", "✅ Validar Agora"))
                        .queue();
                }
            });
        }
    }
    
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
			Role roleValidado = guild.getRoleById(ID_CARGO_MEMBRO_VALIDACAO);
			Role roleFalha = guild.getRoleById(ID_CARGO_FALHA);
			validaRole(roleWakander, roleValidado);
			removeRoleOnboarding(guild, member, roleFalha);
			removeRoleOnboarding(guild, member, roleValidado);
			guild.addRoleToMember(member, roleWakander).queue();
			removeMensagens(member, ID_CANAL_VALIDACAO, ID_CANAL_INICIAR_VALIDACAO, ID_CANAL_VALIDACAO);
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
	
	private void removeMensagens(Member member, String... idCanal) {
		TextChannel onboardingChannel = jda.getTextChannelById(ID_CANAL_VALIDACAO);
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
	
	public void substituirCargoDefaultPorWakander() {
		Guild guild = jda.getGuildById(ID_GUILD);
	    Role cargoDefault = guild.getRoleById(ID_CARGO_MEMBRO_VALIDACAO);
	    Role cargoWakander = guild.getRoleById(ID_CARGO_WAKANDER);

	    guild.loadMembers().onSuccess(members -> {
	        for (Member member : members) {
	            if (member.getUser().isBot()) continue;

	            if (member.getRoles().contains(cargoDefault)) {
	                guild.removeRoleFromMember(member, cargoDefault).queue();
	                guild.addRoleToMember(member, cargoWakander).queue();
	            }
	        }
	    });
	}
}