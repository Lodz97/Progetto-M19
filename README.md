# Progetto-M19 
Gestione parcheggio:
Realizziamo un sistema per la gestione dei posti in un parcheggio multilivello. Il sistema simulerà le colonnine che rilasciano i biglietti agli utenti, le postazioni che accettano il pagamento e gli sbarramenti che consentono l’uscita agli utenti che hanno pagato.
Altre caratteristiche sono: 
- conteggio dei posti disponibili; 
- gestione di tariffe configurabili; 
- interfacce grafiche e testuali; 
- interfaccia di supervisione;
- sistema distribuito.
- gestione database.

# Come avviare
1. Compilare Manager, Cassa, EntryColumn ed ExitColumn.

2. Avviare il manager inserendo come argomenti la porta desiderata per la connessione:
```
java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out java main.Parking.Manager port
```

3. Avviare, in terminali diversi e nell'ordine preferito, le tre periferiche colonnina d'entrata, d'uscita e la cassa, per ognuna sono richiesti due argomenti: l'indirizzo di host del manager e la porta in cui è stata aperta la connessione:
```
java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out main.Peripherals.Cash.Cash hostAddress port

java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out main.Peripherals.Columns.ExitColumn hostAddress port

java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out main.Peripherals.Columns.EntryColumn hostAddress port
```
Esempio:
Per avviare tutte le componenti dallo stesso sistema si può scrivere:
```
java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out main.Parking.Manager 1030

java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out main.Peripherals.Cash.Cash 127.0.0.1 1030

java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out main.Peripherals.Columns.ExitColumn 127.0.0.1 1030

java -cp C:\Users\NomeUtente\Documents\Progetto-M19-out main.Peripherals.Columns.EntryColumn 127.0.0.1 1030
```
Da windows, è possibile inserire i parametri desiderati nel file ```Parcheggio-M19.bat``` e avviarlo.

Al primo avvio viene creato un file di testo db nella cartella da cui è stato chiamato il codice compilato, agli avvii successivi sono caricati nel parcheggio gli abbonati e gli utenti ancora dentro. Per ignorare i dati registrati è sufficiente rimuovere dalla cartella il file db.
