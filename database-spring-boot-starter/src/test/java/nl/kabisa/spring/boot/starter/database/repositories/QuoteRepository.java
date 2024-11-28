package nl.kabisa.spring.boot.starter.database.repositories;

import nl.kabisa.spring.boot.starter.database.entity.QuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends JpaRepository<QuoteEntity, Long> {
}
