package server.database;

import commons.CardSubtask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubtaskRepository extends JpaRepository<CardSubtask, Long> {
}
