
# COMP1206-Sushi-Business
A mock inventory and delivery management system for a Sushi business, with a Server application for running the business and a Client application for making orders. 
Full specification for the Coursework can be found in the repository or [here](https://secure.ecs.soton.ac.uk/notes/comp1206/cw2-2018.html).

## How to Setup
1. Install the [latest JVM](https://java.com/en/download/) your machine.
2. Navigate to the `src` folder
3. Compile source with `javac *.java` (A warning about unchecked operations will show up, this is fine)
### Local use
4. Run the Server Application with `java ServerApplication`
5. Run the Client Application with`java ClientApplication`
### Remote use
4. Run the Server Application with `java ServerApplication PORT` where PORT is the desired connection port.
5. Run the Client Application with `java ClientConnection PORT SERVERADDRESS` where PORT matches the server's port and SERVERADDRESS is the remote address/IP where the server is located.

## Launch Arguments

### Server
`java ServerApplication PORT CONFIG`
* The server's port can be specified. Note that the client and server's port must match.
* The server's starting Config file can be specified (if not specified the default `ConfigurationExample.txt` will be loaded). This could be one you wrote yourself following the `ConfigurationStructure.txt` rules or be a backup file.

### Client
`java ClientApplication PORT SERVERADDRESS`
* The server's port can be specified. Note that the client and server's port must match.
* The remote address where the server is hosted can be specified. By default localhost is used.
