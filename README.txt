Build del progetto firmapiu-daemon

git clone gitolite@dev.libersoft.it:firmapiu-lib
	Il progetto firmapiu-daemon dipende dal progetto firmapiu-lib:
	Il progetto della libreria deve essere importato per primo
	ATTENZIONE: Ha delle dipendenze nel filesystem locale. Installare
	le dipendenze richieste o cambiare lo script build.gradle se si
	vogliono utilizzare repositories e dipendenze remote

git clone gitolite@dev.libersoft.it:firmapiu-daemon
	Importa il progetto del demone
	ATTENZIONE: il progetto dipende da quello della libreria. Entrambi
	i progetti dovrebbero stare sullo stesso "livello" del filesystem
	per essere buildati correttamente. Se firmapiu-lib si trova in un
	path diverso, il path di firmapiu-lib va configurato nel file
	setting.gradle

gradle assemble
	Compila il progetto del demone e della libreria dalla quale
	firmapiu-daemon dipende

gradle build
	compila il progetto ed esegue i test
	ATTENZIONE: I test del progetto al momento dipendono da una
	particolare configurazione di ambiente utilizzata per testare
	il codice. Sarebbe meglio non eseguirli per non far fallire la
	build del progetto.


How to build .deb package
    In order to build the debian package you'll need:
    * git
    * debuild


    `sudo apt-get install git debuild`
    * the libfirmapiu-java repo cloned in the same folder of this repo as the daemon
      depends on that to build

    1. git clone https://github.com/libersoft/firmapiu-daemon.git
    2. cd firmapiu-daemon
    3. git checkout debian
    4. cd debian
    5. debuild -uc -us

Now you should have your .deb package located in the parent folder (..)

