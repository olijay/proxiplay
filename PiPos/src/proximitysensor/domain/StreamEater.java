/*
 * ProxiMagic - (c) 2013,2014 Rolf Bagge, Janus B. Kristensen - CAVI, Aarhus University
 * Website: https://laa.projects.cavi.au.dk
 */
package proximitysensor.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Input handling thread for reading process data reliably
 * 
 * @author Rolf
 */
public class StreamEater implements Runnable {
    private static final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            
            return t;
        }
    });
    
    private final InputStream stream;
    private final String logToken;
    private final PrintWriter logWriter;
    
    private StreamEater(InputStream stream, String logToken, OutputStream output) {
        this.stream = stream;
        this.logToken = logToken;
        if(output != null) {
            this.logWriter = new PrintWriter(new OutputStreamWriter(output));
        } else {
            this.logWriter = null;
        }
    }
    
    public static void eatStream(InputStream stream, String token, OutputStream logOutput) {
        StreamEater eater = new StreamEater(stream, token, logOutput);
        
        threadPool.submit(eater);
    }

    @Override
    public void run() {
        try {
            StringBuilder sb = new StringBuilder();
            
            int read = stream.read();
            while(read != -1) {
                switch((char) read) {
                    case '\r':
                        //Ignore \r
                        break;
                    case '\n':
                        if(logWriter != null) {
                            logWriter.println("["+logToken+"]" + sb.toString());
                        }
                        sb = new StringBuilder();
                        break;
                    default:
                        sb.append((char) read);
                }
                read = stream.read();
            }
        } catch (IOException ex) {
            Logger.getLogger(StreamEater.class.getName()).log(Level.SEVERE, "Error eating stream", ex);
        }
    }
}
