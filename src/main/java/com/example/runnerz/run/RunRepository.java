package com.example.runnerz.run;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Repository
public class RunRepository {

    private final JdbcClient jdbcClient;

    public RunRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Run> findAll() {
        return jdbcClient.sql("SELECT * FROM run")
                .query(Run.class)
                .list();
    }

    public Optional<Run> findById(Integer id) {
        return jdbcClient.sql("SELECT * FROM run WHERE id = :id")
                .param("id", id)
                .query(Run.class)
                .optional();
    }

    public List<Run> findByLocation(Location location) {
        return jdbcClient.sql("SELECT * FROM run WHERE location = :location")
                .param("location", location.name())
                .query(Run.class)
                .list();
    }

    public void create(Run run) {
        int rows = jdbcClient.sql("""
                        INSERT INTO run (id, title, started_on, completed_on, miles, location)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """)
                .params(run.id(), run.title(), run.startedOn(), run.completedOn(), run.miles(), run.location().name())
                .update();
        Assert.state(rows == 1, "Failed to create run " + run.title());
    }

    public void update(Run run, Integer id) {
        int rows = jdbcClient.sql("""
                        UPDATE run
                        SET title = ?, started_on = ?, completed_on = ?, miles = ?, location = ?
                        WHERE id = ?
                        """)
                .params(run.title(), run.startedOn(), run.completedOn(), run.miles(), run.location().name(), id)
                .update();
        if (rows == 0) {
            throw new RunNotFoundException(id);
        }
    }

    public void delete(Integer id) {
        int rows = jdbcClient.sql("DELETE FROM run WHERE id = :id")
                .param("id", id)
                .update();
        if (rows == 0) {
            throw new RunNotFoundException(id);
        }
    }

    public int count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM run")
                .query(Integer.class)
                .single();
    }

    public void saveAll(List<Run> runs) {
        runs.forEach(this::create);
    }
}
