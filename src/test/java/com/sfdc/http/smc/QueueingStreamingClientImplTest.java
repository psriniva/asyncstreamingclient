package com.sfdc.http.smc;

import com.sfdc.http.queue.ProducerInterface;
import com.sfdc.http.queue.StreamingConsumer;
import com.sfdc.http.queue.StreamingProducer;
import com.sfdc.http.queue.WorkItemInterface;
import com.sfdc.http.util.SoapLoginUtil;
import com.sfdc.stats.StatsManager;
import junit.framework.TestCase;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

/**
 * @author psrinivasan
 *         Date: 9/20/12
 *         Time: 11:29 AM
 */
public class QueueingStreamingClientImplTest extends TestCase {
    private QueueingStreamingClientImpl streamingClient;
    private String sessionId;
    private String instance;
    private ProducerInterface producer;
    private StreamingConsumer streamingConsumer;
    private Thread consumerThread;

    public void setUp() throws Exception {
        //String[] credentials = SoapLoginUtil.login("dpham@180.private.streaming.20.org8", "123456", "https://ist6.soma.salesforce.com/");
        String[] credentials = SoapLoginUtil.login("admin@ist8.streaming.20.systest.org501", "123456", "https://ist8.soma.salesforce.com/");
        sessionId = credentials[0];
        instance = credentials[1];
        LinkedBlockingDeque<WorkItemInterface> queue = new LinkedBlockingDeque<WorkItemInterface>();
        producer = new StreamingProducer(queue, true, StatsManager.getInstance());
        Semaphore numConcurrentClients = new Semaphore(2);
        streamingConsumer = new StreamingConsumer(queue, numConcurrentClients, true, StatsManager.getInstance(), true);
        String[] channels = {"/topic/accountTopic", "/topic/c1Topic"};
        long runtime = 0;
        streamingClient = new QueueingStreamingClientImpl(sessionId, instance, producer, producer, channels, new Semaphore(1), new Date(new Date().getTime() + runtime), new Semaphore(1));
        consumerThread = new Thread(streamingConsumer);
        consumerThread.start();

    }

    public void tearDown() throws Exception {
        //consumerThread.interrupt();

    }

    public void testStart() throws Exception {
        streamingClient.start();
        Thread.sleep(115000); // 110 seconds for connect to expire, and 5 seconds for everything else.
        if (streamingClient.getState() != "FSM.Done") {
            System.out.println("End state not reached yet, sleeping for 10 more seconds.");
            //sleep for a 10 more seconds hoping we'll be done.
            Thread.sleep(10000);
        }
        assertEquals("FSM.Done", streamingClient.getState());
    }
}
