# hermes-back
TP1 réseaux INSA Lyon - application messenger

SDK Used :
Java 15

The project works with the gradle build tools which can be called with the gradlew script

We defined tasks to start the applications.

### Hermes Server (chat application) :

The repository already has a bundled and compiled jar, but if you wish you can generate a new using (generates a fat jar with all the dependencies) :
./gradlew shadowJar

The jar should be located in the build folder (gradle build folder) inside the libs subfolder.

To start the application using the jar use :
java -jar [JAR_NAME] [PORT]

For example :
java -jar hermes-back-1.0-all.jar 5000

NOTE : The fat jar is by default the one with the -all suffix

### Multicast client :
To start the multicast client application launch the following command :
./gradlew run
