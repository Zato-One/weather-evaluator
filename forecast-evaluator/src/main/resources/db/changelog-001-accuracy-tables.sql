--liquibase formatted sql

--changeset savic:forecast-evaluator-001-use-schema
ALTER SESSION SET CURRENT_SCHEMA = weather_evaluator;

--changeset savic:forecast-evaluator-001-create-accuracy-daily-table
CREATE TABLE accuracy_daily (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source VARCHAR2(100) NOT NULL,
    location_name VARCHAR2(100) NOT NULL,
    latitude NUMBER(10,6) NOT NULL,
    longitude NUMBER(10,6) NOT NULL,
    forecast_time_utc TIMESTAMP NOT NULL,
    target_date DATE NOT NULL,
    forecast_horizon VARCHAR2(10) NOT NULL,

    -- Temperature accuracy metrics
    temperature_min_mae NUMBER(8,4),
    temperature_max_mae NUMBER(8,4),
    temperature_mean_mae NUMBER(8,4),
    temperature_min_bias NUMBER(8,4),
    temperature_max_bias NUMBER(8,4),
    temperature_mean_bias NUMBER(8,4),

    -- Precipitation accuracy metrics
    precipitation_mae NUMBER(8,4),
    precipitation_bias NUMBER(8,4),

    -- Wind speed accuracy metrics
    wind_speed_mae NUMBER(8,4),
    wind_speed_bias NUMBER(8,4),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_accuracy_daily UNIQUE (source, location_name, forecast_time_utc, target_date)
);

--changeset savic:forecast-evaluator-001-create-accuracy-hourly-table
CREATE TABLE accuracy_hourly (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source VARCHAR2(100) NOT NULL,
    location_name VARCHAR2(100) NOT NULL,
    latitude NUMBER(10,6) NOT NULL,
    longitude NUMBER(10,6) NOT NULL,
    forecast_time_utc TIMESTAMP NOT NULL,
    target_datetime_utc TIMESTAMP NOT NULL,
    forecast_horizon VARCHAR2(10) NOT NULL,

    -- Temperature accuracy metrics
    temperature_mae NUMBER(8,4),
    temperature_bias NUMBER(8,4),

    -- Precipitation accuracy metrics
    precipitation_mae NUMBER(8,4),
    precipitation_bias NUMBER(8,4),

    -- Wind speed accuracy metrics
    wind_speed_mae NUMBER(8,4),
    wind_speed_bias NUMBER(8,4),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_accuracy_hourly UNIQUE (source, location_name, forecast_time_utc, target_datetime_utc)
);

--changeset savic:forecast-evaluator-001-create-accuracy-indexes
CREATE INDEX idx_accuracy_daily_source_location ON accuracy_daily(source, location_name, target_date);
CREATE INDEX idx_accuracy_daily_horizon ON accuracy_daily(forecast_horizon, target_date);
CREATE INDEX idx_accuracy_hourly_source_location ON accuracy_hourly(source, location_name, target_datetime_utc);
CREATE INDEX idx_accuracy_hourly_horizon ON accuracy_hourly(forecast_horizon, target_datetime_utc);