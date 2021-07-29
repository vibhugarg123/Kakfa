#Operating System
- Apache KAFKA is a JAVA application and can run on any OS.

#Installation
- Ensure JAVA is installed.
- Installing Zookeeper [TODO]
    - Stores metadata about the KAFKA cluster (brokers).
    - Stores consumer client details.
    - Keeps track of status of the Kafka cluster nodes.
    - Keeps track of Kafka topics, partitions etc.
    - [Ensemble] Zookeeper cluster is called an ensemble.
    - [Sizing Your Ensemble] 
        - As per algorithm, it's recommended that ensemble contains odd number of servers (3,5,...) as majority of ensemble members must be working in order for Zookeeper to respond to requests. In a 3-node ensemble, if one node is failing, 2 are still working, 5-node ensemle, you can run with 2 nodes failing.
        - When we make configuration changes to the ensemble, nodes need to be reloaded one at a time. Thus, it should be able to tolerate more than one node down.
        - [Max 7] nodes are recommended as performace can degrade due to consensus protocol.
    - To configure Zookeeper servers in an ensemble, they should have common configuration that lists all the servers
        - [tickTime] unit in milliseconds
        - [initLimit] Amount of time to allow followers to connect with a leader. If `initLimit=20`, then it's `20* tickTime = 40000 ms = 40 seconds`
        - [syncLimit] Limits how out-of-sync followers can be with the leader. If `syncLimit=5`, then it's `5 * tickTime = 10000 ms = 10 seconds`
        - Configuration Lists each server in the ensemble.
        - [Format] `server.X=hostname:peerPort:leaderPort`= `server.1=zoo1.example.com:2888:3888`
            - `X`= ID number of the server. Just an integer. No sequential ordering, need not be zero-based.
            - `hostname`= hostname or IP address of the server.
            - `peerPort`= TCP port over which servers in ensemble will communicate with each other.
            - `leaderPort`= TCP port over which leader election is performed.
    - Clients willl connect to the ensemble over the `clientPort`.
    - The members of the ensemble can communicate with each other over all the three ports- `peerPort`, `leaderPort` & `clientPort`.
    - Each server needs a `myid` file in the data directory to specify the `ID number of the server`.
    
    

