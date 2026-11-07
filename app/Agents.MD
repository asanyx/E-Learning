# AGENTS.md: E-learning Android app

## Projekt
Aplikacja e-learningowa (kursy, materiały, quizy, postępy, offline, powiadomienia).
Projekt na studia: kod ma być czytelny i zrozumiały dla studenta, bez nadmiernej abstrakcji.

## Stack (nie zmieniaj bez pytania)
- Kotlin, Jetpack Compose, Navigation Compose, Material 3
- MVVM + Repository, StateFlow, korutyny
- Room, WorkManager, Firebase (Auth, Firestore, Storage, FCM)
- minSdk 26, brak XML layoutów

## Zasady pracy
1. Wykonuj WYŁĄCZNIE zadanie z promptu. Nie rób zadań "na zapas".
2. Nie dodawaj, nie usuwaj i nie aktualizuj zależności bez zgody. Wersje tylko w `gradle/libs.versions.toml`. Jeśli potrzebna nowa biblioteka, zatrzymaj się i zapytaj.
3. Nie modyfikuj `google-services.json`, `local.properties` ani plików konfiguracyjnych Firebase.
4. Nie zmieniaj plików niezwiązanych z zadaniem. Zmiany w istniejącym kodzie maksymalnie małe.
5. Nie commituj. Zostaw zmiany do review.
6. Logika (ocenianie, walidacja, postępy) w czystych klasach/funkcjach bez zależności od Androida, żeby dało się ją testować jednostkowo.
7. Komentarze po polsku, krótkie, tylko tam, gdzie logika nie jest oczywista.
8. Brak haseł/kluczy w kodzie.

## Struktura pakietów
com.example.elearning/
- data/ (local: Room, remote: Firebase, repository)
- domain/ (modele, logika: QuizScorer, ProgressCalculator)
- ui/ (screens, viewmodels, navigation, theme)
- worker/ (WorkManager)

## Definition of Done (każde zadanie)
- `./gradlew assembleDebug` przechodzi bez błędów
- `./gradlew test` przechodzi (jeśli zadanie dotyczy logiki, dodaj testy)
- Brak nowych warningów związanych z Twoją zmianą
- Na końcu odpowiedzi podaj: (a) listę zmienionych/dodanych plików, (b) jak to ręcznie sprawdzić w aplikacji, (c) otwarte kwestie lub założenia

## Czego NIE robić
- Nie przepisuj całej architektury ani nie "ulepszaj" cudzego kodu
- Nie używaj eksperymentalnych/alpha API
- Nie generuj plików binarnych ani zasobów graficznych