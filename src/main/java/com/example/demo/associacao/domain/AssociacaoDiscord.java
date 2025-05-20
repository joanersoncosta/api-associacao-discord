package com.example.demo.associacao.domain;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.example.demo.associacao.handler.APIException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "associacao_discord")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssociacaoDiscord {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(columnDefinition = "uuid", name = "id_associacao")
	private UUID idAssociacao;

	@Column(unique = true, name = "nome_usuario")
	private String nomeUsuario;

	@Column(name = "discord_id")
	private String discordId;

	@Column(unique = true)
	private String token;

	@Column(name = "associado")
	private boolean associado;

	public void associar(String idDiscordUser, String nome) {
		this.discordId = idDiscordUser;
		this.nomeUsuario = nome;
		this.associado = true;
	}

	public void editarToken(String token) {
		this.token = token;
	}

	public AssociacaoDiscord(String nome) {
		this.nomeUsuario = nome;
		this.associado = false;
	}

	public void validaSeJaFoiAssociado() {
		if (associado == true) {
			throw APIException.build(HttpStatus.NOT_FOUND, "Usuario já foi associado!");
		}
	}

}