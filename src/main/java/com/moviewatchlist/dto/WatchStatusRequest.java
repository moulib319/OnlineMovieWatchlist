package com.moviewatchlist.dto;

import jakarta.validation.constraints.NotNull;

public class WatchStatusRequest {

    @NotNull(message = "Watched value is required")
    private Boolean watched;

    public Boolean getWatched() {
        return watched;
    }

    public void setWatched(Boolean watched) {
        this.watched = watched;
    }
}
