package spribe.test.task.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spribe.test.task.dtos.CurrencyRequestDto;
import spribe.test.task.services.FixerExchangeRatesService;
import java.util.Map;
import java.util.Set;

import static spribe.test.task.common.ResponseMessages.*;

@RestController
@RequestMapping("/currencies")
public class CurrencyRateController {
    private final FixerExchangeRatesService fixerExchangeRatesService;
    public CurrencyRateController(FixerExchangeRatesService fixerExchangeRatesService) {
        this.fixerExchangeRatesService = fixerExchangeRatesService;
    }

    @GetMapping
    public ResponseEntity getCurrencies() {
        Set<String> currencies = fixerExchangeRatesService.getCurrencies();
        if(currencies.isEmpty()){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CURRENCIES_NOT_ADDED);
        }
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/rates/{base}")
    public ResponseEntity getExchangeRatesForCurrency(@PathVariable(value = "base")  String currency) {
        Map<String, Double> exchangeRate = fixerExchangeRatesService.getExchangeRates(currency);
        if(exchangeRate.isEmpty()) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format(EXCHANGE_RATES_NOT_RECEIVED, currency));
        }
        return ResponseEntity.ok(exchangeRate);
    }

    @PostMapping
    public ResponseEntity<String> addCurrencyRates(@RequestBody CurrencyRequestDto currencyRequestDto) {
        String currency = currencyRequestDto.currency();
        fixerExchangeRatesService.setCurrency(currency);
        fixerExchangeRatesService.currencyRateProcess();
        return  ResponseEntity.ok(String.format(CURRENCIES_IS_ADDED, currency));
    }
}
