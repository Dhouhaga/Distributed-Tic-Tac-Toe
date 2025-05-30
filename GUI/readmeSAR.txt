
//GUI
# Compilation
javac -d out common/*.java client/*.java server/*.java


#Server
dhouha2@dhouha2:~/Documents/ttt/src$ java -Djava.security.manager -Djava.security.policy=server.policy -Djava.rmi.server.codebase=http://192.168.1.23/classes/ -Djava.rmi.server.hostname=192.168.1.23 -Djava.rmi.useLocalHostname=true -cp /var/www/classes server.ServerMain 192.168.1.23

Server ready at 127.0.0.1


#client
-- Linux
dhouha2@dhouha2:~/Documents/ttt$ java     -Djava.security.manager     -Djava.security.policy==src/client.policy     -Djava.rmi.server.codebase=http://127.0.0.1/classes/     -Djava.rmi.server.useCodebaseOnly=false     -Dsun.rmi.transport.logLevel=VERBOSE     -Dsun.rmi.loader.logLevel=VERBOSE     -Djava.rmi.server.logCalls=true     -cp out/     client.ClientMain 127.0.0.1 127.0.0.1

--Withou debugging mode: java     -Djava.security.manager     -Djava.security.policy==client.policy     -Djava.rmi.server.codebase=http://127.0.0.1/classes/     -Djava.rmi.server.useCodebaseOnly=false   -cp out/     client.ClientMain

-- Windows
PS C:\Users\USER\Downloads\src> java "-Djava.security.manager" "-Djava.security.policy==client.policy" "-Djava.rmi.server.codebase=http://192.168.1.30/classes/" "-Djava.rmi.server.useCodebaseOnly=false" "-Dsun.rmi.transport.logLevel=VERBOSE" "-Dsun.rmi.loader.logLevel=VERBOSE" "-Djava.rmi.server.logCalls=true" -cp "out" "client.ClientMain" 192.168.1.23 192.168.1.19
2-- java "-Djava.security.manager" "-Djava.security.policy==client.policy" "-Djava.rmi.server.codebase=http://192.168.1.23/classes/" "-Djava.rmi.server.useCodebaseOnly=false" -cp "out" "client.ClientMain" 192.168.1.23 192.168.1.19