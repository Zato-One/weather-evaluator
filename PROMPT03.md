Dobře, pojďme dál. Máme problém - aby naše aplikace k něčemu byla a dávala nějaké rozumné výstupy,
je závislá na tom, aby běžela 24/7 a aspoň několik dní (pokud chceme nějak vyhodnotit i daily data v různých časových odstupech),
ideálně aspoň 2 týdny.
Na to teď nemám připravenou ani infrastrukturu ani čas čekat na to až se data nasbírají, a na to že bych mohl
po nasbírání přijít na to, že chci něco změnit/upravit, a pak bych čekal několik dní znovu.
To nechme na samotné finále.

A proto potřebujeme nějaká testovací data, nějaké vygenerované hodnoty, které se aspoň trochu blíží realitě,
ale slouží pro testovací účely.
Tato data by měla mít následující:
 - měla by pokrývat několik dnů dat, dejme tomu třeba posledních 30 dní
 - měla by být jak daily, tak hourly, ať máme hezky plné tabulky
 - měla by být i s aktuálními daty, vůči kterým ty daily a hourly předpovědi porovnáme
 - ať se blížíme realitě, dejme tomu, že vůči těm aktuálním datům budou předpovědi v určité "skoro-přesnosti", dejme tomu třeba 70% a s čím menším intervalem tím přesnější předpovědi budou
 - určitý prvek náhody, ať ty závěrečné grafy nějak vypadají
 - budou pro několik zdrojů - teď taháme z weather-api a open-meteo, tak buď budou vygenerovaná jakože těmito zdroji, nebo vymyslíme pár vlastních
 - budou tam pokryty různé případy - výkyvy v počasí, nekompletní data některých dnů (jen pár, protože chceme hlavně ty kompletní ať je s čím pracovat)

Potřebujeme přípravu těchto dat nějaký proces, nějakou třídu/y nebo aspoň skript nebo něco.
A to něco by mělo být i spustitelné gradle taskem - udělá reset db kontejneru / nebo vymaže všechna data (vyber to vhodnější), a naplní db těmito vygenerovanými daty.
A pojďme to udělat KISS (keep it stupid simple), aby to splňovalo to co od toho očekávám, ale jen potřebným minimem kódu.

Než se do toho pustíme, co na to říkáš? Dává to smysl? Nějaké připomínky? 

-------------------------
Reakce na response:
 - dobře, tak pojďme cestou vymazání dat místo resetu kontejneru, tak jak jsi popsal - to už se mi párkrát hodilo, tak z toho pak zároveň udělej i jiný samostatný task, který se pak jen použije i zde
 - kam ten kód dáme - do jakého modulu?