package spribe.test.task.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spribe.test.task.models.ExchangeRates;

@Repository
public interface ExchangeRatesRepository extends JpaRepository<ExchangeRates, Integer> {
}
