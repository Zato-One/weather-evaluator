--liquibase formatted sql

--changeset savic:001-use-schema
ALTER SESSION SET CURRENT_SCHEMA = weather_evaluator;

--changeset savic:001-create-table-actual-weather
CREATE TABLE actual_weather_observations (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source VARCHAR2(100) NOT NULL,
    location_name VARCHAR2(100) NOT NULL,
    latitude NUMBER(10,6) NOT NULL,
    longitude NUMBER(10,6) NOT NULL,
    observed_time_utc TIMESTAMP NOT NULL,
    temperature_c NUMBER(5,2) NOT NULL,
    precipitation_mm NUMBER(7,2) NOT NULL,
    wind_speed_kph_10m NUMBER(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_actual_weather UNIQUE (source, location_name, observed_time_utc)
);

--changeset savic:001-create-indexes
CREATE INDEX idx_actual_source_location ON actual_weather_observations(source, location_name);
CREATE INDEX idx_actual_observed_time ON actual_weather_observations(observed_time_utc);