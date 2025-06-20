package com.example.demo.associacao.application.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@PatchMapping("/{username}/associar-discord")
	@ResponseStatus(value = HttpStatus.OK)
	public void associarContaDiscord(@PathVariable String username) {
		log.info("[inicia] AssociacaoController - associarContaDiscord");
		associacaoService.associarUsuario(username);
		log.info("[finaliza] AssociacaoController - associarContaDiscord");
	}

	@GetMapping("/{nome}/gerar-link")
	public String gerarLinkConvite(@PathVariable String nome) {
		log.info("[inicia] AssociacaoController - gerarLinkConvite");
		String link = associacaoService.gerarOuObterLinkConvite(nome);
		log.info("[finaliza] AssociacaoController - gerarLinkConvite");
		return link;
	}

	@GetMapping("/{token}")
	public AssociacaoDiscord buscaPorToken(@PathVariable String token) {
		return associacaoService.buscaPorToken(token);
	}
	
	@GetMapping("/")
	public List<AssociacaoDiscord> lista() {
		return associacaoService.lista();
	}
}