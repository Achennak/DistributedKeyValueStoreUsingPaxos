import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Remote interface for the KeyValueStore, representing a distributed key-value store system.
 * Allows clients to perform operations such as get, put, and delete on key-value pairs.
 */
public interface KVStore extends Remote {

    /**
     * Method to put key-value pair in the key-value store
     * @param key integer key
     * @param value string value
     * @return string success/failure message
     * @throws RemoteException if procedure can't be called
     */
    String put(String key, String value) throws RemoteException, InterruptedException;

    /**
     * Method to delete a key from the key-value store
     * @param key integer key
     * @return string success/failure message
     * @throws RemoteException if procedure can't be called
     */
    String delete(String key) throws RemoteException, InterruptedException;

    /**
     * Method to get a value from key-value store
     * @param key integer key
     * @return associated string value of key
     * @throws RemoteException if procedure can't be called
     */
    String get(String key) throws RemoteException, InterruptedException;

    /**
     * Method to check if the key-value store contains a specific key.
     *
     * @param key the key to be checked for existence in the store
     * @return true if the key is present in the store, otherwise false
     * @throws RemoteException    if the method encounters a network-related error
     * @throws InterruptedException if the method is interrupted while waiting
     */
    Boolean containsKey(String key) throws RemoteException, InterruptedException;

}
