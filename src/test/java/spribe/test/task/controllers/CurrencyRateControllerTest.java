package spribe.test.task.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.util.FileCopyUtils;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import spribe.test.task.dtos.CurrencyRequestDto;
import spribe.test.task.dtos.FixerRatesResponseDto;
import spribe.test.task.services.FixerExchangeRatesService;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static spribe.test.task.common.ResponseMessages.*;
import static spribe.test.task.utils.MapperUtil.getObjectMapper;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CurrencyRateController.class)
@AutoConfigureMockMvc(addFilters = false)
class CurrencyRateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    FixerExchangeRatesService fixerExchangeRatesService;
    ObjectMapper objectMapper = getObjectMapper();
    @Test
    public void getCurrenciesTest() throws Exception {
        when(fixerExchangeRatesService.getCurrencies()).thenReturn(Set.of("EUR"));
        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("EUR")));
    }

    @Test
    public void getExchangeRatesForCurrencyWhenCurrenciesIsNotExistsTest() throws Exception {
        String testCurrency = "EUR";
        when(fixerExchangeRatesService.getCurrencies()).thenReturn(Set.of());
        mockMvc.perform(get("/currencies"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(
                        String.format(CURRENCIES_NOT_ADDED, testCurrency))));
    }

    @Test
    public void getExchangeRatesForCurrencyTest() throws Exception {
        String testCurrency = "EUR";
        InputStream resource = getClass().getClassLoader().getResourceAsStream("ExchangeRatesSuccessful.json");
        byte[] bytes = FileCopyUtils.copyToByteArray(resource);
        FixerRatesResponseDto fixerRatesResponseDto =
                objectMapper.readValue(bytes, FixerRatesResponseDto.class);
        when(fixerExchangeRatesService.getExchangeRates(testCurrency))
                .thenReturn(fixerRatesResponseDto.rates());
        mockMvc.perform(get("/currencies/rates/{base}", testCurrency))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(fixerRatesResponseDto.rates())));
    }

    @Test
    public void getExchangeRatesForCurrencyWhenExchangeRatesIsNotReceivedTest() throws Exception {
        String testCurrency = "EUR";
        when(fixerExchangeRatesService.getExchangeRates(testCurrency))
                .thenReturn(Map.of());
        mockMvc.perform(get("/currencies/rates/{base}", testCurrency))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(
                        String.format(EXCHANGE_RATES_NOT_RECEIVED, testCurrency))));
    }

    @Test
    public void postNewCurrencyTest() throws Exception {
        String content = FileUtils.readFileToString(
                new File("src/test/resources/PostCurrencyRequest.json"),
                StandardCharsets.UTF_8
        );
        CurrencyRequestDto currencyRequestDto =
                objectMapper.readValue(content, CurrencyRequestDto.class);
        mockMvc.perform(post("/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        String.format(CURRENCIES_IS_ADDED, currencyRequestDto.currency()))));
    }
}
