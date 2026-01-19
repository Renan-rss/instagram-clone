package br.edu.ifpb.instagram.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import jakarta.persistence.EntityManager;


@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private UserEntity criarUsuarioPadrao() {
        UserEntity user = new UserEntity();
        user.setFullName("Kevyn Bryan");
        user.setUsername("Kevyn123");
        user.setEmail("kevyn@teste.com");
        user.setEncryptedPassword("12345678");
        return user;
    }

    @Test
    void deveSalvarEBuscarUsuarioPorId() {
        UserEntity user = criarUsuarioPadrao();

        UserEntity savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());

        Optional<UserEntity> encontrado = userRepository.findById(savedUser.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Kevyn Bryan", encontrado.get().getFullName());
        assertEquals("Kevyn123", encontrado.get().getUsername());
        assertEquals("kevyn@teste.com", encontrado.get().getEmail());
    }

    @Test
    void deveRetornarTrueQuandoEmailExistir() {
        userRepository.save(criarUsuarioPadrao());

        boolean existe = userRepository.existsByEmail("kevyn@teste.com");

        assertTrue(existe);
    }

    @Test
    void deveRetornarTrueQuandoUsernameExistir() {
        userRepository.save(criarUsuarioPadrao());

        boolean existe = userRepository.existsByUsername("Kevyn123");

        assertTrue(existe);
    }

    @Test
    void deveBuscarUsuarioPorUsername() {
        userRepository.save(criarUsuarioPadrao());

        Optional<UserEntity> encontrado = userRepository.findByUsername("Kevyn123");

        assertTrue(encontrado.isPresent());
        assertEquals("Kevyn Bryan", encontrado.get().getFullName());
    }

    @Test
    void deveBuscarTodosOsUsuarios() {
        userRepository.save(criarUsuarioPadrao());

        UserEntity outroUsuario = new UserEntity();
        outroUsuario.setFullName("Renan de Sales");
        outroUsuario.setUsername("Renan123");
        outroUsuario.setEmail("renan@teste.com");
        outroUsuario.setEncryptedPassword("12345678");

        userRepository.save(outroUsuario);

        var usuarios = userRepository.findAll();

        assertEquals(2, usuarios.size());
    }

   @Test
    void deveAtualizarParcialmenteUsuario() {
        UserEntity user = userRepository.save(criarUsuarioPadrao());

        int linhasAfetadas = userRepository.updatePartialUser(
                "Kevyn",
                null,
                null,
                null,
                user.getId()
        );

        assertEquals(1, linhasAfetadas);

        entityManager.flush();
        entityManager.clear();

        UserEntity atualizado = userRepository.findById(user.getId()).get();

        assertEquals("Kevyn", atualizado.getFullName());
        assertEquals("Kevyn123", atualizado.getUsername());
        assertEquals("kevyn@teste.com", atualizado.getEmail());
}

}