package com.example.demo.associacao.infra;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.associacao.domain.AssociacaoDiscord;

public interface AssociacaoDiscordRepository extends JpaRepository<AssociacaoDiscord, UUID> {
	Optional<AssociacaoDiscord> findByToken(String token);
    Optional<AssociacaoDiscord> findByNomeUsuario(String nome);
}