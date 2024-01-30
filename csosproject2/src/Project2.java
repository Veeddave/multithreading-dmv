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

    private static final int CUSTOMERS = 20;
    private static final int AGENTS = 2;
    private static final int INFO_DESKS = 1;
    private static final int WAITING_ROOM_CAPACITY = 4;

    private final Semaphore agentsAvailable = new Semaphore(1);
    private final Semaphore agentLineSemaphore = new Semaphore(2);
    private final Semaphore theCustomers = new Semaphore(2, true);
    private final Semaphore theInsDesk = new Semaphore(1, true);
    private final Semaphore theAnnouncer = new Semaphore(1, true);
    private final Semaphore waitingRoom = new Semaphore(WAITING_ROOM_CAPACITY, true);

    private final Customer[] customers = new Customer[CUSTOMERS];
    private final Agent[] agents = new Agent[AGENTS];
    private final InfoDesk[] infoDesks = new InfoDesk[INFO_DESKS];
    private final Announcer[] announcers = new Announcer[INFO_DESKS];
    private final WaitingRoom[] waitingRooms = new WaitingRoom[INFO_DESKS];

    private int waitingnum = 0;
    private int[][] waitingarray = new int[CUSTOMERS][1];
    private int[] callednums = new int[CUSTOMERS];
    private int[] agentline = new int[AGENTS];
    private int agentlinenum = 0;

    public Project2() {
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < CUSTOMERS; ++i) {
            customers[i] = new Customer(i);
        }

        for (int i = 0; i < INFO_DESKS; ++i) {
            infoDesks[i] = new InfoDesk(i);
            announcers[i] = new Announcer(i);
            waitingRooms[i] = new WaitingRoom(i);
        }

        for (int i = 0; i < AGENTS; ++i) {
            agents[i] = new Agent(i);
        }
    }

    public void run() {
        createAndRunThreads(customers);
        createAndRunThreads(infoDesks);
        createAndRunThreads(announcers);
        createAndRunThreads(agents);

        waitForThreadsToFinish(customers);
        waitForThreadsToFinish(infoDesks);
        waitForThreadsToFinish(announcers);
        waitForThreadsToFinish(agents);

        System.out.println("Done");
    }

    private void createAndRunThreads(Runnable[] runnables) {
        for (Runnable runnable : runnables) {
            new Thread(runnable).start();
        }
    }

    private void waitForThreadsToFinish(Thread[] threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Project2 thecurrent = new Project2();
        thecurrent.run();
    }

    public class Customer implements Runnable {
        private final int num;

        Customer(int num) {
            this.num = num;
        }

        public void run() {
            try {
                theInsDesk.acquire();
                System.out.println("Customer " + (num + 1) + " created, enters DMV");
                infoDesks[0].processCustomer(this.num);
                waitingRooms[0].addToLine(announcers[0]);
                waitingRoom.acquire();
                waitingRoom.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                theInsDesk.release();
                theAnnouncer.acquire();
                announcers[0].announceNumber(this.num);
                theCustomers.release();
                theAnnouncer.release();
            }
        }
    }

    public class Agent implements Runnable {
        private final int num;
        private int customerNum;

        Agent(int num) {
            this.num = num;
        }

        public void run() {
            serve();
        }

        public void bigset(int customerNum) {
            this.customerNum = customerNum;
        }

        public void serve() {
            System.out.println("Customer " + (customerNum + 1) + " is being served by agent " + (num + 1));
            eyeExam();
        }

        public void eyeExam() {
            System.out.println("Agent " + (num + 1) + " asks customer " + (customerNum + 1) + " to take photo and eye exam");
            finishExam();
        }

        public void finishExam() {
            System.out.println("Customer " + (customerNum + 1) + " completes photo and eye exam for agent " + (num + 1));
            giveId();
        }

        public void giveId() {
            System.out.println("Customer " + (customerNum + 1) + " gets license and departs");
        }
    }

    public class AgentLine implements Runnable {
        private int num;
        private int am;

        AgentLine(int num) {
            this.num = num;
        }

        public void bigset(int i) {
            this.am = i;
        }

        public void run() {
            try {
                agentLineSemaphore.tryAcquire(1);
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

    public class Announcer implements Runnable {
        private int num;
        private int current;

        Announcer(int num) {
            this.num = num;
        }

        public void bigset(int i) {
            this.current = i;
        }

        void set() {
            callednums[this.current] = waitingarray[this.current][0];
        }

        public void run() {
            set();
            System.out.println("Announcer calls number " + (callednums[this.current] + 1));
            theCustomers.release();
        }
    }

    public class WaitingRoom implements Runnable {
        private int num;

        WaitingRoom(int i) {
            this.num = i;
        }

        public void addToLine(Announcer announcer) {
            threadArrLine.add(new Thread(announcer));
        }

        public void run() {
            theq++;
        }
    }

    public class InfoDesk implements Runnable {
        private int num;
        private int stored;

        InfoDesk(int num) {
            this.num = num;
        }

        public void processCustomer(int customerNum) {
            waitingarray[customerNum][0] = waitingnum;
            System.out.println("Customer " + (customerNum + 1) + " gets number " + (waitingnum + 1) + ", enters waiting room");
            waitingnum++;
        }

        public void bigset(int i) {
            this.stored = i;
        }

        public void run() {
            // InfoDesk logic...
        }
    }
}
