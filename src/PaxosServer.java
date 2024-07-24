import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;

/**
 * PaxosServer class represents the main entry point for launching Paxos servers.
 */
public class PaxosServer {

    /**
     * Schedule periodic dropping of servers.
     * @param servers The array of servers.
     * @param helper The helper instance for logging messages.
     */
    private static void scheduler(ServerImpl[] servers,Helper helper) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dropServer(servers,helper);
            }
        }, 10000, 100000);
    }

    /**
     * Drop a server randomly or ignore when triggered.
     * @param servers The array of servers.
     * @param helper The helper instance for logging messages.
     */
    private static void dropServer(ServerImpl[] servers,Helper helper)  {
        int id = (int) (Math.random() * servers.length);
        servers[id].setServerDown();
        helper.logMessage( "Server " + id + " is going down at port  "+servers[id].getPort());
    }

    /**
     * The main method to launch the creation and binding process of the Paxos servers.
     *
     */
    public static void main(String[] args) {
        Helper helper = new Helper();

            int serversNum = 5;
            try {
                // Check for correct number of command-line arguments
                if (args.length != 2) {
                    System.out.println("Time : " + System.currentTimeMillis() + " - Usage: java PaxosServer c");
                    System.exit(1);
                }

                // Extract command-line arguments
                int portInput = Integer.parseInt(args[0]);
                String remoteObjectName = args[1];

                ServerImpl[] servers = new ServerImpl[serversNum];

                // Create and bind servers
                for (int serverId = 0; serverId < serversNum; serverId++) {
                    int port = portInput + serverId; // Increment port for each server
                    // Create RMI registry at the specified port
                    LocateRegistry.createRegistry(port);

                    // Create server instance
                    servers[serverId] = new ServerImpl(serverId, port);

                    // Bind the server to the RMI registry
                    Registry registry = LocateRegistry.getRegistry(port);
                    registry.rebind(remoteObjectName, servers[serverId]);

                   helper.logMessage("Server " + serverId + " is ready at port " + port);
                }
                scheduler(servers,helper);
                // Set acceptors and learners for each server
                for (int serverId = 0; serverId < serversNum; serverId++) {
                    Acceptor[] acceptors = new Acceptor[serversNum];
                    Learner[] learners = new Learner[serversNum];
                    for (int i = 0; i < serversNum; i++) {
                        acceptors[i] = servers[i];
                        learners[i] = servers[i];
                    }
                    servers[serverId].setAcceptors(acceptors);
                    servers[serverId].setLearners(learners);
                }

            } catch (Exception e) {
                helper.logError("Server exception: " + e.getMessage());
            }
    }
}
