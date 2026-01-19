package br.edu.ifpb.instagram.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setup() {
        jwtUtils = new JwtUtils();
    }

    
    // -------Testes do método generateToken-------
    

    @Test
    void deveGerarTokenQuandoAuthenticationForValido() {
        when(authentication.getName()).thenReturn("usuarioTeste");

        String token = jwtUtils.generateToken(authentication);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    
    // -------Testes do método validateToken-------
    

    @Test
    void deveRetornarTrueQuandoTokenForValido() {
        when(authentication.getName()).thenReturn("usuarioTeste");

        String token = jwtUtils.generateToken(authentication);

        boolean valido = jwtUtils.validateToken(token);

        assertTrue(valido);
    }

    @Test
    void deveRetornarFalseQuandoTokenForInvalido() {
        String tokenInvalido = "token.invalido.qualquer";

        boolean valido = jwtUtils.validateToken(tokenInvalido);

        assertFalse(valido);
    }

   
    // -------Testes do método getUsernameFromToken-------
    

    @Test
    void deveExtrairUsernameCorretamenteDoToken() {
        when(authentication.getName()).thenReturn("usuarioTeste");

        String token = jwtUtils.generateToken(authentication);

        String usernameExtraido = jwtUtils.getUsernameFromToken(token);

        assertEquals("usuarioTeste", usernameExtraido);
    }
}
