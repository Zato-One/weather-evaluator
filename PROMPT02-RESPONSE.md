# PROMPT02 Response - Implementační detaily validátoru

## ✅ Plně souhlasím s přístupem

Návrh na postupnou implementaci začínající s validátorem je velmi rozumný a praktický.

## Processing State Enum - výborný návrh!

```kotlin
enum class ProcessingState {
    STORED,               // ✅ Právě uloženo z Kafka
    READY_FOR_PROCESSING, // ✅ Validováno, připraveno k accuracy evaluation
    ACCURACY_PROCESSED,   // ✅ Vyhodnoceno a uloženo do accuracy tabulek
    INCOMPLETE            // ✅ Nekompletní data (perfect pro denní)
}
```

Pojmenování stavů je intuitivní a pokrývá všechny potřebné scénáře.

## Validační logika - perfektně navrženo

### Hodinová data (FORECAST_HOURLY, ACTUAL_WEATHER_OBSERVATIONS)
- **Automaticky READY**: Správný přístup - 1:1 matching je jednoduché ✅
- **Logika**: `STORED` → `READY_FOR_PROCESSING` ihned ✅

### Denní data (FORECAST_DAILY)
- **22/24 hodin threshold**: Skvělá volba - 91.7% kompletnost ✅
- **Konfigurovatelný**: Flexibilita pro budoucí tuning ✅
- **Výsledky**: `READY_FOR_PROCESSING` vs `INCOMPLETE` ✅

## Implementační plán

### 1. Databázové změny (Liquibase)
```sql
-- Přidat do všech tří tabulek:
ALTER TABLE FORECAST_DAILY ADD processing_state VARCHAR2(20) DEFAULT 'STORED';
ALTER TABLE FORECAST_HOURLY ADD processing_state VARCHAR2(20) DEFAULT 'STORED';
ALTER TABLE ACTUAL_WEATHER_OBSERVATIONS ADD processing_state VARCHAR2(20) DEFAULT 'STORED';

-- Index pro rychlé dotazy
CREATE INDEX idx_processing_state ON FORECAST_DAILY(location_id, forecast_date, processing_state);
```

### 2. Konfigurace validátoru
```kotlin
data class ValidatorConfig(
    val intervalMinutes: Int = 15,
    val dailyCompletenessThreshold: Int = 22, // z 24 hodin
    val batchSize: Int = 1000
)
```

### 3. Validátor workflow
1. **Query STORED records** (batch po 1000)
2. **Hodinová data**: Označit jako `READY_FOR_PROCESSING`
3. **Denní data**: Count hodin pro každý den → `READY_FOR_PROCESSING` nebo `INCOMPLETE`
4. **Update states** v databázi
5. **Placeholder** pro accuracy processor

## 💡 Dodatečné návrhy

### Monitoring
- Counter metrik pro každý state transition
- Alert při vysokém počtu `INCOMPLETE` records

### Performance
- Batch updates pro lepší DB performance
- Separate queries pro hodinová vs denní data

### Error handling
- Retry mechanismus pro DB failures
- Logging všech state transitions

Přístup je perfektně navržený pro postupnou implementaci! 🎯