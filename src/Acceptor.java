import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Acceptor interface defines the behavior of acceptors in the Paxos consensus algorithm.
 * Acceptors receive prepare and accept requests from proposers and respond accordingly to help
 * achieve consensus on a value.
 */
public interface Acceptor extends Remote {

    /**
     * Sends a prepare request to the acceptor, indicating a proposal with the given proposalId.
     * The acceptor responds with either a promise not to accept any proposal less than proposalId
     * or with information about the highest-numbered proposal that it has accepted.
     *
     * @param proposalId The ID of the proposal being prepared
     * @return The response from the acceptor, typically containing information about the highest
     *         numbered proposal the acceptor has accepted
     * @throws RemoteException If a communication-related exception occurs
     */
    Boolean prepare(String proposalId, Operation operation) throws RemoteException;

    /**
     * Sends an accept request to the acceptor, indicating a proposal with the given proposalId
     * and the value to be accepted. The acceptor responds with either an acknowledgement of the
     * acceptance of the proposal or a refusal to accept the proposal.
     *
     * @param proposalId    The ID of the proposal being accepted
     * @param proposalValue The value proposed to be accepted
     * @return The ID of the accepted proposal, typically matching the provided proposalId
     * @throws RemoteException If a communication-related exception occurs
     */
    void accept(String proposalId, Operation proposalValue) throws RemoteException;
}
