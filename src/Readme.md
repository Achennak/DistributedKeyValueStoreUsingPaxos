Server Terminal:
1. Compile the server :
    - `javac PaxosServer.java`
2. Run the server: pass one port number 
    - `java PaxosServer <Port Number> <RemoteObjectName> `
      Example: `java PaxosServer 5000 paxos`(starts servers on ports 5000 5001 5002 5003 5004)

Client Terminal:
1. Compile the client code:
    - `javac Client.java`
2. Run the client:
    - `java Client <localHost> <PortNumber> <RemoteObjectName>`
      Example: `java Client 127.0.0.1 5000 paxos`(pass only those 5 ports 5000 5001 5002 5003 5004)
3. Upon starting the client, the first five PUT, GET, and DELETE requests are automated to be sent to the server directly. Afterward, users can input their requests manually.
   4.In client terminal, we can give input as server to which wants to connect.


Additional Details:
1. Both the server and client utilize Remote Method Invocation (RMI) for communication.
2. Requests from the client to the server should be provided in the format: PUT <key> <value>, GET <key>, or DELETE <key> or SHUTDOWN, with each word separated by a space. Ensure to start with PUT to avoid key not found errors.
   Valid examples:
    - `PUT 3 4`
    - `PUT mango fruits`
    - `GET 3`
    - `GET mango`
    - `DELETE 3`
      Invalid examples:
    - `PUT`
    - `PUT 5`
    - `PUT 6 7 8`
    - `GET`
    - `DELETE 5 6`
    - `DELETE`
3.Keys and values are of string data type in the hashmap.

Executive Summary:
Project #4 aims to enhance the fault tolerance of a replicated Key-Value Store Server by integrating the Paxos algorithm for consensus among the server replicas.
This involves implementing Paxos roles such as Proposers, Acceptors, and Learners to ensure continual operation despite replica failures.
Additionally, acceptor threads are configured to fail randomly to simulate real-world scenarios, demonstrating how Paxos overcomes replicated server failures.

Technical Impression:
Implementing Paxos for fault-tolerant consensus in the replicated Key-Value Store Server was both challenging and enlightening. Understanding the intricacies of Paxos and its role in ensuring consistency among distributed replicas required a deep dive into distributed systems theory.
Integrating Paxos roles within the existing architecture demanded careful consideration of threading and fault handling mechanisms. The random failure and restart of acceptor threads added another layer of complexity, highlighting the resilience of Paxos in the face of unpredictable failures.
Overall, the project provided valuable insights into fault tolerance strategies and the importance of consensus algorithms in distributed systems.