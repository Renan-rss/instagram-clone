package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // =========================================
    // Testes do método findById
    // =========================================

    @Test
    void deveRetornarUsuarioQuandoBuscarPorIdExistente() {
        Long userId = 1L;

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFullName("Paulo Pereira");
        userEntity.setEmail("paulo@ppereira.dev");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        UserDto result = userService.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("Paulo Pereira", result.fullName());
        assertEquals("paulo@ppereira.dev", result.email());

        verify(userRepository).findById(userId);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarUsuarioInexistente() {
        Long userId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.findById(userId)
        );

        assertEquals("User not found with id: " + userId, exception.getMessage());

        verify(userRepository).findById(userId);
    }

    // =========================================
    // Testes do método createUser
    // =========================================

    @Test
    void deveCriarUsuarioQuandoEmailEUsernameNaoExistirem() {
        UserDto userDto = new UserDto(
                null,
                "Paulo Pereira",
                "paulo",
                "paulo@ppereira.dev",
                "123456",
                null
        );

        when(userRepository.existsByEmail(userDto.email()))
                .thenReturn(false);
        when(userRepository.existsByUsername(userDto.username()))
                .thenReturn(false);
        when(passwordEncoder.encode(userDto.password()))
                .thenReturn("senha_criptografada");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(1L);
        savedEntity.setFullName(userDto.fullName());
        savedEntity.setUsername(userDto.username());
        savedEntity.setEmail(userDto.email());
        savedEntity.setEncryptedPassword("senha_criptografada");

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(savedEntity);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Paulo Pereira", result.fullName());
        assertEquals("paulo", result.username());
        assertEquals("paulo@ppereira.dev", result.email());

        verify(userRepository).existsByEmail(userDto.email());
        verify(userRepository).existsByUsername(userDto.username());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExistir() {
        UserDto userDto = new UserDto(
                null,
                "Paulo Pereira",
                "paulo",
                "paulo@ppereira.dev",
                "123456",
                null
        );

        when(userRepository.existsByEmail(userDto.email()))
                .thenReturn(true);

        FieldAlreadyExistsException exception = assertThrows(
                FieldAlreadyExistsException.class,
                () -> userService.createUser(userDto)
        );

        assertEquals("E-email already in use.", exception.getMessage());

        verify(userRepository).existsByEmail(userDto.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsernameJaExistir() {
        UserDto userDto = new UserDto(
                null,
                "Paulo Pereira",
                "paulo",
                "paulo@ppereira.dev",
                "123456",
                null
        );

        when(userRepository.existsByEmail(userDto.email()))
                .thenReturn(false);
        when(userRepository.existsByUsername(userDto.username()))
                .thenReturn(true);

        FieldAlreadyExistsException exception = assertThrows(
                FieldAlreadyExistsException.class,
                () -> userService.createUser(userDto)
        );

        assertEquals("Username already in use.", exception.getMessage());

        verify(userRepository).existsByEmail(userDto.email());
        verify(userRepository).existsByUsername(userDto.username());
        verify(userRepository, never()).save(any());
    }


    // =========================================
    // Testes do método updateUser
    // =========================================

    @Test
    void deveAtualizarUsuarioQuandoDadosValidos() {
        UserDto userDto = new UserDto(
                1L,
                "Nome Atualizado",
                "usuarioAtualizado",
                "atualizado@email.com",
                "novaSenha",
                null
        );

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setFullName("Nome Antigo");
        existingUser.setUsername("usuarioAntigo");
        existingUser.setEmail("antigo@email.com");
        existingUser.setEncryptedPassword("senha_antiga");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("novaSenha"))
                .thenReturn("senha_nova_criptografada");
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(userDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Nome Atualizado", result.fullName());
        assertEquals("usuarioAtualizado", result.username());
        assertEquals("atualizado@email.com", result.email());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
        verify(passwordEncoder).encode("novaSenha");
    }

    @Test
    void deveLancarExcecaoQuandoUserDtoOuIdForNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(null)
        );

        UserDto userDtoSemId = new UserDto(
                null,
                "Nome",
                "usuario",
                "email@email.com",
                null,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userDtoSemId)
        );
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForEncontradoParaAtualizacao() {
        UserDto userDto = new UserDto(
                99L,
                "Nome",
                "usuario",
                "email@email.com",
                null,
                null
        );

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(userDto)
        );

        assertEquals("User not found with id: 99", exception.getMessage());

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());
    }

    // =========================================
    // Testes do método deleteUser
    // =========================================

    @Test
    void deveDeletarUsuarioQuandoIdExistir() {
        Long userId = 1L;

        when(userRepository.existsById(userId))
                .thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deveLancarExcecaoQuandoTentarDeletarUsuarioInexistente() {
        Long userId = 999L;

        when(userRepository.existsById(userId))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals("User not found with id: " + userId, exception.getMessage());

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    // =========================================
    // Testes do método findAll
    // =========================================

    @Test
    void deveRetornarListaDeUsuariosQuandoExistiremRegistros() {
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setFullName("Usuario Um");
        user1.setUsername("user1");
        user1.setEmail("user1@email.com");

        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setFullName("Usuario Dois");
        user2.setUsername("user2");
        user2.setEmail("user2@email.com");

        when(userRepository.findAll())
                .thenReturn(java.util.List.of(user1, user2));

        var result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).id());
        assertEquals("Usuario Um", result.get(0).fullName());
        assertEquals("user1@email.com", result.get(0).email());

        assertEquals(2L, result.get(1).id());
        assertEquals("Usuario Dois", result.get(1).fullName());
        assertEquals("user2@email.com", result.get(1).email());

        verify(userRepository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() {
        when(userRepository.findAll())
                .thenReturn(java.util.List.of());

        var result = userService.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(userRepository).findAll();
    }



}
