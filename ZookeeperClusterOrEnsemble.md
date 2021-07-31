#Operating System
- Apache KAFKA is a JAVA application and can run on any OS.

#Details aboout zookeeper
1. Stores metadata about the KAFKA cluster (brokers).
2. Stores consumer client details.
3. Keeps track of status of the Kafka cluster nodes.
4. Keeps track of Kafka topics, partitions etc.
5. [Ensemble] Zookeeper cluster is called an ensemble.
    - [Sizing Your Ensemble] 
        - As per algorithm, it's recommended that ensemble contains odd number of servers (3,5,...) as majority of ensemble members must be working in order for Zookeeper to respond to requests. In a 3-node ensemble, if one node is failing, 2 are still working, 5-node ensemle, you can run with 2 nodes failing.
        - When we make configuration changes to the ensemble, nodes need to be reloaded one at a time. Thus, it should be able to tolerate more than one node down.
        - To create a deployment that can tolerate the failure of F machines, you should count on deploying `2xF+1 machines`.
        - [Max 7] nodes are recommended as performace can degrade due to consensus protocol.
6. To configure Zookeeper servers in an ensemble, they should have common configuration that lists all the servers.
    - [tickTime] unit in milliseconds
    - [initLimit] Amount of time to allow followers to connect with a leader. If `initLimit=20`, then it's `20* tickTime = 40000 ms = 40 seconds`
    - [syncLimit] Limits how out-of-sync followers can be with the leader. If `syncLimit=5`, then it's `5 * tickTime = 10000 ms = 10 seconds`
    - Configuration Lists each server in the ensemble.
    - [Format] `server.X=hostname:peerPort:leaderPort`= `server.1=zoo1.example.com:2888:3888`
        - `X`= ID number of the server. Just an integer. No sequential ordering, need not be zero-based.
        - `hostname`= hostname or IP address of the server.
        - `peerPort`= TCP port over which servers in ensemble will communicate with each other.
        - `leaderPort`= TCP port over which leader election is performed.
7. Clients willl connect to the ensemble over the `clientPort`.
8. The members of the ensemble can communicate with each other over all the three ports- `peerPort`, `leaderPort` & `clientPort`.
9. Each server needs a `myid` file in the data directory to specify the `ID number of the server`.

#Installation 3-node ensemble
- Ensure JAVA is installed.
- Installing Zookeeper
    1. SET JDK path in variable `JAVA_HOME` depending on which shell you are working.
        ```
        export JAVA_HOME=$(/usr/libexec/java_home)
        ```
        - For zsh, copy the above command in `~/.zshrc`
        - For bash, copy the above command in `~/.bashrc`
    2. Download zookeeper tar file from <link>https://zookeeper.apache.org/releases.html</link>
       ```
       sudo mv ~/Downloads/apache-zookeeper-3.7.0-bin /usr/local/zookeeper
       ```
    3. Double clicking on this above file will uncompress it.
    4. Do the following to make 3-node ensemble & create file `myid` in each of the following folders. `myid` will uniquely define each node of the ensemble. 
        ```
        sudo mkdir /var/lib/zookeeper

        sudo mkdir /var/lib/zookeeper/1
        cat > /var/lib/zookeeper/1/myid << EOF
        > 1
        > EOF

        sudo mkdir /var/lib/zookeeper/2
        cat > /var/lib/zookeeper/2/myid << EOF
        > 2
        > EOF

        sudo mkdir /var/lib/zookeeper/3
        cat > /var/lib/zookeeper/3/myid << EOF
        > 3
        > EOF
        ```
    5. Each node of an ensemble must know how to communicate with other nodes,hence a configuration file needs to be written in `/usr/local/zookeeper/conf` for each node in the ensemble.
    Create a file `zoo.cfg` in `/usr/local/zookeeper/conf`
        ```
        dataDir=/var/lib/zookeeper/1 # note the directory name 1
        clientPort=2181
        initLimit=5
        syncLimit=2
        # localhost can be machine name (machine-name.local) of your Mac!
        server.1=localhost:2888:3888
        server.2=localhost:2889:3889
        server.3=localhost:2890:3890

        # The id in the myid file on each machine must match the "server.X" definition. 
        # Eg- the ZooKeeper instance "server.1" in the above example, must have a myid file containing the value "1"
        ```
    6. For node 2 & node 3-
        ```
        cp zoo.cfg zoo2.cfg
        cp zoo.cfg zoo3.cfg

        #Update zoo2.cfg & zoo3.cfg by updating the clientPort & dataDir
        ```
        ```
        #zoo2.cfg
        dataDir=/var/lib/zookeeper/2
        # the port at which the clients will connect
        clientPort=2182
        ...
        ```
        ```
        #zoo3.cfg
        dataDir=/var/lib/zookeeper/3
        # the port at which the clients will connect
        clientPort=2183
        ...
        ```
    7. Start each zoopkeeper instance 
        ```
        cd /usr/local/zookeeper/bin
        sudo ./zkServer.sh start zoo.cfg
        sudo ./zkServer.sh start zoo2.cfg
        sudo ./zkServer.sh start zoo3.cfg
        ```
    8. To stop each zookeeper instance
        ```
        sudo ./zkServer.sh stop zoo.cfg
        sudo ./zkServer.sh stop zoo2.cfg
        sudo ./zkServer.sh stop zoo3.cfg
        ```
    9. To test the zookeeper instance is up
        ```
        # 2181 is the clientPort for zookeeper 1
        telnet localhost 2181
        # send the 4-letter command srvr
        ```
    
    

