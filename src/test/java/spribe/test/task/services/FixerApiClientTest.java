package spribe.test.task.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import spribe.test.task.dtos.FixerRatesResponseDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.GET;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FixerApiClientTest {
    @Value("${fixer.accessKey}")
    private String accessKey;
    @Value("${fixer.baseUrl}")
    private String baseUrl;
    private final String ACCESS_KEY = "access_key";
    private final String BASE_CURRENCY = "base";
    private final String LATEST_EXCHANGE_RATES = "/latest";
    private final String TEST_CURRENCY = "EUR";
    @MockBean
    private RestTemplate restTemplate;
    @Autowired
    private FixerApiClient fixerApiClient;
    @Autowired
    private ObjectMapper objectMapper;
    private URI testUri;

    @BeforeEach
    public void setup() {
        testUri = UriComponentsBuilder.fromUriString(baseUrl + LATEST_EXCHANGE_RATES)
                .queryParam(ACCESS_KEY, accessKey)
                .queryParamIfPresent(BASE_CURRENCY, Optional.ofNullable(TEST_CURRENCY))
                .build()
                .encode()
                .toUri();
    }
    @Test
    public void testGetCurrencyRatesWhenReturnResponseEntityWithNonNullableBody() throws IOException {

        InputStream resource = getClass().getClassLoader().getResourceAsStream("ExchangeRatesSuccessful.json");
        byte[] bytes = FileCopyUtils.copyToByteArray(resource);
        FixerRatesResponseDto fixerRatesResponseDto =
                objectMapper.readValue(
                        bytes,
                        FixerRatesResponseDto.class
                );

        lenient().when(restTemplate.exchange(eq(testUri),
                                eq(GET),
                                any(HttpEntity.class),
                                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(fixerRatesResponseDto, HttpStatus.OK));

        ResponseEntity<FixerRatesResponseDto> currencyRates = fixerApiClient.getCurrencyRates(TEST_CURRENCY);
        verify(restTemplate).exchange(any(URI.class), eq(GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        assertThat(currencyRates.getBody()).isNotNull();
        assertThat(currencyRates.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fixerRatesResponseDto).usingRecursiveComparison().isEqualTo(currencyRates.getBody());
    }

    @Test
    public void testGetCurrencyRatesWhenReturnResponseEntityWithNullableBody() {
        lenient().when(restTemplate.exchange(eq(testUri),
                        eq(GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NO_CONTENT));

        ResponseEntity<FixerRatesResponseDto> currencyRates = fixerApiClient.getCurrencyRates(TEST_CURRENCY);
        verify(restTemplate).exchange(any(URI.class), eq(GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        assertThat(currencyRates.getBody()).isNull();
        assertThat(currencyRates.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetCurrencyRatesWhenRestException() {
        lenient().when(restTemplate.exchange(eq(testUri),
                        eq(GET),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()));
        ResponseEntity<FixerRatesResponseDto> currencyRates = fixerApiClient.getCurrencyRates(TEST_CURRENCY);
        verify(restTemplate).exchange(eq(testUri), eq(GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        assertThat(currencyRates.getBody()).isNull();
        assertThat(currencyRates.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
