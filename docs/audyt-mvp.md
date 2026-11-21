# Audyt kodu MVP platformy e-learningowej

Najważniejsze ryzyka przed wprowadzeniem trybu offline dotyczą trwałości danych. Brak migracji może skutkować odtworzeniem bazy od zera, zastąpienie kursu może usunąć jego materiały, a nieudany zapis wyniku do Room pozostawia na ekranie wynik, którego nie będzie w historii.

## Zakres i weryfikacja

Audyt objął kod Kotlin i Compose, nawigację, Room, Firebase Auth, synchronizację i reguły Firestore, manifest, zasoby oraz testy. Polecenie `./gradlew assembleDebug test lintDebug` uruchomiono na tymczasowej kopii projektu. Po odczytaniu raportów kopię usunięto. Podczas audytu repozytorium pozostało bez zmian.

| Sprawdzenie | Wynik |
|---|---|
| assembleDebug | APK zbudowane poprawnie |
| test | 60 testów w 13 klasach, 0 niepowodzeń, 0 błędów, 0 pominiętych |
| lintDebug | 0 błędów, 32 ostrzeżenia, 2 wskazówki |
| Testy instrumentowane | Nieuruchamiane |

Wśród ostrzeżeń lint 18 dotyczyło dostępności nowszych wersji, 8 nieużywanych zasobów, 2 odmiany liczebników, 2 polskiego słowa „adres”, 1 powtórzonej etykiety aktywności i 1 sugestii użycia `toUri()`. W `strings.xml:40` format może dać „1 z 1 materiałów”. Ostrzeżenia o pisowni „adres” to fałszywe alarmy. Dwie wskazówki dotyczą różnych wersji biblioteki testowej korutyn dla JVM i testów instrumentowanych. Sam raport lint nie uzasadnia ich zrównania.

## Ustalenia

P1 oznacza realny błąd lub ryzyko utraty danych albo naruszenia bezpieczeństwa. P2 oznacza istotną wadę jakości, P3 drobny problem. Nakład S/M/L oznacza odpowiednio małą, średnią lub dużą zmianę.

| Priorytet i obszar | Plik i miejsce | Problem i skutek | Proponowana poprawka | Nakład |
|---|---|---|---|---|
| P1 · Migracja Room | `AppDatabase.kt:30,50`, `getInstance()` | Przy przyszłej zmianie wersji bez migracji destrukcyjny fallback może usunąć ukończenia, wyniki i metadane pobrań. Synchronizacja z Firestore jest jednokierunkowa. Obecna wersja 1 sama nie powoduje utraty. | Zachować schemat v1 i przetestować jawną migrację na bazie z danymi użytkownika. | M |
| P1 · Aktualizacja katalogu | `CourseDao.insertAll()`, `QuizDao.insertQuizzes()`, `MaterialDao.insertAll()` | REPLACE usuwa poprzedni wiersz. W połączeniu z CASCADE zastąpienie kursu może usunąć materiały, quizy i pytania. Zastąpienie materiału domyślną encją resetuje pola pobrania. Obecny seed działa na pustym katalogu. | Aktualizować rekord bez usuwania rodzica, zachowywać pola pobrania i przetestować odświeżenie istniejącego kursu. | M |
| P1 · Trwałość wyniku | `QuizViewModel.kt:76–85`, `finish()` | Wynik jest widoczny przed zapisem. Błąd Room zostaje przemilczany, a kolejne finish() już nie ponawia zapisu. | Oddzielić stan zapisu od wyniku i umożliwić ponowienie tej samej próby ze stabilnym ID. | M |
| P1 · Właściciel próby | `AppNavHost.kt:129–149`, trasa Quiz | UID pochodzi z sesji w chwili finish(). Bezpośrednia zmiana A → B przy zachowanym wpisie quizu może przypisać odpowiedzi A użytkownikowi B. Zwykłe wylogowanie do null ma osobną obsługę. | Powiązać próbę z UID od początku i sprawdzić właściciela przy zapisie. Przetestować zmianę sesji. | M |
| P1 · Kolizja dokumentów | `FirestoreResultsDataSource.kt:8,20`, `resultDocumentId()` | Ten sam quiz i czas w milisekundach na dwóch urządzeniach dają ten sam dokument jednego użytkownika. Drugi set() nadpisze pierwszy wynik. | Nadać próbie trwały identyfikator niezależny od zegara i uzgodnić zmianę z regułami Firestore. | M |
| P1 · Błędy dysku | `MaterialViewModel.kt:33`, `CourseDetailViewModel.kt:44`, `toggleCompleted()` | Wyjątek odczytu lub zapisu SQLite w korutynie może zakończyć aplikację, np. przy braku miejsca. | Obsłużyć błąd i ponowienie, zachowując ostatni potwierdzony stan i propagację anulowania. | S |
| P2 · Częściowa rejestracja | `FirebaseAuthRepository.register()`, `AppNavHost.kt:63–65` | Po utworzeniu konta może zawieść zapis imienia. Formularz pokazuje błąd rejestracji, mimo że konto i sesja istnieją; kolejna próba może zgłosić zajęty e-mail. | Wydzielić stan błędu profilu i ponawiać sam zapis imienia. | M |
| P2 · Powrót | `AppNavHost.kt:83,214`, Register i Profile | Bezwarunkowe popBackStack() może przy powtórnej obsłudze zdjąć również ekran docelowy. Na tych ekranach objaw wymaga potwierdzenia na urządzeniu. | W rejestracji wracać do istniejącej trasy logowania, w profilu sprawdzać aktualny wpis i poprzednika. | S |
| P2 · Śmierć procesu | `QuizViewModel`, inicjalizacja stanu; `AppNavHost.kt:157–174` | Po obrocie odpowiedzi zostają, ale odtworzony proces rozpoczyna niedokończony quiz od pierwszego pytania. Ekran wyniku ma osobny powrót do kursu przy braku outcome. | Jeśli próba ma przetrwać śmierć procesu, zapisywać indeks i odpowiedzi z ID quizu i użytkownika. | M |
| P2 · Dane formularzy | `LoginScreen.kt:21`, `RegisterScreen.kt:22–23` | rememberSaveable obejmuje hasło i potwierdzenie. Mechanizm zapisu stanu przechowuje je także na potrzeby odtworzenia procesu. | Zachować hasło przez obrót w pamięci formularza lub ViewModelu i czyścić po wyjściu. | S |
| P2 · Kopie zapasowe | `AndroidManifest.xml:10`, `backup_rules.xml`, `data_extraction_rules.xml` | allowBackup jest włączone, a reguły mają tylko przykłady w komentarzach. Brak jawnych wyłączeń dla bazy z UID i historią. Przyszłe pobrania również mogą wejść do kopii zależnie od lokalizacji. | Określić politykę kopii i transferu urządzenie–urządzenie dla sesji, Room i pobranych plików. | S |
| P2 · Koszt synchronizacji | `QuizResultSyncer.syncAll()`, `FirestoreResultsDataSource.upload()` | Po zakończeniu synchronizacji znika zapamiętana lista. Kolejne osobne ukończenia wysyłają 1, 2, …, N rekordów mimo stałych ID. | Wysyłać nowy wynik po finish(), a pełne uzgodnienie zachować do odzyskiwania po przerwie lub śledzić oczekujące zapisy. | M |
| P2 · Koszt postępów | `ProgressViewModel`, combine; `RoomMaterialRepository.observeAllMaterials()`; `QuizResultDao.observeByUser()` | Ekran pobiera całe materiały wraz z tekstem i pełną historię. Grupuje dane i ponownie sortuje historię w zakresie ViewModelu. Brakuje indeksu wyników dla UID i czasu. | Pobierać lekką projekcję, wykorzystać kolejność DAO, przenieść cięższe mapowanie poza główny wątek i dodać indeks z migracją. | M |
| P2 · Subskrypcje w tle | `CourseListViewModel.init`, `CourseDetailViewModel.uiState`, `MaterialViewModel.uiState` | Eagerly lub stale działający launch utrzymują obserwatory Room również dla ekranów przykrytych innym wpisem stosu. | Powiązać obserwację z aktywnymi subskrybentami, podobnie jak w ProgressViewModel. | S |
| P2 · Gotowość offline | `MaterialDao.markDownloaded()/clearDownload()`, `MaterialScreen.openMaterial()`, `MaterialDaoTest.filterDownloadClearAndCascade()` | Test sprawdza flagę i przykładową ścieżkę w bazie. Brakuje kontraktu dla przerwanego pobierania, braku pliku i błędu dysku. Ekran używa adresu zdalnego. | Przy wdrażaniu offline publikować najpierw gotowy plik, potem oznaczać go w Room. Przetestować przerwanie, ponowienie i brak miejsca. | M |
| P2 · Transport | `MaterialScreen.kt:58–64`, `openMaterial()` | Wideo może być otwarte przez HTTP. Adres obsługuje zewnętrzna aplikacja, więc polityka transportu własnej aplikacji nie gwarantuje ochrony. | Wymagać HTTPS także dla wideo i stosować tę samą walidację podczas pobierania. | S |
| P2 · Tryb ciemny | `themes.xml`, motyw aplikacji; `MainActivity.onCreate()` | Motyw okna wymusza Light, a korzeń Compose nie rysuje pełnoekranowego tła MaterialTheme. Przez przezroczyste obszary może być widoczne jasne okno. Kontrast wymaga sprawdzenia ręcznego. | Dopasować motyw okna do systemu i narysować powierzchnię tła u podstawy Compose. | S |
| P3 · Testy | `ExampleUnitTest.kt:14`, `QuizViewModelTest.kt:69,81` | Test szablonowy sprawdza dodawanie 2 + 2. Test czasu zależy od zegara systemowego. Brak bezpośrednich testów częściowej rejestracji i granicy 400/401 dokumentów uploadu. | Zastąpić test szablonowy regresją produktu, ustabilizować czas testowy i sprawdzić te granice. | S/M |

## Pięć priorytetów przed trybem offline

1. Przetestować migrację bazy v1 z istniejącymi wynikami i ukończeniami.
2. Zapewnić aktualizację katalogu bez kaskadowego usuwania dzieci i resetowania metadanych pobrań.
3. Oddzielić wynik obliczony od zapisanego i umożliwić bezpieczne ponowienie zapisu.
4. Ustalić kolejność zapisu plik–Room oraz odzyskiwanie po błędzie dysku lub przerwaniu.
5. Powiązać rozpoczętą próbę quizu z właścicielem i przetestować zmianę sesji przy kończeniu quizu.

## Mocne strony i gotowość architektury

- Pakiet domenowy jest wolny od importów Androida, AndroidX i Firebase. Ocenianie quizów i postęp mają testy przypadków brzegowych.
- Seed wykonuje sprawdzenie i zapis w transakcji. Kolumny kluczy obcych materiałów, quizów i pytań mają indeksy. Postępy korzystają ze zbiorczych odczytów.
- Quiz i wynik współdzielą ViewModel wpisu quizu. Atomowe ustawienie wyniku chroni przed podwójnym finish(). W sprawdzonych ViewModelach nie znaleziono referencji do Activity ani Context.
- Upload odpowiada regułom Firestore pod względem ścieżki, sześciu pól, typów i ID. Paczka ma najwyżej 400 dokumentów. Jawny log aplikacji nie zawiera UID, e-maila ani wyniku. Reguły sprawdzają właściciela i format; ocena nadal pochodzi od klienta.
- MainActivity jest eksportowana jako launcher. W sprawdzonych śledzonych plikach nie znaleziono klucza prywatnego ani magazynu kluczy. Plik app/google-services.json jest ignorowany i nie występuje w sprawdzonej historii tej ścieżki.
- Ikony akcji mają opisy, a ikonom statusu bez opisu towarzyszy tekst. UI zbiera stan przez collectAsStateWithLifecycle.
- AppContainer, repozytoria, WorkManager i pola localPath/isDownloaded dają punkty integracji dla offline. Dla FCM dostępna jest zależność Messaging. Pozostają obsługa tokenu i komunikatów, kanał oraz zgoda użytkownika na obsługiwanych wersjach Androida.

## Do weryfikacji ręcznej

Audyt nie obejmował uruchamiania aplikacji. Do sprawdzenia na urządzeniu pozostają gesty i podwójne Wstecz, odtworzenie stosu po zabiciu procesu, zmiana konta w trakcie quizu, kontrast i TalkBack. Osobnej próby wymagają rzeczywiście wdrożone reguły Firebase, wysyłka po odzyskaniu sieci, odtworzenie kopii systemowej oraz obsługa ACTION_VIEW przez dostępne aplikacje.
