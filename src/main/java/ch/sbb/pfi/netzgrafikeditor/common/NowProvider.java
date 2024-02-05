package ch.sbb.pfi.netzgrafikeditor.common;

import java.time.LocalDateTime;

public interface NowProvider {
    LocalDateTime now();
}
