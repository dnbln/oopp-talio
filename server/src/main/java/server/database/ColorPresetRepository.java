package server.database;

import commons.ColorPreset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorPresetRepository extends JpaRepository<ColorPreset, Long> {
}
