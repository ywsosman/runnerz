package com.example.runnerz.run;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest starts only the web layer; the repository is replaced with a Mockito mock
@WebMvcTest(RunController.class)
class RunControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RunRepository repository;

    private final LocalDateTime start = LocalDateTime.of(2026, 9, 1, 7, 0);
    private final Run run = new Run(1, "Easy Run", start, start.plusMinutes(30), 3, Location.OUTDOOR);

    private static final String VALID_JSON = """
            {"id": 3, "title": "New Run", "startedOn": "2026-09-01T07:00:00",
             "completedOn": "2026-09-01T07:30:00", "miles": 3, "location": "OUTDOOR"}
            """;

    @Test
    void findAllReturnsRuns() throws Exception {
        when(repository.findAll()).thenReturn(List.of(run));

        mvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Easy Run"));
    }

    @Test
    void findByIdReturnsRun() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(run));

        mvc.perform(get("/api/runs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miles").value(3));
    }

    @Test
    void findByIdReturns404WhenMissing() throws Exception {
        when(repository.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/runs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns201() throws Exception {
        mvc.perform(post("/api/runs").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isCreated());

        verify(repository).create(any(Run.class));
    }

    @Test
    void createRejectsBlankTitle() throws Exception {
        String invalid = VALID_JSON.replace("\"New Run\"", "\"\"");

        mvc.perform(post("/api/runs").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());

        verify(repository, never()).create(any());
    }

    @Test
    void updateReturns204() throws Exception {
        mvc.perform(put("/api/runs/3").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isNoContent());

        verify(repository).update(any(Run.class), eq(3));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/runs/1"))
                .andExpect(status().isNoContent());

        verify(repository).delete(1);
    }
}
