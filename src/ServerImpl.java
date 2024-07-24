import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServerImpl class implements various roles in the Paxos algorithm and serves as a key-value store server.
 */
public class ServerImpl extends UnicastRemoteObject implements Proposer, Acceptor, Learner, KVStore {

    boolean success = false;
    double divisionFactor = 2.0;
    int serverDelayTime = 100;
    private final Map<String, Pair<String, Operation>> historyEntries;
    private Acceptor[] consensusAcceptors;
    private Learner[] knowledgeLearners;
    private final int uniqueServerId;
    private final Map<String, Pair<Integer, Boolean>> learningRecord;
    private long downtimeTracker = 0;
    private final ConcurrentHashMap<String, String> keyValueStore = new ConcurrentHashMap<>();
    private boolean serverStatus = false;
    private final int port;
    private final Helper helper;


    /**
     * Constructor to create a Server instance.
     *
     * @param serverId The unique ID of this server.
     * @param port The port number this server listens on.
     * @throws RemoteException if RMI error occurs.
     */
    public ServerImpl(int serverId, int port) throws RemoteException {
        this.uniqueServerId = serverId;
        this.port = port;
        this.historyEntries = new HashMap<>();
        this.learningRecord = new HashMap<>();
        this.helper = new Helper();
    }

    /**
     * Get the port number this server is running on.
     * @return The port number.
     */
    public int getPort(){
        return this.port;
    }

    /**
     * Set the acceptors for this server.
     * @param acceptors Array of acceptors.
     */
    public void setAcceptors(Acceptor[] acceptors) throws RemoteException {
        this.consensusAcceptors = acceptors;
    }


    /**
     * Set the learners for this server.
     * @param learners Array of learners.
     */
    public void setLearners(Learner[] learners) throws RemoteException {
        this.knowledgeLearners = learners;
    }


    @Override
    public synchronized String put(String key, String value)
            throws RemoteException, InterruptedException {
        success = false;
        proposeOperation(new Operation("PUT", key, value));
        if (success)
            return "PUT operation successful for key - "+ key +" with value - "+value;
        else
            return "Error occurred during PUT operation for key - "+key;
    }


    @Override
    public synchronized String delete(String key) throws RemoteException, InterruptedException {
        success = false;
        proposeOperation(new Operation("DELETE", key, null));
        if (success)
            return "DELETE operation successful for key - "+ key;
        else
            return "Error occurred during DELETE operation for key - "+key;
    }


    @Override
    public synchronized  String get(String key) throws RemoteException {
        if (keyValueStore.containsKey(key)) {
            helper.logMessage("GET Operation successful for Key :"+key);
            return keyValueStore.get(key);
        }
        return "No entry exist for they key - "+key;
    }


    @Override
    public Boolean containsKey(String key) throws RemoteException, InterruptedException {
        return keyValueStore.containsKey(key);
    }

    /**
     * Propose an operation to be applied.
     * @param operation The operation to be proposed.
     * @throws RemoteException If a remote error occurs.
     */
    private void proposeOperation(Operation operation) throws RemoteException, InterruptedException {
        String proposalId = generateProposalId();
        propose(proposalId, operation);
    }

    /**
     * Check if acceptor is down. Return a boolean value depending on the acceptor status.
     *
     * @return true if the acceptor is down
     */
    private boolean checkAcceptorStatus() throws RemoteException {
        if(serverStatus) {
            long currentTime = System.currentTimeMillis() / 1000L;

            if(this.downtimeTracker + serverDelayTime <= currentTime) {
                serverStatus = false;
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Process the prepare operation of a acceptor. Receive the prepare request from the acceptor
     * and accept / reject it based on the if there's any latest operation in it's log.
     * @param proposalId The unique ID of the proposal.
     * @param oper operation to be performed
     * @return pair if  the given operation is accepted or not along with accepted operation
     * @throws RemoteException if there's any issue with RMI
     */
    @Override
    public synchronized Boolean prepare(String proposalId, Operation oper) throws RemoteException {
        if(checkAcceptorStatus()) {
            return null;
        }
        // check in the log for any highest value.
        if(this.historyEntries.containsKey(oper.key)) {
            if(Long.parseLong(this.historyEntries.get(oper.key).getKey().split(":")[1]) > Long.parseLong(proposalId.split(":")[1])) {
                return false;
            }
        }
        this.historyEntries.put(oper.key, new Pair<>(proposalId, oper));
        return true;
    }

    /**
     * Accept the value that the proposers give. If there's any operation with higher number, reject
     * the acceptance.
     * @param proposalId The unique ID of the proposal.
     * @param proposalValue The value of the proposal.
     * @throws RemoteException if issue arises with RMI
     */
    @Override
    public synchronized void accept(String proposalId, Operation proposalValue) throws RemoteException {
        if(checkAcceptorStatus()) {
            return;
        }
        // check in the log for any highest value.
        if(this.historyEntries.containsKey(proposalValue.key)) {
            if(Long.parseLong(this.historyEntries.get(proposalValue.key).getKey().split(":")[1]) <= Long.parseLong(proposalId.split(":")[1])) {
                for(Learner learner : this.knowledgeLearners) {
                    learner.learn(proposalId, proposalValue);
                }
            }
        }
    }


    /**
     * Porpose a value to all the acceptors and get their input on the proposal. If all the
     * acceptors accept the proposal, send a accept request. If any of them rejects it, send a
     * accept request for the higher operation value.
     * @param proposalId The unique identifier for the proposal.
     * @param proposalValue The value being proposed.
     * @throws RemoteException if issue arises with RMI
     * @throws InterruptedException if sleep is interrupted
     */
    @Override
    public synchronized void propose(String proposalId, Operation proposalValue)
            throws RemoteException {
        // Implement Paxos propose logic here
        List<Boolean> prepareResponse = new ArrayList<>();
        for(Acceptor acceptor : this.consensusAcceptors) {
            Boolean res = acceptor.prepare(proposalId, proposalValue);
            prepareResponse.add(res);
        }
        int majorityCount = 0;

        // check for rejections and majority
        for(int i=0; i<5; i++) {
            if(prepareResponse.get(i) != null) {
                if(prepareResponse.get(i))
                    majorityCount += 1;
            }
        }
        // if majority, accept the proposed value
        if(majorityCount >= Math.ceil(consensusAcceptors.length/divisionFactor)) {
            for(int i=0; i<5; i++) {
                if(prepareResponse.get(i) != null)
                    this.consensusAcceptors[i].accept(proposalId, proposalValue);
            }
        }
    }

    /**
     * learn the value that the acceptors pass.
     * @param proposalId The unique identifier for the proposal.
     * @param acceptedValue The value that has been accepted.
     * @throws RemoteException if any issue with the RMI
     */
    @Override
    public synchronized void learn(String proposalId, Operation acceptedValue) throws RemoteException {
        // Implement Paxos learn logic here
        if(!this.learningRecord.containsKey(proposalId)) {
            this.learningRecord.put(proposalId, new Pair<>(1, false));
        } else {
            Pair<Integer, Boolean> learnerPair = this.learningRecord.get(proposalId);
            learnerPair.setKey(learnerPair.getKey()+1);
            if(learnerPair.getKey() >= Math.ceil(consensusAcceptors.length/divisionFactor) && !learnerPair.getValue()) {
                this.success = executeOperation(acceptedValue);
                learnerPair.setValue(true);
            }
            this.learningRecord.put(proposalId, learnerPair);
        }
    }

    /**
     * Generates a unique proposal ID.
     * @return A unique proposal ID.
     */
    private String generateProposalId() throws RemoteException {
        // Placeholder code to generate a unique proposal ID
        return uniqueServerId + ":" + System.currentTimeMillis();
    }

    /**
     * Apply the given operation to the key-value store.
     * @param operation The operation to apply.
     */
    private boolean executeOperation(Operation operation) throws RemoteException {
        if (operation == null) return false;
        switch (operation.type.toUpperCase()) {
            case "PUT":
                keyValueStore.put(operation.key, operation.value);
                helper.logMessage("PUT Operation successful for Key:Value - " + operation.key + ":" + operation.value);
                return true;
            case "DELETE":
                if(keyValueStore.containsKey(operation.key)) {
                    keyValueStore.remove(operation.key);
                    helper.logMessage("DELETE Operation successful for Key - " + operation.key );
                    return true;
                } else {
                   helper.logMessage("DELETE Operation Failed for Key - " + operation.key );
                    return false;
                }
            default:
                helper.logError("Unknown operation type: " + operation.type);
                return false;
        }
    }
    /**
     * Set an Server as down
     */
    public void setServerDown() {
        this.serverStatus = true;
        this.downtimeTracker = System.currentTimeMillis() / 1000L;
    }

}

/**
 * Operation class represents an operation on the key-value store.
 */

class Operation {
    String type;
    String key;
    String value;

    /**
     * Constructor to create an Operation instance.
     * @param type The type of operation (PUT or DELETE).
     * @param key The key associated with the operation.
     * @param value The value associated with the operation (used in PUT operations).
     */
    Operation(String type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

}

/**
 * Pair class represents a pair of two values of different types.
 * @param <K> The type of the first value.
 * @param <T> The type of the second value.
 */
class Pair<K, T> {
    private T value;
    private K key;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    Pair(K key, T value) {
        this.key = key;
        this.value = value;
    }
}
