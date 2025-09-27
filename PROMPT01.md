Pojďme na 5. službu: forecast-evaluator.
Aktuálně máme 3 tabulky: FORECAST_DAILY, FORECAST_HOURLY a ACTUAL_WEATHER_OBSERVATIONS.
Cílem této služby bude porovnat data z forecast tabulek a aktálních předpovědí, zapsat přesnosti do nové tabulky/tabulek.
Tato služba bude pomocí cronu běžet pravidelně, třeba každých 15 minut (bude to konfigurovatelné).
Asi bychom měli rozdělit přesnosti na hodinové a denní (tedy podle granularit).
Co takhle mít tabulky ACCURACY_DAILY a ACCURACY_HOURLY?
Pokaždé když služba bude dělat svůj job (těch každých 15 minut), tak projde dosud nezpracovaná data,
ty zpracuje, zapíše do accuracy tabulek a označí ta data za zpracovaná.
To znamená, že bychom do těch dosavadních tabulek přidali sloupec/sloupce o tom jestli byla data zpracovaná.

Ještě se nabízí co vlastně budeme vyhodnocovat?
ACTUAL_WEATHER_OBSERVATIONS - je potřeba, aby se tabulka pravidelně plnila aktuálními předpověďmi,
abychom byly schopní vyhodnotit jestli denní předpověď byla přesná (nesmí nám chybět část dne, protože potřebujeme řešit agregované funkce jako min, max, mean)
FORECAST_DAILY:
  - Bude nás zajímat přesnost předpovědi, který udělala 1 den dopředu, 2 dny dopředu, ..., až po 14 dní dopředu. Z toho pak můžeme jednak usoudit přesnosti, ale i jak moc je to přesné na základě délky předpovědi.
FORECAST_HOURLY:
  - Bude nás zajímat čas předpovědi (hodina) vůči cílovému časi předpovědi.
  - Můžeme porovnávat, jak byla předpověď přesna s délkou 1h, 2h, 3h, ..., 24h, 2 dny, 3 dny, ..., 14 dní (protože open-meteo má k dispozici i 14 dní hodinové předpovědi)

Protože máme pro každou granularitu i různé délky předpovědí - budeme si to do nových tabulek nějak poznamenat.
Asi vznikne nějaký enum (obodobně jako u granularit), který bude značit délku měřené a vyhodnocované předpovědi.

Ještě je otázka, jestli nemít nějakou službu validátora, který označí předpovědi celého dne jako READY,
jen v případě, že je kompletní (necelé dny by se nevyhodnocovaly).

Dává to všechno smysl nebo máš nějaké připomínky nebo nápady?