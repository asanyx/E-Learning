# Testy reguł wyników quizów

W konsoli Firebase otwórz **Firestore Database → Rules**, wklej zawartość `firestore.rules` do edytora i otwórz **Rules Playground**. Dla każdego wiersza ustaw ścieżkę dokumentu, operację i uwierzytelnienie. Wartość `anna` lub `jan` oznacza włączone uwierzytelnienie z podanym UID, a „brak” oznacza wyłączone uwierzytelnienie.

Przy zapisie wprowadź pola z kolumny JSON. Liczby całkowite, w tym czas w milisekundach, ustaw jako **Integer**, a wartości logiczne jako **Boolean**. Myślnik oznacza operację bez danych wejściowych. Uruchom symulację i porównaj decyzję z ostatnią kolumną. Przypadki `create` i `update` sprawdzają odpowiednio pierwszy i ponowny zapis przez `set()`.

| Przypadek | Ścieżka | Operacja | UID | Dane JSON | Oczekiwany wynik |
|---|---|---|---|---|---|
| Pierwszy zapis | `/users/anna/quizResults/q1_1700000000000` | `create` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":1700000000000}` | **Allow** |
| Ponowienie tego samego zapisu | `/users/anna/quizResults/q1_1700000000000` | `update` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":1700000000000}` | **Allow** |
| Własny wynik | `/users/anna/quizResults/q1_1700000000000` | `get` | anna | — | **Allow** |
| Cudzy wynik | `/users/anna/quizResults/q1_1700000000000` | `get` | jan | — | **Deny** |
| Odczyt bez logowania | `/users/anna/quizResults/q1_1700000000000` | `get` | brak | — | **Deny** |
| Zapis bez logowania | `/users/anna/quizResults/q1_1700000000000` | `create` | brak | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":1700000000000}` | **Deny** |
| Nadpisanie cudzego wyniku | `/users/anna/quizResults/q1_1700000000000` | `update` | jan | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":1700000000000}` | **Deny** |
| Procent ponad 100 | `/users/anna/quizResults/q1_1700000000000` | `update` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":150,"passed":true,"completedAt":1700000000000}` | **Deny** |
| Dodatkowe pole | `/users/anna/quizResults/q1_1700000000000` | `create` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":1700000000000,"extra":true}` | **Deny** |
| Więcej poprawnych odpowiedzi niż pytań | `/users/anna/quizResults/q1_1700000000000` | `create` | anna | `{"quizId":"q1","correctCount":6,"total":5,"percent":60,"passed":true,"completedAt":1700000000000}` | **Deny** |
| Brak pola passed | `/users/anna/quizResults/q1_1700000000000` | `create` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"completedAt":1700000000000}` | **Deny** |
| Inny czas w ID dokumentu | `/users/anna/quizResults/q1_1700000000001` | `create` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":1700000000000}` | **Deny** |
| Czas zapisany jako tekst | `/users/anna/quizResults/q1_1700000000000` | `create` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":"1700000000000"}` | **Deny** |
| Czas równy zero | `/users/anna/quizResults/q1_0` | `create` | anna | `{"quizId":"q1","correctCount":3,"total":5,"percent":60,"passed":true,"completedAt":0}` | **Deny** |
| Usunięcie własnego wyniku | `/users/anna/quizResults/q1_1700000000000` | `delete` | anna | — | **Deny** |
| Odczyt z innej kolekcji | `/courses/x` | `get` | anna | — | **Deny** |
