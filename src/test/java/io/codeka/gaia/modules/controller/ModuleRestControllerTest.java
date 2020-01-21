package io.codeka.gaia.modules.controller;

import io.codeka.gaia.modules.bo.TerraformModule;
import io.codeka.gaia.modules.repository.TerraformModuleRepository;
import io.codeka.gaia.teams.Team;
import io.codeka.gaia.teams.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleRestControllerTest {

    @InjectMocks
    private ModuleRestController moduleRestController;

    @Mock
    private TerraformModuleRepository moduleRepository;

    private User admin;

    private User bob;

    private User john;

    private Team bobsTeam;

    @BeforeEach
    void setUp() {
        admin = new User("admin", null);

        john = new User("John Dorian", null);

        bobsTeam = new Team("bobsTeam");
        bob = new User("Bob Kelso", bobsTeam);
    }

    @Test
    void findAll_shouldReturnAllModules_forAdmin(){
        // when
        moduleRestController.findAllModules(admin);

        // then
        verify(moduleRepository).findAll();
    }

    @Test
    void findAll_shouldReturnAuthorizedModules_forUserTeam_andOwnedModules(){
        // given
        when(moduleRepository.findAllByCreatedByOrAuthorizedTeamsContaining(bob, bobsTeam)).thenReturn(List.of(new TerraformModule()));

        // when
        var modules = moduleRestController.findAllModules(bob);

        // then
        assertThat(modules).hasSize(1);
        verify(moduleRepository).findAllByCreatedByOrAuthorizedTeamsContaining(bob, bobsTeam);
    }

    @Test
    void findAll_shouldReturnOwnedModules_forUserWithNoTeam(){
        // when
        var modules = moduleRestController.findAllModules(john);

        // then
        verify(moduleRepository).findAllByCreatedBy(john);
    }

    @Test
    void findById_shouldReturnModule_forAdmin(){
        // given
        when(moduleRepository.findById("12")).thenReturn(Optional.of(new TerraformModule()));

        // when
        moduleRestController.findModule("12", admin);

        // then
        verify(moduleRepository).findById("12");
    }

    @Test
    void findById_shouldReturnOwnedModule_forUserWithNoTeam(){
        // given
        var ownedModule = new TerraformModule();
        ownedModule.setCreatedBy(john);
        when(moduleRepository.findById("12")).thenReturn(Optional.of(ownedModule));

        // when
        moduleRestController.findModule("12", john);

        // then
        verify(moduleRepository).findById("12");
    }

    @Test
    void findById_shouldReturnAuthorizedModule_forUserTeam(){
        // given
        var module = mock(TerraformModule.class);
        when(module.isAuthorizedFor(bob)).thenReturn(true);
        when(moduleRepository.findById("12")).thenReturn(Optional.of(module));

        // when
        moduleRestController.findModule("12", bob);

        // then
        verify(moduleRepository).findById("12");
    }


    @Test
    void findById_shouldThrowModuleNotFound_forNonExistingModules(){
        // given
        when(moduleRepository.findById("12")).thenReturn(Optional.empty());

        // when
        assertThrows(ModuleNotFoundException.class, () -> moduleRestController.findModule("12", admin));
        assertThrows(ModuleNotFoundException.class, () -> moduleRestController.findModule("12", john));
        assertThrows(ModuleNotFoundException.class, () -> moduleRestController.findModule("12", bob));
    }

    @Test
    void findById_shouldThrowForbiddenModule_forUnauthorizedUsers(){
        // given
        var module = mock(TerraformModule.class);
        when(module.isAuthorizedFor(bob)).thenReturn(false);
        when(moduleRepository.findById("12")).thenReturn(Optional.of(module));

        // then
        assertThrows(ModuleForbiddenException.class, () -> moduleRestController.findModule("12", bob));
    }

    @Test
    void save_shouldSaveTheModule_whenTheUserIsAuthorized(){
        // given
        var module = mock(TerraformModule.class);
        when(module.isAuthorizedFor(bob)).thenReturn(true);
        when(moduleRepository.findById("12")).thenReturn(Optional.of(module));

        // when
        moduleRestController.saveModule("12", module, bob);

        // then
        verify(moduleRepository).findById("12");
        verify(moduleRepository).save(module);
    }

    @Test
    void save_shouldSaveTheModule_forTheAdminUser(){
        // given
        var module = mock(TerraformModule.class);
        when(module.isAuthorizedFor(admin)).thenReturn(true);
        when(moduleRepository.findById("12")).thenReturn(Optional.of(module));

        // when
        moduleRestController.saveModule("12", module, admin);

        // then
        verify(moduleRepository).findById("12");
        verify(moduleRepository).save(module);
    }

    @Test
    void save_shouldThrowAndExecption_whenTheUserIsUnauthorized(){
        // given
        var module = mock(TerraformModule.class);

        // when

        // then
        assertThrows(ModuleNotFoundException.class, () -> moduleRestController.saveModule("12", module, bob));
    }

    @Test
    void createModule_shouldSaveTheModule_forTheGivenUser(){
        // given
        var module = new TerraformModule();
        module.setName("test-creation");
        // when
        moduleRestController.createModule(module, bob);

        // then
        verify(moduleRepository).save(module);
        assertThat(module.getCreatedBy()).isEqualTo(bob);
        assertThat(module.getId()).isNotBlank();
    }

    @Test
    void updateModule_shouldSetUpdatedMetadata(){
        // given
        var module = mock(TerraformModule.class);
        when(module.isAuthorizedFor(bob)).thenReturn(true);
        when(moduleRepository.findById("12")).thenReturn(Optional.of(module));

        // when
        moduleRestController.saveModule("12", module, bob);

        // then
        verify(module).setUpdatedAt(any(LocalDateTime.class));
        verify(module).setUpdatedBy(bob);
    }

}