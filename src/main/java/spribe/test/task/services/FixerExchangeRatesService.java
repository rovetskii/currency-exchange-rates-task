package spribe.test.task.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import spribe.test.task.dtos.FixerRatesResponseDto;
import spribe.test.task.models.ExchangeRates;
import spribe.test.task.repositories.ExchangeRatesRepository;


import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toMap;


@Service
public class FixerExchangeRatesService {

    private  Map<String, Map<String,Double>> currenciesExchangeRates = new ConcurrentHashMap<>();
    private final ExchangeRatesRepository exchangeRatesRepository;
    private final FixerApiClient fixerApiClient;

    private final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public FixerExchangeRatesService(ExchangeRatesRepository exchangeRatesRepository, FixerApiClient fixerApiClient) {
        this.exchangeRatesRepository = exchangeRatesRepository;
        this.fixerApiClient = fixerApiClient;
    }

    public Map<String,Double> getExchangeRates(String currency) {
        return currenciesExchangeRates
                .getOrDefault(currency, Collections.EMPTY_MAP);
    }

    public void setCurrency(String currency) {
        currenciesExchangeRates.putIfAbsent(currency, new HashMap<>());
    }

    public Set<String> getCurrencies() {
        return currenciesExchangeRates.keySet().stream().collect(Collectors.toSet());
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Async
    public CompletableFuture<Void> currencyRateProcess() {
        Set<String> currencies = currenciesExchangeRates.keySet();
        List<CompletableFuture<Optional<FixerRatesResponseDto>>> fixerRatesDtoFutures =
                currencies
                        .stream()
                        .map(currency ->
                                CompletableFuture.supplyAsync(() ->
                                        Optional.ofNullable(fixerApiClient.getCurrencyRates(currency).getBody()),
                                        pool
                                )
                        )
                        .toList();

        CompletableFuture<List<Optional<FixerRatesResponseDto>>> allFixerRatesDtoFutures =
                CompletableFuture.allOf(fixerRatesDtoFutures.toArray(new CompletableFuture[fixerRatesDtoFutures.size()]))
                        .thenApply(e -> fixerRatesDtoFutures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                        );

        allFixerRatesDtoFutures.thenAccept(
                fixerRatesResponseDtos -> {
                    List<ExchangeRates> newCurrencyRates =
                            fixerRatesResponseDtos
                                    .stream()
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .flatMap(fixerRatesResponseDto -> fixerRatesResponseDto.rates().entrySet().stream())
                                    .map(entry -> new ExchangeRates(entry.getKey(), entry.getValue(), now()))
                                    .toList();
                    if(!newCurrencyRates.isEmpty()) {
                        exchangeRatesRepository.saveAll(newCurrencyRates);
                    }
                }
        );

        return allFixerRatesDtoFutures.thenAccept(
                fixerRatesResponseDtos -> {
                    Map<String, Map<String, Double>> newExchangeRates =
                            fixerRatesResponseDtos
                                    .stream()
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(toMap(FixerRatesResponseDto::base, FixerRatesResponseDto::rates));
                    newExchangeRates
                            .keySet()
                            .stream()
                            .forEach(currency -> {
                                currenciesExchangeRates.merge(currency, newExchangeRates.get(currency), (oldValue, newValue) -> newValue);
                            });
                }
        );
    }
}
