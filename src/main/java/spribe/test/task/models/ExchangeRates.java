package spribe.test.task.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="`EXCHANGERATES`")
public class ExchangeRates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String currency;
    private Double rate;
    @Column(name="modify_datetime")
    private LocalDateTime date;

    public ExchangeRates(String currency, Double rate, LocalDateTime localDateTime) {
        this.currency = currency;
        this.rate = rate;
        this.date = localDateTime;
    }

    public ExchangeRates() {
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
