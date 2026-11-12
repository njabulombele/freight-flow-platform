package com.freightflow.incidents;

import jakarta.validation.constraints.NotBlank;

public record ResolveRequest(
        @NotBlank String resolution
) {
}
