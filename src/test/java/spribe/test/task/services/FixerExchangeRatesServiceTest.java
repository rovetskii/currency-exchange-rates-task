package spribe.test.task.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import spribe.test.task.dtos.FixerRatesResponseDto;
import spribe.test.task.models.ExchangeRates;
import spribe.test.task.repositories.ExchangeRatesRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class FixerExchangeRatesServiceTest extends FixerExchangeRatesServiceBaseTest {
    private final String TEST_CURRENCY = "EUR";
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private FixerApiClient fixerApiClientMock;
    @Autowired
    ExchangeRatesRepository exchangeRatesRepository;
    @Autowired
    private FixerExchangeRatesService fixerExchangeRatesService;

    @BeforeEach
    public void init() {
        exchangeRatesRepository.deleteAll();
    }

    @Test
    public void testCurrencyRatesProcessWhenNonNullableResponseEntity() throws IOException {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("ExchangeRatesSuccessful.json");
        byte[] bytes = FileCopyUtils.copyToByteArray(resource);
        FixerRatesResponseDto fixerRatesResponseDto =
                objectMapper.readValue(
                        bytes,
                        FixerRatesResponseDto.class
                );
        when(fixerApiClientMock.getCurrencyRates(TEST_CURRENCY))
                .thenReturn(ResponseEntity.ok(fixerRatesResponseDto));
        fixerExchangeRatesService.setCurrency(TEST_CURRENCY);

        fixerExchangeRatesService.currencyRateProcess().join();

        assertThat(fixerExchangeRatesService.getExchangeRates(TEST_CURRENCY))
                .containsAllEntriesOf(fixerRatesResponseDto.rates());
        Map<String, Double> exchangeRatesActual =
                exchangeRatesRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ExchangeRates::getCurrency, ExchangeRates::getRate));
        assertThat(exchangeRatesActual).isEqualTo(fixerRatesResponseDto.rates());
    }

    @Test
    public void testCurrencyRatesProcessWhenNullableResponseEntity() {
        when(fixerApiClientMock.getCurrencyRates(TEST_CURRENCY))
                .thenReturn(ResponseEntity.ok(null));
        fixerExchangeRatesService.setCurrency(TEST_CURRENCY);

        fixerExchangeRatesService.currencyRateProcess().join();

        assertThat(fixerExchangeRatesService.getExchangeRates(TEST_CURRENCY))
                .containsAllEntriesOf(Map.of());
        Map<String, Double> exchangeRatesActual =
                exchangeRatesRepository.findAll()
                        .stream()
                        .collect(Collectors.toMap(ExchangeRates::getCurrency, ExchangeRates::getRate));
        assertThat(exchangeRatesActual).isEqualTo(Map.of());
    }
}
