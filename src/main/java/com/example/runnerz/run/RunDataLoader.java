package com.example.runnerz.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// fills the database from data/runs.json on startup, but only if it's empty
@Component
public class RunDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RunDataLoader.class);
    private final RunRepository runRepository;
    private final ObjectMapper objectMapper;

    public RunDataLoader(RunRepository runRepository, ObjectMapper objectMapper) {
        this.runRepository = runRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws IOException {
        if (runRepository.count() > 0) {
            log.info("Runs already in the database, skipping JSON load");
            return;
        }
        try (InputStream in = new ClassPathResource("data/runs.json").getInputStream()) {
            List<Run> runs = List.of(objectMapper.readValue(in, Run[].class));
            runRepository.saveAll(runs);
            log.info("Loaded {} runs from data/runs.json", runs.size());
        }
    }
}
