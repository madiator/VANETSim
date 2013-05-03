VANETSim: A Vehicular Network Content Dissemination Simulator
========

Introduction
------------

VANETSim is a java based tool that:
* provides a set of APIs that can connect to databases and fetch structured data, such as vehicle's location information etc.
* can be used to simulate storage of files on vehicles and content dissemination. 


Design
------


### Modules

All these components are made up of multiple modules:

* [Node Manager][NodeManager]: It will manage the list of nodes. 
* [Location Manager][LocationManager]: It will manage the location of the nodes. It is possible to plug in some module to generate the location of nodes according to some model (for example random walk, but that code needs to be written). Right now it uses the location information stored in datasets (Beijing/Chicago). 
* [Database Manager][DBManager]: This module will handle all database transactions. Currently there is one flavor of the [DBManager][DBManager] - [MongoManager], which talks with a MongoDB database to find the location of nodes etc. Similarly, other databases (such as mySQL) can be used. 
* [Storage Manager][StorageManager]: This manages the storage of all the nodes. It has all the functionality to store files, copy file from one node to another, delete file and so on. It further has smaller modules such as [NodeStorageManager][StorageManager], which handles the storage of a single node and a class called [File][StorageManager], which represents files. Currently, the code allows storing and handling uncoded files, and handling coded files will not be tough - coded files can be treated as many uncoded files, except there needs to be a way to group bunch of coded blocks. Also, it is possible to modify functionality here oblivious to the simulator to perform random storage allocation, to handle files of different sizes etc. Current allocation strategy is deterministic and all files are of the same size. 
* [Time Manager][TimeManager]: Handles the time from the dataset. This is useful to hide the irregular times recorded in the dataset and allows for time increments. 

### Configuration
There is a class called [Configuration][Configuration] which stores all the global parameters. All the modules access required parameters through this class. In the future, this class can read the configuration variables from an XML document. Example parameters are number of files, file size etc. 

Running
-------
Before running the Simulator, you need to prepare the dataset (if its being used) and possibly run a few one time utilities in utils/. 
For example, ChicagoExporter and BeijingExporter were used to get already existing data from a trace of Chicago dataset and a trace of Beijing and store in a convenient format in MongoDB. 

Why we use MongoDB
-------
In general, one needs to store the dataset in a database instead of say a file so that it is easier to read the GPS values etc. on the basis of time. 
MongoDB supports geospatial querying. 


[Simulator]: https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/Simulator.java
[NodeManager]: https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/manager/NodeManager.java
[LocationManager]: https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/manager/LocationManager.java
[TimeManager]: https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/manager/TimeManager.java
[DBManager]: https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/manager/DBManager.java
[MongoManager]:  https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/manager/MongoManager.java
[StorageManager]: https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/manager/StorageManager.java
[Configuration]: https://github.com/madiator/VANETSim/blob/master/src/edu/usc/anrg/vsim/discrete/support/Configuration.java