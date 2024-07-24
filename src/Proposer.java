import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Proposer interface defines the behavior of proposers in the Paxos consensus algorithm.
 * Proposers are responsible for initiating the Paxos rounds, proposing values to be agreed upon
 * by the system, and ensuring that a consensus is eventually reached.
 */
public interface Proposer extends Remote {


    /**
     * Initiates the Paxos protocol by proposing a value with the given proposalId.
     *
     * @param proposalId    The ID of the proposal being made
     * @param proposalValue The value proposed by the proposer
     * @return True if the proposal is accepted and consensus is reached, false otherwise
     * @throws RemoteException If a communication-related exception occurs
     */
    void propose(String proposalId, Operation proposalValue) throws RemoteException, InterruptedException;
}
