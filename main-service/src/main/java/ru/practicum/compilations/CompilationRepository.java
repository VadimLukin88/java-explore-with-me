package ru.practicum.compilations;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.compilations.models.Compilation;


import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query(value = "SELECT c FROM Compilation AS c WHERE "
                    + ":pinned IS NULL OR c.pinned = :pinned ")
    List<Compilation> findCompilationForUser(Boolean pinned, Pageable pageable);
}
