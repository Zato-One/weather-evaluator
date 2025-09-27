--liquibase formatted sql

--changeset savic:forecast-evaluator-002-add-accuracy-percentages
ALTER TABLE accuracy_hourly ADD (
    temperature_accuracy_percent NUMBER(5,2),
    precipitation_accuracy_percent NUMBER(5,2),
    wind_speed_accuracy_percent NUMBER(5,2)
);

ALTER TABLE accuracy_daily ADD (
    temperature_min_accuracy_percent NUMBER(5,2),
    temperature_max_accuracy_percent NUMBER(5,2),
    temperature_mean_accuracy_percent NUMBER(5,2),
    precipitation_accuracy_percent NUMBER(5,2),
    wind_speed_accuracy_percent NUMBER(5,2)
);

COMMENT ON COLUMN accuracy_hourly.temperature_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±2°C';
COMMENT ON COLUMN accuracy_hourly.precipitation_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±1mm';
COMMENT ON COLUMN accuracy_hourly.wind_speed_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±3km/h';

COMMENT ON COLUMN accuracy_daily.temperature_min_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±2°C';
COMMENT ON COLUMN accuracy_daily.temperature_max_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±2°C';
COMMENT ON COLUMN accuracy_daily.temperature_mean_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±2°C';
COMMENT ON COLUMN accuracy_daily.precipitation_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±2mm';
COMMENT ON COLUMN accuracy_daily.wind_speed_accuracy_percent IS 'Tolerance-based accuracy: % of forecasts within ±3km/h';