package ru.practicum.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.categories.models.Category;
import ru.practicum.compilations.models.Compilation;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
