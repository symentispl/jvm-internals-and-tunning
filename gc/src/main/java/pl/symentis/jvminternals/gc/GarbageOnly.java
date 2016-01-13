package pl.symentis.jvminternals.gc;


/**
 * A GC micro benchmark running a given number of threads that create objects of a given size and dereference them immediately
 * 
 * @author jsound
 */
public class GarbageOnly {
    // object size in bytes
    private static final int DEFAULT_NUMBEROFTHREADS=8;
    private static final int DEFAULT_OBJECTSIZE=100;
    
    private static int numberOfThreads=DEFAULT_NUMBEROFTHREADS;
    private static int objectSize=DEFAULT_OBJECTSIZE;
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // first (optional) argument is the number of threads to run
        if( args.length>0 ) {
            numberOfThreads = Integer.parseInt(args[0]);
            // second (otpional) argument is the size of the objects
            if( args.length>1 ) {
                objectSize = Integer.parseInt(args[1]);
            }
        }
        System.out.println("Creating objects of size "+objectSize+" with "+numberOfThreads+" threads");
        
        // run the configured number of GC producer threads
        for(int i=0; i<numberOfThreads; i++) {
            new Thread(new GCProducer()).start();
        }
        try {
            // Let the benchmark run 5 minutes (using the -Xloggc:<filename> JVM parameter)
            // For evaluation of the GC rate from the GC log I disregard the first minute as warmup and 
            // use the average of the remaining 4 minutes as a score value
            Thread.sleep(300000);
        } catch( InterruptedException iexc) {
            iexc.printStackTrace();
        }
        System.exit(0);
    }
    
    public static class GCProducer implements Runnable {

        @Override
        public void run() {
            // make object creation as cheap as possible to measure GC performance with
            // as little overhead as possible
            int osize = objectSize; 
            while(true) {
                // create character arrays of the configured size
                char[] garbageObject = new char[osize];
            }
        }
    }
}
