# PROMPT04 Response - Plán dokončení forecast-evaluator služby

## 📋 Aktuální stav
Máme funkční základ forecast-evaluator služby:
- ✅ **Validátor**: Označuje data jako READY_FOR_PROCESSING/INCOMPLETE
- ✅ **Processing states**: Správně nastavené lifecycle stavy
- ✅ **Test data generator**: Fungující generování testovacích dat
- ✅ **Základní infrastruktura**: Konfigurace, databáze, logging

## 🎯 Co zbývá implementovat

### Krok 1: Accuracy calculation tabulky
**Cíl**: Vytvořit `ACCURACY_DAILY` a `ACCURACY_HOURLY` tabulky pro ukládání výsledků

**Implementace**:
- Liquibase migration pro nové tabulky
- Sloupce: location, forecast_date, target_date/time, source, forecast_horizon, accuracy_metrics
- Accuracy metriky: MAE (Mean Absolute Error), RMSE, bias pro temperature/precipitation/wind
- Forecast horizon enum: 1H, 2H, 6H, 12H, 1D, 2D, ..., 14D

### Krok 2: Accuracy calculation engine
**Cíl**: Implementovat core logiku pro vyhodnocení přesnosti předpovědí

**Implementace**:
- `AccuracyCalculator` class s metodami pro hodinové a denní accuracy
- Matching logiku: forecast vs actual weather podle času a lokace
- Matematické metriky: MAE, RMSE, bias calculations
- Forecast horizon calculation (rozdíl mezi forecast_time a target_time)

### Krok 3: AccuracyProcessor service
**Cíl**: Orchestrátor který načte READY_FOR_PROCESSING data a spočítá accuracy

**Implementace**:
- `AccuracyProcessor` class co najde zpracovatelná data
- Batch processing pro performance
- Volání AccuracyCalculator pro jednotlivé záznamy
- Ukládání výsledků do ACCURACY_* tabulek
- Označení původních dat jako ACCURACY_PROCESSED

### Krok 4: Integrace do ForecastEvaluatorRunner
**Cíl**: Přidat accuracy processing do hlavního workflow

**Implementace**:
- Rozšířit `runOnce()` method o accuracy calculation step
- Po validation spustit accuracy processing
- Error handling a logging pro celý pipeline
- Konfigurace pro enable/disable accuracy calculation

### Krok 5: Database persistence layer
**Cíl**: MyBatis mappers a entities pro accuracy tabulky

**Implementace**:
- `AccuracyDailyEntity` a `AccuracyHourlyEntity` data classes
- `AccuracyMapper` interface s XML mapping
- `AccuracyPersistenceService` pro batch inserts
- Connection pooling a transaction management

### Krok 6: Configuration a tuning
**Cíl**: Konfigurovatelnost a optimalizace

**Implementace**:
- Accuracy calculation konfigurace (které metriky počítat)
- Batch size tuning pro performance
- Timeout konfigurace pro dlouhé calculations
- Logging level konfigurace pro debugging

## 🏗 Implementační pořadí

1. **Database schema** (tabulky + migrace)
2. **Accuracy calculation math** (core algoritmy)
3. **Persistence layer** (MyBatis mappers)
4. **AccuracyProcessor** (business logic)
5. **Integration** (do main runner)
6. **Configuration** (tuning a optimalizace)

## 📊 Expected outputs po dokončení

### Accuracy tabulky budou obsahovat:
- **Hourly accuracy**: Přesnost hodinových předpovědí pro 1H až 14D horizons
- **Daily accuracy**: Přesnost denních předpovědí pro 1D až 14D horizons
- **Multiple sources**: Porovnání weather-api vs open-meteo přesnosti
- **Time-based trends**: Jak se accuracy mění s délkou předpovědi
- **Location-based patterns**: Rozdíly v přesnosti mezi lokacemi

### Pipeline bude kompletní:
1. Data fetching (forecast-fetcher, actual-weather-fetcher)
2. Data storage (forecast-writer, actual-weather-writer)
3. **Data validation** (forecast-evaluator validator) ✅
4. **Accuracy calculation** (forecast-evaluator processor) ← TO-DO
5. Future: Reporting API (accuracy-api)

Po dokončení budeme mít funkční systém pro evaluaci přesnosti weather forecast APIs! 🎯