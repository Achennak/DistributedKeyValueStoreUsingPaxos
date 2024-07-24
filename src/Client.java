import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.util.Random;
import java.util.Scanner;

/**
 * This class represents the client for a remote method invocation (RMI) based key-value store system.
 * The client interacts with a remote server that implements the RemoteInterface to perform various
 * operations on the key-value store.
 */
public class Client {

    /**
     * The main method to start the RMI client.
     * @param args Command-line arguments: [hostname] [port] [remoteObjectName]
     */
    public static void main(String[] args) {
       Helper helper = new Helper();
        try {
            // Check for correct number of command-line arguments
            if (args.length != 3) {
                System.out.println("Time : " + System.currentTimeMillis() + " - Usage: java PaxosClient c");
                System.exit(1);
            }

            // Extract command-line arguments
            String hostname = args[0];
            int port = Integer.parseInt(args[1]);
            String remoteObjectName = args[2];

            RMIClientSocketFactory rmiClientSocketFactory = new RMIClientSocketFactory() {
                /**
                 * Creates a socket with a timeout of 5 seconds for connection.
                 * @param host The remote host address
                 * @param port The remote host port
                 * @return The socket created for communication
                 * @throws IOException If an I/O error occurs during socket creation
                 */
                public Socket createSocket(String host, int port) throws IOException {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), 5000); // 5 sec timeout
                    return socket;
                }
            };

            rmiClientSocketFactory.createSocket(hostname, port);

            // Obtain a reference to the remote object from the RMI registry
            Random random = new Random();
            int addition = random.nextInt(4);
            Registry registry = LocateRegistry.getRegistry(hostname, port + addition, rmiClientSocketFactory);
            KVStore remoteObject = (KVStore) registry.lookup(remoteObjectName);

            // Perform pre-population of the key-value store
            for (int i = 0; i < 10; i++) {
                helper.logMessage("sending PUT message");
                handleOperation("PUT key" + i + " value" + i, remoteObject,helper);
            }

            // Perform GET operations on the key-value store
            for (int i = 0; i < 5; i++) {
                helper.logMessage("sending GET message");
                handleOperation("GET key" + i, remoteObject,helper);
            }

            // Perform DELETE operations on the key-value store
            for (int i = 0; i < 5; i++) {
                helper.logMessage("sending DELETE message");
                handleOperation("DELETE key" + i, remoteObject,helper);
            }

            // Interactive loop to handle user input for operations
            while (true) {
                try {
                    Scanner sc = new Scanner(System.in);
                    System.out.println("Enter operation: PUT <key> <value> or GET <key> or DELETE <key> or SHUTDOWN:");
                    String operation = sc.nextLine();
                    addition = random.nextInt(4);
                    registry = LocateRegistry.getRegistry(hostname, port + addition, rmiClientSocketFactory);
                    System.out.println("Server port - " + port);
                    remoteObject = (KVStore) registry.lookup(remoteObjectName);
                    if (operation.equalsIgnoreCase("SHUTDOWN")){
                        break;
                    }

                    else if (operation.toLowerCase().startsWith("put ") ||
                            operation.toLowerCase().startsWith("get ") ||
                            operation.toLowerCase().startsWith("delete ")) {
                        handleOperation(operation, remoteObject ,helper);
                    }
                } catch (RemoteException e) {
                    helper.logError( "RemoteException occurred while processing client request");
                } catch (ServerNotActiveException se) {
                   helper.logError("ServerNotActiveException occurred while processing client request");
                } catch (Exception e) {
                    helper.logError( "Exception occurred while processing client request with message" + e.getMessage());
                }
            }
        } catch (RemoteException e) {
          helper.logError( "RemoteException occurred while processing"+e.getMessage());
        } catch (Exception e) {
          helper.logError("Exception occurred while processing client with message" + e.getMessage());
        }
    }


    /**
     * Handles the specified operation on the key-value store by invoking the corresponding method on the remote object.
     *
     * @param operation    The operation to be performed on the key-value store (e.g., "PUT key value", "GET key", "DELETE key").
     * @param remoteObject The reference to the remote object implementing the RemoteInterface.
     * @throws ServerNotActiveException If the server is not active during the RMI call.
     * @throws RemoteException          If an RMI communication-related exception occurs.
     */
    private static void handleOperation(String operation, KVStore remoteObject,Helper helper)
            throws ServerNotActiveException, RemoteException, InterruptedException {
        helper.logMessage("Received operation - " + operation);
        ProcessRequest response = processRequest(operation, remoteObject);
        String responseData;
        if (!response.status) {
            helper.logError("Operation failed: ");
            responseData = response.message;
        } else {
            responseData = response.value;
        }
        helper.logMessage("Response from server - " + responseData);
    }

    /**
     * Processes the specified request by parsing the operation and invoking the corresponding method on the remote object.
     *
     * @param requestData  The request data containing the operation (e.g., "PUT key value", "GET key", "DELETE key").
     * @param remoteObject The reference to the remote object implementing the RemoteInterface.
     * @return A ProcessRequest object containing the response status and message.
     * @throws RemoteException          If an RMI communication-related exception occurs.
     * @throws ServerNotActiveException If the server is not active during the RMI call.
     */
    private static ProcessRequest processRequest(String requestData, KVStore remoteObject)
            throws RemoteException, InterruptedException {

        if (requestData.toLowerCase().startsWith("put")) {
            String[] parts = requestData.split(" ");
            if (parts.length == 3) {
                String key = parts[1];
                String value = parts[2];
                if (key.isEmpty() || value.isEmpty()) {
                    return new ProcessRequest(false, "PUT operation failed due to empty key or value", "");
                } else {
                    remoteObject.put(key, value);
                    return new ProcessRequest(true, "PUT process successful", "Key:" + key + " added with the Value:" + value);
                }
            } else {
                return new ProcessRequest(false, "PUT operation failed due to malformed input", "");
            }
        }

        if (requestData.toLowerCase().startsWith("get")) {
            String[] parts = requestData.split(" ");
            if (parts.length == 2) {
                String key = parts[1];
                if (remoteObject.containsKey(key)) {
                    String value = remoteObject.get(key);
                    return new ProcessRequest(true, "GET process successful", "Value returned for the given Key is : " + value);
                } else {
                    return new ProcessRequest(false, "Key not found in key store", "");
                }
            } else {
                return new ProcessRequest(false, "GET operation failed due to malformed input", "");
            }
        }

        if (requestData.toLowerCase().startsWith("delete")) {
            String[] parts = requestData.split(" ");
            if (parts.length == 2) {
                String key = parts[1];
                if (key.isEmpty()) {
                    return new ProcessRequest(false, "DELETE operation failed due to empty key", "");
                } else {
                    remoteObject.delete(key);
                    return new ProcessRequest(true, "DELETE process successful", "Value deleted for Key:" + key);
                }

            } else {
                return new ProcessRequest(false, "DELETE operation failed due to malformed input", "");
            }
        }
        return new ProcessRequest(false, "Operation failed due to malformed input", "");
    }

}

