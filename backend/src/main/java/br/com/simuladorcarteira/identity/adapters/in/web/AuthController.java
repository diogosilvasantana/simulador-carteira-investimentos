package br.com.simuladorcarteira.identity.adapters.in.web;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Quem está logado; sem sessão, a cadeia de segurança responde 401 antes de chegar aqui. */
@RestController
@RequestMapping("/api/auth")
class AuthController {

  record Me(String username) {}

  @GetMapping("/me")
  Me me(Principal principal) {
    return new Me(principal.getName());
  }
}
