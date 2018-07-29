# FTP-Client

## Compiling Instructions:
1. Download the directory **/src**.
2. Compile it with javac in terminal with JavaSE-1.7, **OR**
3. Create a project with **/src** in Eclipse and run it.


## Running Instructions:
1. Type “myftp inet.cis.fiu.edu” to start a connection.
2. Type “demo” as username.
3. Type “demopass” as password.
4. Type “ls” to get a file listing of current directory.
5. Type “put filename” to upload a file to the server.
6. Type “get filename” to download a file from the server.
7. Type “delete filename” to delete a file on the server.
8. Type “quit” to quit the ftp client.

## Note:
1. Long-waiting for commands may lead to disconnection.
2. The file to upload should be in the current directory. If the file is “lingbo.txt”:
	1) In Eclipse, put it here in the project: /FTPClient/lingbo.txt.
	2) In terminal, put it in the current directory.

## TODO:
1. Code needed to be refactored.
2. The “Enter” will be lost during the file transferring.
