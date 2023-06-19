package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository repository;

	private MovieDTO movieDTO;
	private MovieEntity movieEntity;
	private PageImpl<MovieEntity> pageMovies;
	private Long existMovieId, nonExistMovieId, dependentId;


	@BeforeEach
	void setUp() throws Exception {
		movieEntity = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movieEntity);
		pageMovies = new PageImpl<>(List.of(movieEntity));
		existMovieId = 1L;
		nonExistMovieId = 2L;
		dependentId = 3L;

		Mockito.when(repository.searchByTitle(any(), (Pageable) any())).thenReturn(pageMovies);

		Mockito.when(repository.findById(existMovieId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(repository.findById(nonExistMovieId)).thenReturn(Optional.empty());

		Mockito.when(repository.save(any())).thenReturn(movieEntity);

		Mockito.when(repository.getReferenceById(existMovieId)).thenReturn(movieEntity);
		Mockito.when(repository.getReferenceById(nonExistMovieId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(repository.existsById(existMovieId)).thenReturn(true);
		Mockito.when(repository.existsById(dependentId)).thenReturn(true);
		Mockito.when(repository.existsById(nonExistMovieId)).thenReturn(false);
		Mockito.doNothing().when(repository).deleteById(existMovieId);
		Mockito.doThrow(DatabaseException.class).when(repository).deleteById(nonExistMovieId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {

		Pageable pageable = PageRequest.of(0, 12);
		Page<MovieDTO> page = service.findAll(movieDTO.getTitle(), pageable);

		Assertions.assertNotNull(page);
		Assertions.assertEquals(page.getSize(), 1);
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {

		MovieDTO dto = service.findById(existMovieId);

		Assertions.assertNotNull(dto);
		Assertions.assertEquals(dto.getId(), existMovieId);
		Assertions.assertEquals(dto.getTitle(), movieEntity.getTitle());
		Assertions.assertEquals(dto.getScore(), movieEntity.getScore());
		Assertions.assertEquals(dto.getCount(), movieEntity.getCount());
		Assertions.assertEquals(dto.getImage(), movieEntity.getImage());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistMovieId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {

		MovieDTO dto = service.insert(movieDTO);

		Assertions.assertNotNull(dto);
		Assertions.assertEquals(dto.getId(), existMovieId);
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {

		MovieDTO dto = service.update(existMovieId, movieDTO);

		Assertions.assertNotNull(dto);
		Assertions.assertEquals(dto.getId(), existMovieId);
		Assertions.assertEquals(dto.getTitle(), movieEntity.getTitle());
		Assertions.assertEquals(dto.getScore(), movieEntity.getScore());
		Assertions.assertEquals(dto.getCount(), movieEntity.getCount());
		Assertions.assertEquals(dto.getImage(), movieEntity.getImage());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistMovieId, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {

		Assertions.assertDoesNotThrow(() -> {
			service.delete(existMovieId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistMovieId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
	}
}
