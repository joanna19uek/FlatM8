# FlatM8
_FlatM8_ to aplikacja przeznaczona dla osób wspólnie wynajmujących mieszkanie, pomagająca w zarządzaniu wspólnymi wydatkami.

Aplikacja posiada następujące funkcjonalności:
- rejestracja grup współlokatorów
- dodawanie lokatorów do grup
- możliwość określenia miesięcznego budżetu
- rejestrowanie zakupów dokonanych przez poszczególnych współlokatorów (kto, kiedy, co, za ile)
- porównanie wydatków z bieżącego miesiąca z założonym budżetem
- rejestrowanie przypomnień o zbliżającym się terminie np. zapłaty czynszu

Aplikacja przechowywuje wszystkie wprowadzane dane w bazie MySQL na serwerze v-ie.uek.krakow.pl

## Przewodnik po aplikacji

### Rejestracja grupy współlokatorów
Po uruchomieniu aplikacji dostępne są dwie opcje: Wybierz grupę oraz Nowa grupa.
Pierwsza z nich umożliwia zalogowanie się do już utworzonej grupy współlokatorów, a druga - stworzenie swojej grupy. Aby zarejestrować grupę należy podać jej unikalną nazwę oraz podać hasło dostępu do niej.

### Główna aktywność
Po poprawnym zalogowaniu się do grupy mamy możliwość szybkiego podglądu stopnia wykorzystania budżetu w aktualnym miesiącu oraz ostatnich zarejestrowanych wydatków. Znajdujący się obok nich plus, umożliwia zarejestrowanie nowego wydatku - należy podać kto (wybrać odpowiedną osobę z listy), kiedy, za ile dokonał jakiego zakupu.
W lewym górnym rogu dostępne jest również rozwijane menu, z którego można się dostać do dowolnej aktywności.

#### Lokatorzy
Pozycja Grupa umożliwia podgląd przypisanych do grupy lokatorów oraz dodawanie nowych.
#### Miesięczne wydatki
Pozycja Wydatki umożliwia podgląd wszystkich zerejestrowanych wydatków z aktualnego miesiąca.
#### Przypomnienia
Pozycja Przypomnienia umożliwia podgląd wszystkich utworzonych przypomnień oraz dodawanie nowych - należy podać treść przypomnienia (np. zapłata czynszu), termin zapłaty oraz kwotę. Dodatkowo po każdym zalogowaniu się do aplikacji pojawia się informacja czy w ciągu najbliższych 7 dni nie ma jakiegoś terminu zapłaty.
#### Budżet
Pozycja Budżet umożliwia podgląd aktualnie ustawionego miesięcznego budżetu (jego ustawienie lub zmianę) oraz porównania wydatków z aktualnego miesiąca z budżetem w formie zaoszczędzonej kwoty. Jeżeli kwota ta jest ujemna, to znaczy, że budżet został przekroczony.
#### Mail do autorów aplikacji
Pozycja Zgłoś błędy umożliwia wprowadzenie treści maila dotyczącego zgłaszanego problemu. Mail ten zostaje automatycznie wysłany do autorów aplikacji.


## Dane autorów
Joanna Pełeńska, nr albumu 187772
Paweł Pietralik, nr albumu 187805
studia dzienne, grupa KrDZIs3013Io
