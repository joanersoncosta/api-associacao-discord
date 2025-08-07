package com.example.demo.associacao.application.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.demo.config.JDAProvider;
import com.example.demo.handler.APIException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Log4j2
@Component
@RequiredArgsConstructor
public class DiscordApplicationService implements DiscordService {
//	private final JDA jda;
	private final JDAProvider jdaProvider;
	private static final String ID_GUILD = "963147104258256957";
	private static final String ID_CANAL_VALIDACAO = "1390403777323864074";
	private static final String ID_CANAL_FALHA = "1390416969278165032";
	private static final String ID_CANAL_INICIAR_VALIDACAO = "1391886723151171714";

	private static final String ID_CARGO_MEMBRO_VALIDACAO = "1391892125326774362";
	private static final String ID_CARGO_WAKANDER = "1391893372645671052";
	private static final String ID_CARGO_FALHA = "1391891594436939846";
	private static final String ID_EVERIONE = "963147104258256957";
	private static final String ID_DEFAULT = "1391854218276503552";
	private static final String ID_CARGO_CANCELADO = "1394374062821605406";

	@PostConstruct
	public void enviarMensagemInicialComBotao() {
		CompletableFuture.runAsync(() -> {
			jdaProvider.getJda().ifPresentOrElse(jda -> {
				TextChannel canal = jda.getTextChannelById(ID_CANAL_INICIAR_VALIDACAO);
				if (canal == null) {
					log.warn("Canal de validação não encontrado (ID: {})", ID_CANAL_INICIAR_VALIDACAO);
					return;
				}
				log.debug("Verificando se já existe mensagem do bot no canal: {}", canal.getName());
				criaButtonCanal(ID_CANAL_INICIAR_VALIDACAO, canal);
			}, () -> log.warn("JDA ainda não disponível, mensagem inicial não será enviada agora!"));
		}, CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS));
	}

	private void criaButtonCanal(String canalId, TextChannel canal) {
		canal.getIterableHistory().takeAsync(10).thenAccept(messages -> {
			boolean jaTemMensagem = messages.stream().anyMatch(msg -> msg.getAuthor().isBot());
			if (!jaTemMensagem) {
				log.info("Enviando mensagem de validação para o canal {}", canal.getName());
				canal.sendMessage("**🎯 Para validar sua conta e ter novamente acesso aos canais:**\n"
						+ "Clique no botão abaixo para iniciar sua validação:")
						.setActionRow(Button.primary("validar:botao", "✅ Validar Agora"))
						.queue(success -> log.info("Mensagem enviada com sucesso"),
								error -> log.error("Erro ao enviar mensagem: {}", error.getMessage()));
			} else {
				log.debug("Mensagem de validação já existe no canal {}", canal.getName());
			}
		}).exceptionally(ex -> {
			log.error("Erro ao buscar mensagens no canal (ID: {}): {}", canalId, ex.getMessage());
			return null;
		});
	}
	
	private Guild getGuildById() {
		return jdaProvider.getJdaOrThrow().getGuildById(ID_GUILD);
	}
	
	public void atualizaCargoParaWakander(String idDiscord) {
		log.info("[inicia] DiscordApplicationService - atualizaCargoParaWakander");
		Guild guild = getGuildById();
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
			removeRole(guild, member, roleFalha, roleValidado);
			guild.addRoleToMember(member, roleWakander).queue();
			removeMensagens(member, ID_CANAL_VALIDACAO, ID_CANAL_INICIAR_VALIDACAO, ID_CANAL_FALHA);
		}, failure -> {
			log.warn("Falha ao buscar membro com ID {}: {}", idDiscord, failure.getMessage());
			throw APIException.build(HttpStatus.NOT_FOUND, "Membro não encontrado!");
		});
	}

	private void removeMensagens(Member member, String... idCanais) {
		for (String idCanal : idCanais) {
			TextChannel channel = jdaProvider.getJdaOrThrow().getTextChannelById(idCanal);			
			if (channel != null) {
				channel.getHistory().retrievePast(100).queue(messages -> {
					messages.stream()
				    .filter(msg -> msg.getMentions().getUsers().contains(member.getUser()))
				    .filter(msg -> !msg.getAuthor().isBot() || msg.getActionRows().stream()
				        .noneMatch(row -> row.getButtons().stream()
				            .anyMatch(button -> "validar:botao".equals(button.getId()))
				        )
				    )
				    .forEach(msg -> msg.delete().queue());
				});
			}
		}
	}

	private void validaSeMembroExiste(Member member) {
		if (member == null) {
			log.warn("Membro ainda é null após retrieveMemberById");
			throw APIException.build(HttpStatus.NOT_FOUND, "Membro não encontrado!");
		}
	}

	private void removeRole(Guild guild, Member member, Role... roles) {
		for (Role role : roles) {
			if (role != null && member.getRoles().contains(role)) {
				guild.removeRoleFromMember(member, role).queue();
			}
		}
	}

	private void validaRole(Role... roles) {
		for (Role role : roles) {
			if (role == null) {
				throw APIException.build(HttpStatus.NOT_FOUND, "Membro ou cargo não encontrado!");
			}
		}
	}

	private void validaGuild(Guild guild) {
		if (guild == null) {
			throw APIException.build(HttpStatus.NOT_FOUND, "Servidor não encontrado!");
		}
	}

	@Override
	public void substituirCargoDefaultPorWakander(String idDiscord) {
		log.info("Verificando se deve atribuir Wakander ao ID: {}", idDiscord);
		Guild guild = getGuildById();
		Role cargoWakander = guild.getRoleById(ID_CARGO_WAKANDER);
		guild.loadMembers().onSuccess(members -> {
			int contador = 0;
			for (Member member : members) {
				if (member.getUser().isBot())
					continue;
				List<Role> roles = member.getRoles();
				if (roles.size() == 0) {
					guild.addRoleToMember(member, cargoWakander).queue();
					contador++;
				}
			}
			log.info("Total de membros atualizados com o cargo Wakander: {}", contador);
		}).onError(throwable -> {
			log.error("Erro ao carregar membros da guilda: ", throwable);
		});
	}

	@Override
	public CompletableFuture<Integer> contaQuantosSemCargoExistem() {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		Guild guild = getGuildById();

		guild.loadMembers().onSuccess(members -> {
			int contador = 0;
			for (Member member : members) {
				if (member.getUser().isBot())
					continue;

				List<Role> roles = member.getRoles();

				if (roles.size() == 0) {
					contador++;
				}
			}
			future.complete(contador);
		}).onError(future::completeExceptionally);

		return future;
	}

//	@Override
//	public CompletableFuture<Integer> contaQuantosSemCargoExistem() {
//		CompletableFuture<Integer> future = new CompletableFuture<>();
//		Guild guild = jda.getGuildById(ID_GUILD);
//
//		guild.loadMembers().onSuccess(members -> {
//			int contador = 0;
//			for (Member member : members) {
//				if (member.getUser().isBot()) continue;
//				List<Role> roles = member.getRoles();
//				if (roles.size() == 0) {
//					contador++;
//				}
//			}
//			future.complete(contador);
//		}).onError(future::completeExceptionally);
//
//		return future;
//	}

//	public void substituirCargoDefaultPorWakander() {
//    Guild guild = jda.getGuildById(ID_GUILD);
//    Role cargoWakander = guild.getRoleById(ID_CARGO_WAKANDER);
//
//    guild.loadMembers().onSuccess(members -> {
//        for (Member member : members) {
//            if (member.getUser().isBot()) continue;
//
//            List<Role> roles = member.getRoles();
//            if (roles.isEmpty()) {
//                guild.addRoleToMember(member, cargoWakander).queue();
//            }
//        }
//    });
//}

//	public void substituirCargoDefaultPorWakander() {
//	    Guild guild = jda.getGuildById(ID_GUILD);
//	    Role cargoWakander = guild.getRoleById(ID_CARGO_WAKANDER);
//
//	    guild.loadMembers().onSuccess(members -> {
//	        for (Member member : members) {
//	            if (member.getUser().isBot()) continue;
//
//	            List<Role> roles = member.getRoles();
//	            if (roles.isEmpty()) {
//	                guild.addRoleToMember(member, cargoWakander).queue();
//	            }
//	        }
//	    });
//	}

//	public CompletableFuture<Integer> contaQuantosSemCargoExistem() {
//	    CompletableFuture<Integer> future = new CompletableFuture<>();
//	    Guild guild = jda.getGuildById(ID_GUILD);
//
//	    guild.loadMembers().onSuccess(members -> {
//	        int contador = 0;
//	        for (Member member : members) {
//	            if (member.getUser().isBot()) continue;
//	            if (member.getRoles().isEmpty()) {
//	                contador++;
//	            }
//	        }
//	        future.complete(contador); 
//	    }).onError(future::completeExceptionally);
//	    return future;
//	}

}