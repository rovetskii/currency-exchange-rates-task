package spribe.test.task.dtos;
import java.time.LocalDate;
import java.util.Map;

public record FixerRatesResponseDto(String base,  Map<String, Double> rates, LocalDate date){}
