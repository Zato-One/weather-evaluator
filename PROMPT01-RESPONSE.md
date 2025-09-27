# PROMPT01 Response - Návrh forecast-evaluator služby

## Analýza návrhu
Návrh na 5. službu `forecast-evaluator` je velmi dobře promyšlený a logicky strukturovaný. Všechny klíčové body dávají smysl.

## ✅ Souhlasím s návrhem

### Architektura
- **Cron-based služba** běžící každých 15 minut (konfigurovatelné) ✅
- **Dvě nové tabulky**: `ACCURACY_DAILY` a `ACCURACY_HOURLY` ✅
- **Označování zpracovaných dat** v existujících tabulkách ✅
- **Validátor pro kompletnost** dat před vyhodnocením ✅

### Vyhodnocované metriky
- **Denní předpovědi**: přesnost pro 1-14 dní dopředu ✅
- **Hodinové předpovědi**: přesnost pro 1h-14 dní dopředu ✅
- **Agregované funkce**: min, max, mean pro denní vyhodnocení ✅

### Potřebné nové komponenty
- **ForecastLength enum** pro délky předpovědí ✅
- **Processing flag** v existujících tabulkách ✅

## 💡 Připomínky a rozšíření

### 1. Databázové optimalizace
- Composite index na `(location_id, forecast_date, processed)` pro rychlé dotazy
- Zvážit partitioning tabulek podle data pro lepší výkon

### 2. Validace kompletnosti
- Definovat threshold pro "kompletnost" (např. min 20/24 hodin pro denní validaci)
- Validátor může být součást forecast-evaluator služby (ne samostatná služba)

### 3. Accuracy metriky
- Implementovat různé typy přesnosti: MAE, RMSE, bias
- Možnost konfigurace váhování různých parametrů (teplota vs vlhkost)

### 4. Error handling
- Handling chybějících nebo neúplných dat
- Retry mechanismus pro failed processing
- Monitoring a alerting pro detekci problémů

### 5. Škálovatelnost
- Batch processing pro lepší výkon
- Možnost paralelního zpracování různých lokací
- Redis cache pro často používané aggregace

## 🎯 Doporučené implementační kroky

1. **Databázové schéma**
   - Přidat processed flag do existujících tabulek
   - Vytvořit ACCURACY_DAILY a ACCURACY_HOURLY tabulky
   - Implementovat ForecastLength enum

2. **Core služba**
   - Založit forecast-evaluator modul
   - Implementovat cron scheduler
   - Vytvořit data validation logic

3. **Accuracy algorithms**
   - Implementovat MAE, RMSE calculations
   - Handling různých forecast horizons
   - Agregace pro denní metriky

4. **Monitoring**
   - Metrics pro processing performance
   - Health checks
   - Error tracking

Návrh je solidní a ready pro implementaci! 🚀