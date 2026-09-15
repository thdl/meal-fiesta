# MealCase

En liten Android-app som utforsker [TheMealDB](https://www.themealdb.com/api.php) over tre
skjermer: **kjøkken → retter → detaljer**. Skrevet som case-oppgave til Giant Leap Technologies.

```
AreaScreen            MealListScreen              MealDetailScreen
(195 kjøkken)   →     (retter for ett kjøkken)  →  (bilde, ingredienser, fremgangsmåte)
```

## Kjøring

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Ingen API-nøkkel å sette opp — TheMealDB sin åpne testnøkkel (`1`) ligger i basis-URL-en.
Krever Android SDK 37 og JDK 17+ (Gradle henter riktig toolchain selv).

## Arkitektur

MVVM med et tynt domenelag. Én Gradle-modul.

```
Compose  →  ViewModel  →  Repository   →  Retrofit  →  TheMealDB
 (tekst)     (UiState)    (AppResult,
                          DTO→domene)
```

DTO-ene stopper på repository-grensen — ViewModel og UI kjenner bare domenemodellene.

Pakkene er organisert etter *feature*, ikke etter lag, slik at grensene ligner moduler man
senere kan trekke ut:

```
core/     model · network · data · common · ui
feature/  areas · meals · detail
app/      navigation · di
```

| Bibliotek | Til hva |
|---|---|
| Jetpack Compose + Material 3 | UI |
| Navigation Compose | Type-sikre ruter (`@Serializable`) |
| Retrofit + kotlinx.serialization | Nettverk og parsing |
| OkHttp | HTTP-klient, logging kun i debug |
| Coil 3 | Bildelasting |

## Sentrale avveininger

**MVVM uten UseCase-lag.** Et use-case-lag tjener til noe når operasjoner kombinerer flere
repositories, håndhever forretningsregler eller gjenbrukes. Her ville hver UseCase vært en
ren gjennomstikk-klasse rundt ett repository-kall, så de er utelatt med vilje.

**Manuell DI.** `AppContainer` bygger objektgrafen med `by lazy`, og ViewModels får
avhengighetene sine gjennom `viewModelFactory`. Rundt 30 linjer, ingen kodegenerering.
Sømmen mot Hilt er intakt — ViewModels tar imot et `MealRepository`, ikke en container — så
byttet er billig den dagen appen vokser.

**Egen `AppResult`, ikke Kotlins `Result`.** `Result.failure` tar kun `Throwable`, og å la
`AppError` arve `Throwable` bare for å passe inn ville bøyd domenemodellen etter en
bibliotek-detalj. `AppResult` bærer en typet `AppError` i stedet.

**Feil er typer, ikke tekst.** ViewModel-ene kjenner ingen strenger. `AppError` oversettes
til tekst ett sted — i Compose-laget, via `stringResource` — som er det som gjør appen
oversettbar uten å røre logikken.

**Cancellation svelges aldri.** `apiCall()` kaster `CancellationException` videre før den
fanger noe annet. `runCatching` ville fanget den, og en skjerm man forlater midt i en
henting ville da blinket en feilmelding på vei ut.

**Tre ulike betydninger av `null`.** Alle endepunktene svarer `{"meals": null}` når de ikke
har noe, men det betyr ikke det samme:

| Kall | `meals: null` betyr |
|---|---|
| `list.php?a=list` | Ødelagt svar — kjøkkenlista er aldri tom |
| `filter.php?a=X` | Ekte tomt resultat → `UiState.Empty` |
| `lookup.php?i=X` | Retten finnes ikke → `AppError.NotFound` |

Én felles nullable-mapping ville latt et ødelagt svar se ut som legitimt tomt innhold.

**Delt `UiState` er en forenkling.** Bare `MealListScreen` har en ekte `Empty`-tilstand; de
to andre behandler manglende data som feil. Én lesbar type slår tre nesten like for tre
skjermer, men delt UI er holdt bevisst smalt: `StatusContent` rendrer spinner, feil og tom
tilstand, og eier ikke skjermenes layout.

## Tester

20 enhetstester, alle på JVM (`./gradlew :app:testDebugUnitTest`).

- **`MealMappersTest`** — TheMealDB leverer ingredienser denormalisert som
  `strIngredient1..20` + `strMeasure1..20`, padet med tomme felter. Sammenslåingen til én
  liste er den eneste ekte logikken i appen, og testes mot et lagret, ekte API-svar.
  Assertene går på oppførsel (antall, paring, forkasting), ikke på en kopiert liste med
  ingrediensnavn, slik at testen overlever at oppskriften endres hos TheMealDB.
- **`ApiCallTest`** — at cancellation kastes videre, og at `IOException`, `HttpException` og
  `SerializationException` blir riktig `AppError`.
- **`DefaultMealRepositoryTest`** — låser fast de tre `null`-kontraktene over.
- **ViewModel-tester** — `Loading → Success`, `Loading → Error`, `Loading → Empty`, at retry
  setter `Loading` synkront, og at en kansellert henting aldri ender som feiltilstand.

## Kjente begrensninger

- **Omtrent 166 av de 195 kjøkkenene er tomme.** TheMealDB lister alle land, men har retter
  for bare ~29 av dem. Lista hentes dynamisk uansett, og tomme kjøkken får en egen tom
  tilstand framfor en blank skjerm. Å hardkode en liste over de 29 som virker ville skjult
  hvordan API-et faktisk oppfører seg.
- **Ingen caching eller offline-støtte.** Hver skjerm henter på nytt; ingenting lagres.
- **Ingen «stale-while-refreshing».** En ny henting bytter til spinner framfor å beholde
  forrige innhold. Det er også derfor `StatusContent` er holdt smal.
- **Ingen paginering eller søk.** Kjøkkenlista er lang å scrolle; et lokalt filtreringsfelt
  ville vært det første jeg la til.
- **Ingen UI-tester.** Logikken er dekket av enhetstester; navigasjon og rendering er
  verifisert manuelt.
- **Ingen flagg-emoji.** API-et returnerer `Italian`/`Italy`, ikke ISO-koder, så en mapping
  for ~195 navn ville kostet mer enn den smaker. Landsnavnet vises som undertekst i stedet.
