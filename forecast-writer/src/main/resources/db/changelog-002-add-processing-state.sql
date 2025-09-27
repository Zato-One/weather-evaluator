--liquibase formatted sql

--changeset savic:forecast-writer-002-add-processing-state-daily
ALTER TABLE forecast_daily ADD processing_state VARCHAR2(25) DEFAULT 'STORED' NOT NULL;

--changeset savic:forecast-writer-002-add-processing-state-hourly
ALTER TABLE forecast_hourly ADD processing_state VARCHAR2(25) DEFAULT 'READY_FOR_PROCESSING' NOT NULL;

--changeset savic:forecast-writer-002-add-processing-state-indexes
CREATE INDEX idx_daily_processing_state ON forecast_daily(location_name, target_date, processing_state);
CREATE INDEX idx_hourly_processing_state ON forecast_hourly(location_name, target_datetime_utc, processing_state);