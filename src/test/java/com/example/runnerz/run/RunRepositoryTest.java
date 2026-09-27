package com.example.runnerz.run;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// @JdbcTest starts only the database parts of Spring, and rolls back after each test
@JdbcTest
@Import(RunRepository.class)
class RunRepositoryTest {

    @Autowired
    RunRepository repository;

    private final LocalDateTime start = LocalDateTime.of(2026, 9, 1, 7, 0);

    @BeforeEach
    void setUp() {
        repository.create(new Run(1, "Easy Run", start, start.plusMinutes(30), 3, Location.OUTDOOR));
        repository.create(new Run(2, "Treadmill Run", start, start.plusMinutes(45), 5, Location.INDOOR));
    }

    @Test
    void findAllReturnsEveryRun() {
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void findByIdReturnsTheRun() {
        assertThat(repository.findById(1))
                .isPresent()
                .get()
                .extracting(Run::title)
                .isEqualTo("Easy Run");
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertThat(repository.findById(99)).isEmpty();
    }

    @Test
    void findByLocationFilters() {
        List<Run> indoor = repository.findByLocation(Location.INDOOR);
        assertThat(indoor).extracting(Run::id).containsExactly(2);
    }

    @Test
    void updateChangesTheRun() {
        repository.update(new Run(1, "Updated Run", start, start.plusHours(1), 7, Location.INDOOR), 1);

        Run updated = repository.findById(1).orElseThrow();
        assertThat(updated.title()).isEqualTo("Updated Run");
        assertThat(updated.miles()).isEqualTo(7);
    }

    @Test
    void updateThrowsWhenMissing() {
        Run run = new Run(99, "Ghost Run", start, start.plusMinutes(10), 1, Location.OUTDOOR);
        assertThatThrownBy(() -> repository.update(run, 99)).isInstanceOf(RunNotFoundException.class);
    }

    @Test
    void deleteRemovesTheRun() {
        repository.delete(1);
        assertThat(repository.count()).isEqualTo(1);
    }
}
