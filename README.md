# MealCase

A small Android app for exploring [TheMealDB](https://www.themealdb.com/api.php) across three
screens: **cuisines → meals → details**.

```
AreaScreen                  MealListScreen              MealDetailScreen
(cuisines with meals)   →   (meals for a cuisine)   →   (image, ingredients, instructions)
```

## Run

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest # requires an emulator or connected device
```

No API key setup is needed: TheMealDB's public test key (`1`) is included in the base URL.
The project uses Android SDK 36 and requires a JDK compatible with the Android Gradle plugin;
source and target compatibility are set to Java 17.

## Architecture

MVVM with a thin domain layer in one Gradle module.

```
Compose  →  ViewModel  →  Repository   →  Retrofit  →  TheMealDB
 (UI)       (UiState)     (AppResult,
                          DTO→domain)
```

DTOs stop at the repository boundary. ViewModels and UI use domain models only.

Packages are grouped by feature and shared responsibilities, keeping the boundaries clear if
the project is split into modules later:

```
core/     model · network · data · common · ui
feature/  areas · meals · detail
app/      navigation · di
```

| Library | Purpose |
|---|---|
| Jetpack Compose + Material 3 | UI |
| Navigation Compose | Type-safe routes (`@Serializable`) |
| Retrofit + kotlinx.serialization | Networking and parsing |
| OkHttp | HTTP client; logging in debug builds only |
| Coil 2 | Image loading |

## Design decisions

**MVVM without a UseCase layer.** Use cases help when operations combine repositories,
enforce business rules, or are reused. Here, they would mostly forward one repository call,
so they are intentionally omitted.

**Manual dependency injection.** `AppContainer` builds the object graph, and ViewModels
receive dependencies through factories. ViewModels depend on `MealRepository`, not on the
container, so replacing the wiring later would not require changing their logic.

**A typed `AppResult` instead of Kotlin's `Result`.** `Result.failure` accepts only a
`Throwable`. Making `AppError` a throwable just to fit that API would distort the domain
model; `AppResult` carries a typed error instead.

**Errors are types, not strings.** ViewModels do not know about localized text. Compose maps
`AppError` to a string resource, keeping presentation text out of the logic.

**Cancellation is never swallowed.** `apiCall()` rethrows `CancellationException` before
handling other failures. A request cancelled when leaving a screen must not turn into an
error state.

**Three different meanings of `null`.** The API may return `{"meals": null}`, but the
meaning depends on the endpoint:

| Request | Meaning of `meals: null` |
|---|---|
| `list.php?a=list` | Invalid response: the cuisine list is required |
| `filter.php?a=X` | Legitimately empty result → `UiState.Empty` |
| `lookup.php?i=X` | Meal not found → `AppError.NotFound` |

The `meals` field must be present in the JSON response. A missing field is invalid, not an
empty list.

**Show only cuisines with meals.** TheMealDB has no single endpoint for this. The repository
loads the cuisine list, removes exact duplicates, then checks `search.php?f=` for letters
`a`–`z` to identify countries with meals, with at most four requests in flight. Querying
every cuisine separately hit the API's rate limit. If one or more letter requests fail,
confirmed cuisines remain visible with an incomplete-results warning and a **Try again**
button. If no cuisines can be confirmed, the screen shows a retryable error instead of a
non-retryable empty state. Only a complete, nonempty discovery result is cached in memory
for the app process lifetime; partial and empty results are never cached.

**Cuisine labels and API queries can differ.** The list may display `Afghan`, while meals
are indexed by `Afghanistan`. Navigation carries both the display name and country: the
title remains `Afghan`, while `filter.php` receives `Afghanistan`. If the API provides no
country, the cuisine name is used.

**Shared `UiState` is a simplification.** A meal list can be genuinely empty; an unusable
cuisine discovery is a retryable error; and a missing detail is `NotFound`. A shared state
type is simpler than three nearly identical ones, while the shared status UI is limited to
loading, errors, and empty content rather than owning each screen's layout.

## Tests

30 JVM unit tests (`./gradlew :app:testDebugUnitTest`) and three instrumented UI tests
(`./gradlew :app:connectedDebugAndroidTest`).

- **`MealMappersTest`** covers the API's denormalized `strIngredient1..20` and
  `strMeasure1..20` fields using a saved real response. Assertions focus on pairing and
  discarding empty values, not on recipe content that the API may change.
- **`ApiCallTest`** checks that cancellation propagates and that `IOException`,
  `HttpException`, and `SerializationException` map to the appropriate `AppError`.
- **`DefaultMealRepositoryTest`** covers the three `null` contracts, filtering,
  deduplication, caching of complete results, and retry after partial or empty discovery.
- **ViewModel tests** cover loading, success, error, and empty states, synchronous retry
  loading, and cancellation behavior.
- **`AppNavigationTest`** rapidly taps Back from the meal list and details, and verifies
  the incomplete-results warning and retry flow on the cuisine screen.

## Known limitations

- **Discovery requires multiple API calls on a fresh launch.** Hiding empty cuisines takes
  26 first-letter lookups. Meals whose names do not start with `a`–`z` may be missed by
  this availability check.
- **No offline support or general-purpose cache.** Only a complete, nonempty cuisine
  discovery is cached in memory. Partial results, meal lists, and details are fetched
  again; after process death, cuisine discovery runs again.
- **No stale-while-refreshing.** A new request shows a loading indicator rather than
  keeping the previous content visible.
- **No pagination or search.** The cuisine list can be long to scroll; local filtering
  would be a useful next addition.
- **No flag emoji.** The API returns names such as `Italian` and `Italy`, not ISO codes;
  the list therefore displays cuisine names without flags.
