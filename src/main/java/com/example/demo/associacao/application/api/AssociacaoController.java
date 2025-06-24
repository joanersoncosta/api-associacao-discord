package com.example.demo.associacao.application.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

	@GetMapping("/{username}/{idDiscord}/{token}/associar-discord")
	@ResponseStatus(value = HttpStatus.OK)
	public void associarContaDiscord(@PathVariable String username, @PathVariable String idDiscord, @PathVariable String token) {
		log.info("[inicia] AssociacaoController - associarContaDiscord");
		associacaoService.associarUsuario(username, idDiscord, token);
		log.info("[finaliza] AssociacaoController - associarContaDiscord");
	}

	@GetMapping("/gerar-link")
	public String gerarLinkConvite() {
		log.info("[inicia] AssociacaoController - gerarLinkConvite");
		String link = associacaoService.gerarOuObterLinkConvite();
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
	
	@GetMapping("/delete")
	public String deleteAll() {
		associacaoService.deleteAll();
		return "Dados Deletas com sucesso";
	}
	
	@GetMapping("/{token}/desassociar")
	public String desassociar(@PathVariable String token) {
		associacaoService.desassociar(token);
		return "Desassociar usuario";
	}
}