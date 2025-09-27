--liquibase formatted sql

--changeset savic:actual-weather-writer-002-add-processing-state
ALTER TABLE actual_weather_observations ADD processing_state VARCHAR2(25) DEFAULT 'READY_FOR_PROCESSING' NOT NULL;

--changeset savic:actual-weather-writer-002-add-processing-state-index
CREATE INDEX idx_actual_processing_state ON actual_weather_observations(location_name, observed_time_utc, processing_state);