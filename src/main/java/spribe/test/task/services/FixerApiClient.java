package spribe.test.task.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import spribe.test.task.dtos.FixerRatesResponseDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class FixerApiClient {
    Logger logger = LoggerFactory.getLogger(FixerApiClient.class);
    private final String ACCESS_KEY = "access_key";
    private final String BASE_CURRENCY = "base";
    private final String LATEST_EXCHANGE_RATES = "/latest";
    @Value("${fixer.accessKey}")
    private String accessKey;
    @Value("${fixer.baseUrl}")
    private String baseUrl;

    @Autowired
    private final RestTemplate restTemplate;

    public FixerApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<FixerRatesResponseDto> getCurrencyRates(String currency)  {
        try {
            URI uriComponentsBuilder = UriComponentsBuilder
                    .fromUriString(baseUrl + LATEST_EXCHANGE_RATES)
                    .queryParam(ACCESS_KEY, accessKey)
                    .queryParamIfPresent(BASE_CURRENCY, Optional.ofNullable(currency))
                    .build()
                    .encode()
                    .toUri();
            return restTemplate.exchange(uriComponentsBuilder,
                            HttpMethod.GET,
                            new HttpEntity<>(new HttpHeaders()),
                            new ParameterizedTypeReference<>() {}
                    );
        } catch (RestClientException ex) {
           //logging error
            logger.error(String.format("Error %s occured when getting currency rates cause %s. Time is %s", ex.getMessage(), ex.getCause(), LocalDateTime.now()));
            return new ResponseEntity<>(null, null, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
