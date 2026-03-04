DROP TABLE IF EXISTS gas_stations;

CREATE TABLE gas_stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_code VARCHAR(20) NOT NULL,
    region VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    self_service BOOLEAN NOT NULL,
    gasoline_price BIGINT NOT NULL,
    diesel_price BIGINT NOT NULL,
    gasoline_price_yesterday BIGINT NOT NULL,
    diesel_price_yesterday BIGINT NOT NULL
);
