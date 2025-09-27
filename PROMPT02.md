Dobře, tak pojďme zatím službu jen založit.
A začneme validátorem. Asi bych nepřidával sloupeček "processed", ale "processing_state".
Tam by mohly být stavy: STORED, READY FOR PROCESSING, ACCURACY PROCESSED, INCOMPLETE (pro nekompletní dny).
Dává to smysl, nebo bys ty stavy pojmenoval jinak?
Jak by ten validátor fungoval? Asi by si prošel data, co jsou ve stavu STORED, a vyhodnotil,
jestli jsou READY FOR PROCESSING. U hodinových dat dává smysl to asi rovnou označovat jako READY,
protože u těch nám stačí 1 ku 1 záznamu (předpověď na konkrétní hodinu vs aktuální data na předpovězenou hodinu),
ale u denních musíme hodnotit celý den (schválně bych dal požadavek mít aspoň 22 z 24 hodin, ale případně konfigurovatelný).
Začli bychom validátor procesem, a dali si placeholder, že po něm bude běžet vyhodnocovaný proces (ten zatím řešit nebudeme).
Potřebné nové sloupce bychom mohli přidat do těch původních zdrojových tabulek - máme tam liquibase, takže přidat nový changelog
pro nový sloupec/sloupce nebude problém.

Dává to smysl?