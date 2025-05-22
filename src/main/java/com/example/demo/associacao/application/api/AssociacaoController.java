package com.example.demo.associacao.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.associacao.application.service.AssociacaoService;
import com.example.demo.associacao.domain.AssociacaoDiscord;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/associacao")
public class AssociacaoController {
	private final AssociacaoService associacaoService;

	@PostMapping("/{username}/{discordId}/{token}/associar-discord")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void associarContaDiscord(@PathVariable String username, @PathVariable String discordId,
			@PathVariable String token) {
		log.info("[inicia] AssociacaoController - associarContaDiscord");
		associacaoService.associarUsuario(username, discordId, token);
		log.info("[finaliza] AssociacaoController - associarContaDiscord");
	}

	@PostMapping("/{nomeUsuario}/gerar-link")
	public String gerarLinkConvite(@PathVariable String nomeUsuario) {
		log.info("[inicia] AssociacaoController - gerarLinkConvite");
		String link = associacaoService.gerarOuObterLinkConvite(nomeUsuario);
		log.info("[finaliza] AssociacaoController - gerarLinkConvite");
		return link;
	}

	@GetMapping("/")
	public ResponseEntity<String> olaHome() {
		return ResponseEntity.ok("Olaaa home");
	}
	
	@GetMapping("/{token}")
	public AssociacaoDiscord buscaPorToken(@PathVariable String token) {
		return associacaoService.buscaPorToken(token);
	}
}