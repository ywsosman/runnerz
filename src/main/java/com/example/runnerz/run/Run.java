package com.example.runnerz.run;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record Run(Integer id,
                  @NotBlank String title,
                  @NotNull LocalDateTime startedOn,
                  @NotNull LocalDateTime completedOn,
                  @Positive Integer miles,
                  @NotNull Location location
) {

    // compact constructor: runs every time a Run is created
    public Run {
        if (startedOn != null && completedOn != null && !completedOn.isAfter(startedOn)) {
            throw new IllegalArgumentException("completedOn must be after startedOn");
        }
    }
}
