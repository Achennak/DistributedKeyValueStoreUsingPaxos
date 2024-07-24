import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Learner interface defines the behavior of learners in the Paxos consensus algorithm.
 * Learners are responsible for learning the values that have been accepted by a majority of
 * acceptors, thus achieving consensus.
 */
public interface Learner extends Remote {

    /**
     * Informs the learner about a proposal that has been accepted by a majority of acceptors.
     * Upon receiving this information, the learner updates its state to reflect the accepted value
     * and the proposal ID.
     *
     * @param proposalId    The ID of the proposal that has been accepted
     * @param acceptedValue The value that has been accepted by the majority of acceptors
     * @throws RemoteException If a communication-related exception occurs
     */
    void learn(String proposalId, Operation acceptedValue) throws RemoteException;

}
