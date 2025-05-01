--liquibase formatted sql

--changeset savic:001-create-user splitStatements:false endDelimiter:/
--comment Initial changeset for creating user, tables and indexes
DECLARE
  v_exists INTEGER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM dba_users WHERE username = 'WEATHER_EVALUATOR';
  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE 'CREATE USER weather_evaluator IDENTIFIED BY "weather-evaluator"';
    EXECUTE IMMEDIATE 'GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO weather_evaluator';
  END IF;
END;
/

--changeset savic:001-use-schema
ALTER SESSION SET CURRENT_SCHEMA = weather_evaluator;

--changeset savic:001-create-table-daily
CREATE TABLE forecast_daily (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source VARCHAR2(100) NOT NULL,
    location_name VARCHAR2(100) NOT NULL,
    latitude NUMBER(10,6) NOT NULL,
    longitude NUMBER(10,6) NOT NULL,
    forecast_time_utc TIMESTAMP NOT NULL,
    target_date DATE NOT NULL,
    temperature_min_c NUMBER(5,2) NOT NULL,
    temperature_max_c NUMBER(5,2) NOT NULL,
    temperature_mean_c NUMBER(5,2) NOT NULL,
    precipitation_mm_sum NUMBER(7,2) NOT NULL,
    wind_speed_kph_10m_max NUMBER(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_forecast UNIQUE (source, location_name, forecast_time_utc, target_date)
);

--changeset savic:001-create-table-hourly
CREATE TABLE forecast_hourly (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source VARCHAR2(100) NOT NULL,
    location_name VARCHAR2(100) NOT NULL,
    latitude NUMBER(10,6) NOT NULL,
    longitude NUMBER(10,6) NOT NULL,
    forecast_time_utc TIMESTAMP NOT NULL,
    target_datetime_utc TIMESTAMP NOT NULL,
    temperature_c NUMBER(5,2) NOT NULL,
    precipitation_mm NUMBER(7,2) NOT NULL,
    wind_speed_kph_10m NUMBER(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_hourly_forecast UNIQUE (source, location_name, forecast_time_utc, target_datetime_utc)
);

--changeset savic:001-create-indexes
CREATE INDEX idx_daily_source_location ON forecast_daily(source, location_name);
CREATE INDEX idx_daily_target_date ON forecast_daily(target_date);
CREATE INDEX idx_hourly_source_location ON forecast_hourly(source, location_name);
CREATE INDEX idx_hourly_target_datetime ON forecast_hourly(target_datetime_utc);