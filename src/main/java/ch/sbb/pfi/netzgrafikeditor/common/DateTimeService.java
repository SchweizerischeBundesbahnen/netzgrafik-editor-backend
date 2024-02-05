package ch.sbb.pfi.netzgrafikeditor.common;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DateTimeService implements NowProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
