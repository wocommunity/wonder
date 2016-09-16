package er.extensions.concurrency;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXProperties;


public class ERXJobLoadBalancer {


    /*
     * This class solves the following problem:  we have a set of jobs (identified by an Id) waiting to be processed
     * Several worker processes are competing for jobs and we need to way to efficiently parcel out those jobs out 
     * We want to avoid as much as possible several workers attempting to grab the same jobs and locking it
     * 
     * This class will let Workers threads or processes ask for a 'JobSet', which is basically int1 modulo int2
     * The worker should then process jobs whose id = int1 module int2
     * 
     * This is NOT a substitute for proper locking, as there may be temporary situations where several workers attempt
     * to grab the same job.  Rather it ensures that, as steady state, jobs are handed out equally and adapts automatically
     * when either new workers are added or some workers die
     * 
     * This class assumes access to a shared file system.  This is where the state will be shared.  
     * It is also able to detect crashed or deadlocked workers and will adjust accordingly
     * 
     * The expected pattern is sth like:
     * 
     * WorkerIdentification wid=new WorkerIdentification("ProcessInvoices", 3);
     *   while(true) {
     *       jobLoadBalancer().heartbeat(wid);
     *       jobSet=jobLoadBalancer().idSpace(wid);
     *       ..  fetch N jobs whose primary key ==  jobset.index mod jobset.module
     *       for (i=0; i<N; i++_) {
     *           ..  process Job i
     *           jobLoadBalancer().heartbeat(wid);
     *       }
     * 
     * 
     * TO DO:  we probably would be able to automatically infer the Id of a given worker based on thread id + pid + host ip
     */
  
    
    private final static String SHARED_ROOT_LOCATION = "er.extensions.ERXJobLoadBalancer.RootLocation";
    private static final Logger log = LoggerFactory.getLogger(ERXJobLoadBalancer.class);
    /*
     * How old an entry in the shared state has to be before we consider its author dead
     */
    private final static String DEFAULT_DEAD_TIMEOUT_MILLIS = "er.extensions.ERXJobLoadBalancer.DefaultDeadTimeoutMillis";
    
    /**
     * Describes which jobs (index mod modulo) the worker should attempt to process
     */
    public static class JobSet {
        /*
         * Indicate to the worke what id space (index mod modulo) they should be attempting to process
         */
        public int _index;
        public int _modulo;
        public JobSet(int i, int m) { _index=i; _modulo=m; }  
        @Override
        public String toString() { return index()+" mod "+modulo(); }
        
        public int index() { return _index; }
        public int modulo() { return _modulo; }
    }
    
    private static ERXJobLoadBalancer _instance;
    public static ERXJobLoadBalancer jobLoadBalancer() {
        if (_instance==null) {
            _instance=new ERXJobLoadBalancer();
        }
        return _instance;
    }
    
    /**
     * Identifies a worker to the load balancer
     */
    public static class WorkerIdentification {
        String _type;
        String _id;
        public WorkerIdentification(String t, String i) { _type = t; _id=i; } 
        @Override
        public String toString() { return type()+"-"+id(); }
        public String id() { return _id; }
        public String type() { return _type; }
    }

    private String _sharedRootLocation;
    /**
     * @return the shared path where the state of the workers is stored
     */
    public String sharedRootLocation() {
        if (_sharedRootLocation == null) {
            _sharedRootLocation = ERXProperties.stringForKeyWithDefault(SHARED_ROOT_LOCATION, 
                    ERXProperties.stringForKeyWithDefault("java.io.tmpdir", "/tmp")+"/ERXJobLoadBalancer");
        }
        return _sharedRootLocation;
    }
    
    private File _sharedRoot;
    protected File sharedRoot() {
        if (_sharedRoot == null) {
            _sharedRoot= new File(sharedRootLocation());
        }
        return _sharedRoot;
    }
    
    
    
    private Map<String, Long> _ttlsPerType = new Hashtable<>();
    /**
     * @param type
     * @return the ttl for a given worker type.  An instance that has not called heartbeat for more than this TTL
     * will be considered dead by the other instances
     */
    public long ttlForWorkerType(String type) {
        // to do specify TTL per type?
        Long result = _ttlsPerType.get(type);
        if (result==null) {
            result = Long.valueOf(ERXProperties.longForKeyWithDefault(DEFAULT_DEAD_TIMEOUT_MILLIS, 60000L)); // 1mn by default
            _ttlsPerType.put(type, result);
        }
        return result.longValue();
    }
    /**
     * Sets the timeout for a given worker type
     * @param type
     * @param ttl (in milliseconds)
     */
    public void setTtlForWorkerType(String type, long ttl) {
    		_ttlsPerType.put(type, Long.valueOf(ttl));
    }

    protected String pathForWorkerIdentification(WorkerIdentification workerId) {
        return sharedRootLocation()+"/"+workerId.type()+"-"+workerId.id();
    }
    
    /**
     * Signals to the load balncer that the worker identified is alive
     * Clients should call this periodically, and certainly more often than the timeout
     * 
     * @param workerId which worker is alive
     */
    public void heartbeat(WorkerIdentification workerId) {
        /* method used to indicate the worker # workerId (process or thread) is alive */
        String pathForEntry = pathForWorkerIdentification(workerId);
        File entryFile = new File(pathForEntry);
        final File tempFile = new File(pathForEntry + "." + System.currentTimeMillis());
        log.debug("Writing Entry at {}: {}", tempFile, workerId);
        ObjectOutputStream out=null;
        try {
            // First make sure we have a directory
            File parentDir=tempFile.getParentFile();
            if (!parentDir.exists()) parentDir.mkdirs();
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
            // First we write when the entry expires                                                                                                                                                  
            long now=System.currentTimeMillis();
            // 1. now                                                                                                                                                                                 
            out.writeLong(now);
            // 2. write my Id
            out.writeUTF(workerId.id());
            log.debug("Wrote to {}", tempFile);

            out.close();
            out=null;
            tempFile.renameTo(entryFile);
        } catch (FileNotFoundException e) {
            log.error("Writing to {}", tempFile, e);
        } catch (IOException e2) {
            log.error("Writing to {}", tempFile, e2);
        } finally {
            if (out!=null)
                try {
                    out.close();
                } catch (IOException e) {}
        }
    }

        
    
    
    /**
     * @param workerId
     * @return the JobSet that the worker should attempt to process
     * Given a worker looks at the shared state and determine the id space (index mod module) they should be processing
     */
    public JobSet idSpace(final WorkerIdentification workerId) {
    		// heartbeat for ourselves
    		// this will ensure we don't overcount
    		ERXJobLoadBalancer.jobLoadBalancer().heartbeat(workerId);
    	
        FilenameFilter friendsFilter=new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.indexOf(workerId.type())==0;
            }
        };
        File[] friends = sharedRoot().listFiles(friendsFilter);
        // now check if each is alive
        int aliveFriendsCount=0;
        int aliveFriendsWithLowerIdFound=0;
        long now=System.currentTimeMillis();
        long ttl=ttlForWorkerType(workerId.type());
        for (int i=0; i<friends.length; i++) {
            File friend=friends[i];
            ObjectInputStream in=null;
            try {
                in = new ObjectInputStream(new FileInputStream(friend));                                                                                                                                                                      
                long entryCreationTime = in.readLong();
                String friendId = in.readUTF();
                if ((now-entryCreationTime)<ttl) {
                    aliveFriendsCount++;
                    if (friendId.compareTo(workerId.id())<0) {
                        aliveFriendsWithLowerIdFound++;
                    }
                } else {
                	// we found a dead worker - remove his entry to keep the shared directory clean
                	if (!friend.delete()) {
                		log.info("Could not delete dead worker entry: {}", friend);
                	}
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e2) {
            } finally {
                if (in!=null) try {
                    in.close();
                } catch (IOException e) {}
            }                
        }    
        // if we end up here with 0, we must have had a pb with the file system
        // in this case, just count ourselves and try to process everything
        if (aliveFriendsCount==0) {
        		aliveFriendsCount=1;
        		aliveFriendsWithLowerIdFound=0;
        }
        return new JobSet(aliveFriendsWithLowerIdFound, aliveFriendsCount);
    }

    /**
     * @return a String suitable to identify this particular worker instance
     * !!  this string is not MT safe
     */
    public String workerInstanceIdentification() {
        return ERXConfigurationManager.defaultManager().hostName()+"-"+System.getProperty("com.webobjects.pid");
    }
    
/*
 * -------------------------------------------------------------------------------------------------------------
 */

    /*
     * A simple test
     */
    public static void main(String[] args) {
         if (args.length<2) {
             usage(); System.exit(1);
         }
         String arg1=args[0];
         if (arg1.equals("-createJobs")) {
             createJobs(args[1], Integer.parseInt(args[2]));
         } else if (arg1.equals("-processJobs")) {
             processJobs(args[1],Integer.parseInt(args[2]));
         } else {
             System.out.println("Unrecognized: "+arg1);
             usage();
         }
     }
    public static void usage() {
        System.out.println("Usage -createJobs <workerType> <numberOfJobs> | -processJobs <workerType> <workerNumber>");
    }

    public static void createJobs(String workerType, int n) {
        System.out.println("Creating "+n+" jobs for "+workerType);
        jobLoadBalancer().sharedRoot().mkdirs();
        try {
            for (int i=0; i<n; i++) {
                File f=new File(jobLoadBalancer().sharedRootLocation(), "Job-"+i);
                f.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Could not create jobs: "+e);
        }
    }
    
    public static void processJobs(String workerType, int workerNumber) {
        WorkerIdentification wid=new WorkerIdentification(workerType,""+workerNumber);
        jobLoadBalancer().setTtlForWorkerType(workerType, 20000); // 20s
        while(true) {
            jobLoadBalancer().heartbeat(wid);
            System.out.println("Worker number "+workerNumber+" processing jobs #"+jobLoadBalancer().idSpace(wid));
            try { 
            		Thread.sleep(5000);
            		
            } catch (Exception e) {
            	System.out.println("ERXJobLoadBalancer.processJobs: " + e);
            }
        }
    }   

}
