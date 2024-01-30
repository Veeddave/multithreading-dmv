/*

   Veed Dave
   Vxd180012
   10/29/2022



 */
// Do the imports

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Project2 {

    // Constants for the number of customers, agents, info desks, and waiting room capacity
    private static final int CUSTOMERS = 20;
    private static final int AGENTS = 2;
    private static final int INFO_DESKS = 1;
    private static final int WAITING_ROOM_CAPACITY = 4;

    // Semaphores for synchronization
    private final Semaphore agentsAvailable = new Semaphore(1);
    private final Semaphore agentLineSemaphore = new Semaphore(2);
    private final Semaphore theCustomers = new Semaphore(2, true);
    private final Semaphore theInsDesk = new Semaphore(1, true);
    private final Semaphore theAnnouncer = new Semaphore(1, true);
    private final Semaphore waitingRoom = new Semaphore(WAITING_ROOM_CAPACITY, true);

    // Arrays to store customer-related information
    private final Customer[] customers = new Customer[CUSTOMERS];
    private final Agent[] agents = new Agent[AGENTS];
    private final InfoDesk[] infoDesks = new InfoDesk[INFO_DESKS];
    private final Announcer[] announcers = new Announcer[INFO_DESKS];
    private final WaitingRoom[] waitingRooms = new WaitingRoom[INFO_DESKS];

    // Additional variables used in the program
    private int waitingnum = 0;
    private int[][] waitingarray = new int[CUSTOMERS][1];
    private int[] callednums = new int[CUSTOMERS];
    private int[] agentline = new int[AGENTS];
    private int agentlinenum = 0;

    // Constructor to initialize the objects
    public Project2() {
        initialize();
    }

    // Method to initialize objects
    private void initialize() {
        // Initialize customer objects
        for (int i = 0; i < CUSTOMERS; ++i) {
            customers[i] = new Customer(i);
        }

        // Initialize info desk, announcer, and waiting room objects
        for (int i = 0; i < INFO_DESKS; ++i) {
            infoDesks[i] = new InfoDesk(i);
            announcers[i] = new Announcer(i);
            waitingRooms[i] = new WaitingRoom(i);
        }

        // Initialize agent objects
        for (int i = 0; i < AGENTS; ++i) {
            agents[i] = new Agent(i);
        }
    }

    // Main execution method
    public void run() {
        // Create and run threads for customers, info desks, announcers, and agents
        createAndRunThreads(customers);
        createAndRunThreads(infoDesks);
        createAndRunThreads(announcers);
        createAndRunThreads(agents);

        // Wait for threads to finish execution
        waitForThreadsToFinish(customers);
        waitForThreadsToFinish(infoDesks);
        waitForThreadsToFinish(announcers);
        waitForThreadsToFinish(agents);

        // Display completion message
        System.out.println("Done");
    }

    // Method to create and run threads for an array of runnables
    private void createAndRunThreads(Runnable[] runnables) {
        for (Runnable runnable : runnables) {
            new Thread(runnable).start();
        }
    }

    // Method to wait for threads to finish execution
    private void waitForThreadsToFinish(Thread[] threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Main method to start the program
    public static void main(String[] args) {
        Project2 thecurrent = new Project2();
        thecurrent.run();
    }

    // Inner class representing a customer
    public class Customer implements Runnable {
        private final int num;

        // Constructor for Customer
        Customer(int num) {
            this.num = num;
        }

        // Run method for Customer
        public void run() {
            try {
                // Acquire semaphore for entering the DMV
                theInsDesk.acquire();
                System.out.println("Customer " + (num + 1) + " created, enters DMV");

                // Process customer at info desk, add to line, and enter waiting room
                infoDesks[0].processCustomer(this.num);
                waitingRooms[0].addToLine(announcers[0]);
                waitingRoom.acquire();
                waitingRoom.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Release semaphore and announce customer number
                theInsDesk.release();
                theAnnouncer.acquire();
                announcers[0].announceNumber(this.num);
                theCustomers.release();
                theAnnouncer.release();
            }
        }
    }

    // Inner class representing an agent
    public class Agent implements Runnable {
        private final int num;
        private int customerNum;

        // Constructor for Agent
        Agent(int num) {
            this.num = num;
        }

        // Run method for Agent
        public void run() {
            serve();
        }

        // Method to set customer number
        public void bigset(int customerNum) {
            this.customerNum = customerNum;
        }

        // Method representing the service provided by the agent
        public void serve() {
            System.out.println("Customer " + (customerNum + 1) + " is being served by agent " + (num + 1));
            eyeExam();
        }

        // Method representing the eye exam
        public void eyeExam() {
            System.out.println("Agent " + (num + 1) + " asks customer " + (customerNum + 1) + " to take photo and eye exam");
            finishExam();
        }

        // Method to finish the exam
        public void finishExam() {
            System.out.println("Customer " + (customerNum + 1) + " completes photo and eye exam for agent " + (num + 1));
            giveId();
        }

        // Method to give the ID
        public void giveId() {
            System.out.println("Customer " + (customerNum + 1) + " gets license and departs");
        }
    }

    // Inner class representing an agent line
    public class AgentLine implements Runnable {
        private int num;
        private int am;

        // Constructor for AgentLine
        AgentLine(int num) {
            this.num = num;
        }

        // Method to set customer number for the agent line
        public void bigset(int i) {
            this.am = i;
        }

        // Run method for AgentLine
        public void run() {
            try {
                // Acquire semaphore for agent line
                agentLineSemaphore.tryAcquire(1);

                // Add customer to agent line and run agent
                agentline[agentlinenum] = this.am;
                System.out.println("Customer " + (this.am + 1) + " moves to agent line");
                agents[0].bigset(am);
                agentlinenum = (agentlinenum + 1) % AGENTS;
                agentsAvailable.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Inner class representing an announcer
    public class Announcer implements Runnable {
        private int num;
        private int current;

        // Constructor for Announcer
        Announcer(int num) {
            this.num = num;
        }

        // Method to set customer number for the announcer
        public void bigset(int i) {
            this.current = i;
        }

        // Method to set the called number
        void set() {
            callednums[this.current] = waitingarray[this.current][0];
        }

        // Run method for Announcer
        public void run() {
            // Set called number and release semaphore for customers
            set();
            System.out.println("Announcer calls number " + (callednums[this.current] + 1));
            theCustomers.release();
        }
    }

    // Inner class representing the waiting room
    public class WaitingRoom implements Runnable {
        private int num;

        // Constructor for WaitingRoom
        WaitingRoom(int i) {
            this.num = i;
        }

        // Method to add a customer to the line
        public void addToLine(Announcer announcer) {
            threadArrLine.add(new Thread(announcer));
        }

        // Run method for WaitingRoom
        public void run() {
            theq++;
        }
    }

    // Inner class representing an information desk
    public class InfoDesk implements Runnable {
        private int num;
        private int stored;

        // Constructor for InfoDesk
        InfoDesk(int num) {
            this.num = num;
        }

        // Method to process a customer at the info desk
        public void processCustomer(int customerNum) {
            waitingarray[customerNum][0] = waitingnum;
            System.out.println("Customer " + (customerNum + 1) + " gets number " + (waitingnum + 1) + ", enters waiting room");
            waitingnum++;
        }

        // Method to set customer number for the info desk
        public void bigset(int i) {
            this.stored = i;
        }

        // Run method for InfoDesk
        public void run() {
            // InfoDesk logic...
        }
    }
}
