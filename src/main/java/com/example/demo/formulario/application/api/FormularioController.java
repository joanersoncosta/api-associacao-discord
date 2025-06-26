package com.example.demo.formulario.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.associacao.application.service.AssociacaoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/formulario")
public class FormularioController {
	private final AssociacaoService associacaoService;

	@GetMapping("/{username}/{idDiscord}/associar-discord")
	@ResponseStatus(value = HttpStatus.OK)
	public ModelAndView retornaAssociarContaDiscord() {
		log.info("[inicia] AssociacaoController - associarContaDiscord");
		return new ModelAndView("formulario-token-discord");
	}

	@GetMapping("/resposta-token-discord")
	public ModelAndView respostaTokenDiscord() {
	    return new ModelAndView("resposta-token-discord");
	}
	
    @GetMapping("/")
    public ModelAndView exibirFormulario() {
        return new ModelAndView("formulario");
    }
}