# PROMPT03 Response - Testovací data generátor

## ✅ Výborný nápad!

Proposal na testovací data generátor je velmi praktický a logický. Bez čekání na reálná data nemůžeme otestovat celý accuracy pipeline.

## 📋 Analýza požadavků

### Časové pokrytí
- ✅ **30 dní**: Dostatečné pro testování různých forecast horizonů (1-14 dní)
- ✅ **Hodinová + denní granularita**: Pokryje oba processing pipeline
- ✅ **Aktuální pozorování**: Nutné pro accuracy calculation

### Realistická data
- ✅ **70% přesnost**: Simuluje reálné API chování
- ✅ **Vzdálenost vs přesnost**: Kratší forecast = vyšší přesnost
- ✅ **Náhodnost**: Realistic variance pro grafy
- ✅ **Výkyvy počasí**: Edge cases pro testování

### Zdroje a nekompletnost
- ✅ **Múltiple sources**: weather-api, open-meteo (+ custom)
- ✅ **Částečně nekompletní data**: Testování INCOMPLETE stavů

## 💡 Implementační návrhy

### 1. Architektura
```kotlin
// Hlavní generátor
TestDataGenerator(
    dateRange: DateRange,
    locations: List<Location>,
    sources: List<DataSource>
)

// Konfigurace přesnosti
AccuracyConfig(
    baseAccuracy: 0.7,
    decayByDistance: 0.05,  // -5% za každý den
    randomVariance: 0.15
)
```

### 2. Data generation strategie
- **Temperature**: Base ± seasonal + daily pattern + noise
- **Precipitation**: Realistic probability clusters
- **Wind**: Correlated with weather patterns
- **Forecast accuracy**: Distance-based degradation

### 3. Gradle task struktura
```kotlin
tasks.register("generateTestData") {
    group = "data"
    description = "Generate test weather data for 30 days"

    doLast {
        // Reset DB
        // Generate data
        // Insert via existing persistence layer
    }
}
```

## 🎯 Výhody tohoto přístupu

### Development velocity
- **Okamžité testování**: Bez čekání na real data
- **Reproducible**: Stejná data pro všechny developery
- **Configurable**: Různé scénáře pro testing

### Quality assurance
- **Edge cases**: Kontrolované testy missing data
- **Performance**: Load testing s velkými datasets
- **Accuracy validation**: Známé expected results

## 🛠 Implementační detaily

### 1. Database reset strategie
**Doporučuji**: Vymazání dat místo reset kontejneru
```sql
DELETE FROM accuracy_daily;
DELETE FROM accuracy_hourly;
DELETE FROM actual_weather_observations;
DELETE FROM forecast_hourly;
DELETE FROM forecast_daily;
```
- Rychlejší než restart kontejneru
- Zachovává DB schema a connections

### 2. Processing state generování
- **forecast_daily**: Mix STORED a některé READY_FOR_PROCESSING
- **forecast_hourly**: Většinou READY_FOR_PROCESSING
- **actual_weather_observations**: Vždy READY_FOR_PROCESSING

### 3. KISS implementace
- Single `TestDataGenerator` class
- Simple linear accuracy degradation
- Basic weather patterns (žádné komplexní meteorology)
- Existing persistence layer reuse

## 🚀 Doporučené kroky

1. **Vytvořit test data modul** nebo package
2. **Implementovat základní generátor** s realistic patterns
3. **Přidat Gradle task** pro easy execution
4. **Testování s forecast-evaluator** na generated data
5. **Iterace** based on accuracy calculation results

## ⚠️ Potenciální úskalí

- **Realistic weather patterns**: Nechceme příliš komplexní meteorologii
- **Data volume**: 30 dní × 24 hodin × locations může být hodně dat
- **Generation time**: Balance mezi rychlostí a realismem

Celkově skvělý nápad pro praktický development! KISS přístup je správný - hlavně ať můžeme testovat accuracy pipeline. 🎯

-------------------------
## Reakce na feedback:

### ✅ Samostatný DB cleanup task
Výborný nápad! `clearDatabase` task bude užitečný i pro jiné účely:

```kotlin
tasks.register("clearDatabase") {
    group = "database"
    description = "Clear all weather data from database"
    // DELETE FROM všechny tabulky
}

tasks.register("generateTestData") {
    group = "data"
    description = "Generate test weather data for 30 days"
    dependsOn("clearDatabase")
    // Generování a vkládání dat
}
```

### 🏗 Umístění kódu - doporučuji vlastní modul

**Nový modul**: `test-data-generator`
- **Výhody**: Izolovaný, čisté dependencies, lze vypnout/zapnout
- **Struktura**:
  ```
  test-data-generator/
    src/main/kotlin/
      generator/TestDataGenerator.kt
      model/WeatherPattern.kt
      persistence/TestDataWriter.kt
    build.gradle.kts
  ```

**Alternativa**: Package v `common` modulu
- **Výhody**: Jednodušší setup, méně modulů
- **Nevýhody**: Mixing production a test kódu

**Doporučení**: Vlastní modul pro čistotu architektury a možnost optional builds. Co říkáš?