--liquibase formatted sql

--changeset irovetskyi:create_exchange_rates_table runOnChange:true
CREATE TABLE IF NOT EXISTS "${database.defaultSchemaName}"."EXCHANGERATES"(
    id serial PRIMARY KEY,
    currency VARCHAR ( 50 ),
    rate DOUBLE PRECISION,
    modify_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
