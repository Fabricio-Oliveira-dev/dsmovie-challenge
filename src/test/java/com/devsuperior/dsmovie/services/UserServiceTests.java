package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

	@Mock
	private UserRepository repository;

	@Mock
	private CustomUserUtil userUtil;

	private String existingEmail, nonExistingEmail;
	private UserEntity user;
	private List<UserDetailsProjection> userProjection = new ArrayList<>();

	@BeforeEach
	void setUp() {
		existingEmail = "maria@gmail.com";
		nonExistingEmail = "joao@gmail.com";
		user = UserFactory.createUserEntity();
		userProjection = UserDetailsFactory.createCustomAdminUser(existingEmail);

		Mockito.when(repository.searchUserAndRolesByUsername(existingEmail)).thenReturn(userProjection);
		Mockito.when(repository.searchUserAndRolesByUsername(nonExistingEmail)).thenReturn(new ArrayList<>());

		Mockito.when(repository.findByUsername(existingEmail)).thenReturn(Optional.of(user));
		Mockito.when(repository.findByUsername(nonExistingEmail)).thenReturn(Optional.empty());
	}

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {
		Mockito.when(userUtil.getLoggedUsername()).thenReturn(existingEmail);
		user = service.authenticated();

		Assertions.assertNotNull(user);
		Assertions.assertEquals(user.getUsername(), existingEmail);
	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		Mockito.doThrow(ClassCastException.class).when(userUtil).getLoggedUsername();

		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.authenticated();
		});
	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
		UserDetails userDetails = service.loadUserByUsername(existingEmail);

		Assertions.assertNotNull(userDetails);
		Assertions.assertEquals(userDetails.getUsername(), existingEmail);
	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.loadUserByUsername(nonExistingEmail);
		});
	}
}
