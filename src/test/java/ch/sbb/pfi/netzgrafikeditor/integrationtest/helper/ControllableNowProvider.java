package ch.sbb.pfi.netzgrafikeditor.integrationtest.helper;

import ch.sbb.pfi.netzgrafikeditor.common.NowProvider;

import java.time.LocalDateTime;

public class ControllableNowProvider implements NowProvider {

    private LocalDateTime now = LocalDateTime.now();

    @Override
    public LocalDateTime now() {
        return this.now;
    }

    public void setNow(LocalDateTime now) {
        this.now = now;
    }
}
