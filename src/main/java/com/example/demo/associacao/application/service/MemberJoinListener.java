package com.example.demo.associacao.application.service;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Log4j2
@Component
@RequiredArgsConstructor
public class MemberJoinListener extends ListenerAdapter {
    @Value("${aws.url}")
    private String urlInstancia;
    private static final String ID_BOT = "1374448871836745853";
    private static final String ID_CANAL_VALIDACAO = "1374404661670318143";
    private static final String ID_CANAL_FALHA = "1391760038246481970";
    private static final String ID_CANAL_INICIAR_VALIDACAO = "1391784645535993919";

    private static final String ID_CARGO_MEMBRO_VALIDACAO = "1387064641410044076";
    private static final String ID_CARGO_DEFAULT = "1387064641410044076";
    private static final String ID_CARGO_FALHA = "1391761026772369448";
    private static final String ID_CARGO_WAKANDER = "1387069524679065750";

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
        Role roleDefault = guild.getRoleById(ID_CARGO_DEFAULT);
        Role roleValidacao = guild.getRoleById(ID_CARGO_MEMBRO_VALIDACAO);

        validarMembroECargo(member, roleValidacao);
        removerCargo(member, roleDefault);
        adicionarCargo(member, roleValidacao);
    }

    private void enviaMensagemDeBoasVindas(GuildMemberJoinEvent event) {
        validarGuild(event.getGuild());
        enviarMensagemPrivadaOuFallback(event);
    }

    private void adicionarCargo(Member member, Role cargo) {
        member.getGuild().addRoleToMember(member, cargo).queue();
    }

    private void removerCargo(Member member, Role cargo) {
        member.getGuild().removeRoleFromMember(member, cargo).queue();
    }

    private void enviarMensagemPrivadaOuFallback(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        User user = member.getUser();
        Guild guild = event.getGuild();
        TextChannel canalOnboarding = guild.getTextChannelById(ID_CANAL_VALIDACAO);
        enviaMensagem(member, user, guild, canalOnboarding);
    }

    private void enviarMensagemPrivada(PrivateChannel channel, User user, Member member, TextChannel canalOnboarding) {
        String url = gerarUrlValidacao(member.getUser().getName(), member.getId());
        String mensagemPrivada = String.format(
            "👋 Olá %s!\n\nClique no link abaixo e adicione o token que foi enviado no seu WhatsApp para validar sua entrada e liberar os módulos:\n\n%s",
            user.getName(), url);

        channel.sendMessage(mensagemPrivada).queue(
            sucesso -> {
                canalOnboarding.sendMessage(String.format(
                    "%s, Seja bem-vindo à Guild Wakanda!👋 \n" +
                    "Foi enviada uma mensagem no seu privado para validar sua entrada.\n" +
                    "Caso não encontre, acesse: https://discord.com/channels/@me/%s",
                    member.getAsMention(), ID_BOT)).queue();
            },
            falha -> {
                notificarFalhaNaDM(member, channel.getJDA().getGuildById(member.getGuild().getId()));
            }
        );
    }

    private void notificarFalhaNaDM(Member member, Guild guild) {
        TextChannel canalFalha = guild.getTextChannelById(ID_CANAL_FALHA);
        Role cargoFalha = guild.getRoleById(ID_CARGO_FALHA);
        Role cargoAntigo = guild.getRoleById(ID_CARGO_MEMBRO_VALIDACAO);
        String url = gerarUrlValidacao(member.getUser().getName(), member.getId());
        if (cargoAntigo != null) {
            guild.retrieveMemberById(member.getId()).queue(updatedMember -> {
                if (updatedMember.getRoles().contains(cargoAntigo)) {
                    log.info("[removerCargo] Removendo cargo antigo de {}", updatedMember.getEffectiveName());
                    removerCargo(updatedMember, cargoAntigo);
                } else {
                    log.warn("[removerCargo] Membro {} NÃO está com o cargo antigo no Discord (atualizado)", updatedMember.getEffectiveName());
                }
            }, error -> {
                log.error("❌ Erro ao atualizar dados do membro: {}", error.getMessage());
            });
        }
        if (cargoFalha != null) {
            adicionarCargo(member, cargoFalha);
        }

        String msg = String.format("%s, não conseguimos enviar a mensagem de validação no seu privado. \uD83D\uDE15\n\n" +
                "\uD83D\uDCA1 Valide usando o link abaixo:\n\n" +
                "\uD83D\uDC49 %s",
                member.getAsMention(), url);

        canalFalha.sendMessage(msg).queue();
    }

    private String gerarUrlValidacao(String username, String idDiscord) {
        return String.format("%s/api/formulario/%s/%s/associar-discord", urlInstancia, username, idDiscord);
    }

    private void validarMembroECargo(Member member, Role role) {
        if (member == null || role == null) {
            throw new IllegalStateException("Membro ou cargo não encontrado!");
        }
    }

    private void validarGuild(Guild guild) {
        if (guild == null) {
            throw new IllegalStateException("Servidor não encontrado!");
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("validar:botao")) return;

        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (member == null || guild == null || member.getUser().isBot()) return;

        event.deferReply(true).queue();
        
        Role cargoWakander = guild.getRoleById(ID_CARGO_WAKANDER);
        Role cargoValidacao = guild.getRoleById(ID_CARGO_MEMBRO_VALIDACAO);

        if (cargoWakander != null && member.getRoles().contains(cargoWakander)) {
            removerCargo(member, cargoWakander);
        }
        if (cargoValidacao != null) {
            adicionarCargo(member, cargoValidacao);
        }

        enviarMensagemPrivadaOuFallback(member, member.getUser(), guild);

        TextChannel canal = guild.getTextChannelById(ID_CANAL_INICIAR_VALIDACAO);
        if (canal != null) {
            canal.sendMessage(member.getAsMention() + " clicou para iniciar a validação. ✅").queue();
        }
        limparMensagensAntigas(canal);
    }
    
    private void enviarMensagemPrivadaOuFallback(Member member, User user, Guild guild) {
        TextChannel canal = guild.getTextChannelById(ID_CANAL_VALIDACAO);
        enviaMensagem(member, user, guild, canal);
    }

	private void enviaMensagem(Member member, User user, Guild guild, TextChannel canalOnboarding) {
		user.openPrivateChannel().queue(
            privateChannel -> enviarMensagemPrivada(privateChannel, user, member, canalOnboarding),
            failure -> notificarFalhaNaDM(member, guild)
        );
	}
	
	private void limparMensagensAntigas(TextChannel canal) {
	    canal.getHistory().retrievePast(100).queue(messages -> {
	        OffsetDateTime limite = OffsetDateTime.now().minusDays(14);
	        messages.stream()
	            .filter(msg -> msg.getTimeCreated().isBefore(limite))
	            .forEach(msg -> msg.delete().queue());
	    });
	}
}
