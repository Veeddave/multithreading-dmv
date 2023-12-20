/*

   Veed Dave
   Vxd180012
   10/29/2022



 */
// Do the imports
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.LinkedList;
//--------------------------------------------------------------------------------------------------------------------//
//Create the Project2 class
public class Project2 implements Runnable {

//--------------------------------------------------------------------------------------------------------------------//
    //initilaize the variables, semaphores, threads , and classes
     public  int num = 0;
     public int val = 0;
    public  int waitingnum= 0;
    public  int[][] waitingarray = new int[20][1];
    public  int[] callednums = new int[20];
    public   int[] agentline = new int[4];
    public   int agentlinenum = 0;
     public final int customers = 20;
     public  final int agents = 2;
     public  final int thedesk = 1;
    public Thread[] thecustomers= new Thread[customers];
    public Semaphore agentsAvailable = new Semaphore(1);
   public Semaphore agentlineSem = new Semaphore(2);
   public Semaphore theCustomers = new Semaphore(2,true);
    public  Semaphore theInsDesk = new Semaphore(1,true);
    public  Semaphore theAnnouncer = new Semaphore(1,true);
    public  Semaphore waitingRoom = new Semaphore(4,true);
    public  Customer[] thr = new Customer[customers] ;
    public  AgentLine agentLine = new AgentLine(1) ;
    public Agent[] theagents = new Agent[2];
    public InfoDesk[] thecurrdesk = new InfoDesk[1];
    public WaitingRoom[] roomwait = new WaitingRoom[1];
    public Thread[] deskThread = new Thread[1];
    public  Announcer[] theannouncer = new Announcer[thedesk];
    public Thread[] announcerThread = new Thread[thedesk];
    public Queue<Thread> threadArrLine = new LinkedList<Thread>();
    public Thread[] waiter = new Thread[1];
    public int theq =0;
    public  Thread[] agent = new Thread[agents];

    //--------------------------------------------------------------------------------------------------------------------//
     //project 2
    public Project2() {



    }

    //--------------------------------------------------------------------------------------------------------------------//
    //run for project2
    public void run(){


        //create the customers and threads
        for(int  i = 0; i < 20 ; ++i ) {


            thr[i] = new Customer(i);
            thecustomers[i] = new Thread( thr[i] );


        }

        //create the information desk and thread
        for(int i=0; i<1;++i){

            thecurrdesk[i] = new InfoDesk(i);
            deskThread[i] = new Thread(thecurrdesk[i]);
            System.out.println("Information desk created");

        }
        //create the announcer and thread
        for(int i=0; i<1;++i){

            theannouncer[i] = new Announcer(i);
            announcerThread[i] = new Thread(theannouncer[i]);
            System.out.println("Announcer created");

        }
        //create the agents and threads
        for(int i=0; i<2;++i){
            theagents[i] = new Agent(i);
            agent[i] = new Thread(theagents[i]);
            System.out.println("Agent " + (i+1) + " created");

        }

        //this will aquire and then run the thread
        for(int i=0; i< 20; ++i){

            try {
                theCustomers.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thecustomers[i].start();

        }

        //joins desk
        for(int i = 1;i<1;++i){


            try {
                deskThread[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //joins announcer
        for(int i = 1;i<1;++i){


            try {
                announcerThread[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //joins the agents
        for(int i = 1;i<2;++i){


            try {
                agent[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //customers join them and output it
        for( int i = 0; i < 20; ++i )
        {
            try
            {
                thecustomers[i].join();
                System.out.println("Customer " + (i+1) + " was joined");
                agentlineSem.release();
            }
            catch (InterruptedException e)
            {
            }
        }
        System.out.println("Done");

    }

    //--------------------------------------------------------------------------------------------------------------------//
    //main method create project 2 and runs it
    public static void main(String []args) throws InterruptedException {


        Project2 thecurrent = new Project2();
        thecurrent.run();

    }

    //--------------------------------------------------------------------------------------------------------------------//
    //customer class
    public  class Customer implements Runnable{
        //intilaize variables
       public int num;

        //--------------------------------------------------------------------------------------------------------------------//
       //customer
     Customer(int num){
         this.num = num;

     }

        //--------------------------------------------------------------------------------------------------------------------//
     //run
     public void run()
     {
         //trys
          try {
              //aquite semaphore
             theInsDesk.acquire();
             //prints and sets
             System.out.println("Customer " + (num+1) + " created, enters DMV");

             //sets
              thecurrdesk[0].bigset(num);
             deskThread[0] = new Thread(thecurrdesk[0]);
             //runs
             deskThread[0].run();

             //releases semaphore , sets
              theInsDesk.release();
              theannouncer[0].bigset(this.num);
              announcerThread[0] = new Thread(theannouncer[0]);
            //runs waiting room
              roomwait[0] = new WaitingRoom(num);
              waiter[0] = new Thread(roomwait[0]);
              waitingRoom.acquire();
              waiter[0].run();
              //release
              waitingRoom.release();






         } catch (InterruptedException e) {
             e.printStackTrace();
         }

          //trys to aquire announcer
         try {

             //runs the thread and then runs the agent line
                  theAnnouncer.acquire();
                  announcerThread[0].run();
                 agentLine.bigset(this.num);
                 agentLine.run();


         } catch (InterruptedException e) {
             e.printStackTrace();
         }

         //releases semaphore
            theAnnouncer.release();
     }
        //--------------------------------------------------------------------------------------------------------------------//
    }

    //--------------------------------------------------------------------------------------------------------------------//
    //this is the agent class
    public class Agent implements Runnable{

        //initalize variables
        int num;
        int thenum;

        //--------------------------------------------------------------------------------------------------------------------//
        //agent
        Agent(int num){

            this.num = num;
        }
        //--------------------------------------------------------------------------------------------------------------------//
        //agent setter for customer
        public void bigset(int i){

            this.thenum = i;
        }

        //--------------------------------------------------------------------------------------------------------------------//
        //run
        public void run(){
            serve();

        }
        //--------------------------------------------------------------------------------------------------------------------//
        //connects to the serve
        public void serve(){

            System.out.println("Customer " +   (this.thenum+1) + " is being served by agent " + (val+1));
            eyeExam();
        }

        //--------------------------------------------------------------------------------------------------------------------//
        //then connects to eye exam
        public void eyeExam() {
            System.out.println("Agent " + (val+1) + " asks customer " +   (this.thenum+1) + " to take photo and eye exam");
            finishExam();

        }
        //--------------------------------------------------------------------------------------------------------------------//
        //finish the exam
        public void finishExam(){

            System.out.println("Customer " +   (this.thenum+1) + " completes photo and eye exam for agent " + (val+1));
            giveId();

        }
        //--------------------------------------------------------------------------------------------------------------------//
        //give the id
        public void giveId(){

            System.out.println("Customer " +   (this.thenum+1) + " gets license and departs");

        }
        //--------------------------------------------------------------------------------------------------------------------//
    }
    //--------------------------------------------------------------------------------------------------------------------//
    //the agent line
    public  class AgentLine implements Runnable{

        int num;
        int am;

        //--------------------------------------------------------------------------------------------------------------------//
        //agent line
        AgentLine(int num){

            this.num = num;

        }
        //--------------------------------------------------------------------------------------------------------------------//
        //set agent line cus
        public void bigset(int i){

            this.am = i;

        }
        //--------------------------------------------------------------------------------------------------------------------//
        //run function
        public void run()
        {
            //semaphore , set line then move to line
                agentlineSem.tryAcquire(1);
                agentline[agentlinenum] = this.am;
                System.out.println("Customer " + (this.am+1) + " moves to agent line");
                //run agent
                theagents[0].bigset(am);
                agent[val] = new Thread(theagents[0]);
                agent[val].run();
                //agent number
               if(val==0){

                   val=1;
               }else{
                   val = 0;
               }
               agentsAvailable.release();
        }
        //--------------------------------------------------------------------------------------------------------------------//
    }

    //--------------------------------------------------------------------------------------------------------------------//
    //announcer
    public class Announcer implements Runnable {

        int num;
        int current;

        //--------------------------------------------------------------------------------------------------------------------//
        //announcer
        Announcer(int num) {

            this.num = num;
        }

        //--------------------------------------------------------------------------------------------------------------------//
        //set customer from announcer
        public void bigset(int i) {

            this.current = i;


        }

        //--------------------------------------------------------------------------------------------------------------------//
        //set the called num
        void set() {
            callednums[this.current] = waitingarray[this.current][0];
        }

        //run
        public void run() {
            //set
            set();
            //call and release
            System.out.println("Announcer calls number " + ((callednums[this.current]) + 1));
            theCustomers.release();
        }
        //--------------------------------------------------------------------------------------------------------------------//
    }

    //--------------------------------------------------------------------------------------------------------------------//
    //waiting room
    public class WaitingRoom implements Runnable {

        int num;

        //--------------------------------------------------------------------------------------------------------------------//
        //waiting room
        WaitingRoom(int i) {

            this.num=i;

        }

        //--------------------------------------------------------------------------------------------------------------------//
      //add too line and increment run
        public void run() {

                threadArrLine.add(announcerThread[0]);
                theq++;

        }

        //--------------------------------------------------------------------------------------------------------------------//
    }

    //--------------------------------------------------------------------------------------------------------------------//
    //info desk
    public class InfoDesk implements Runnable{

        int num;
        int stored;

        //--------------------------------------------------------------------------------------------------------------------//
        //info desk
        InfoDesk(int num){

            this.num = num;

        }

        //--------------------------------------------------------------------------------------------------------------------//
        //desk customer from info desk
        public void bigset(int i){

            this.stored = i;
        }

        //--------------------------------------------------------------------------------------------------------------------//
        //run
        public void run()
        {

            //waiting people array
            waitingarray[stored][0] = waitingnum;
            System.out.println("Customer " + (stored+1) + " gets number " + ((waitingarray[stored][0])+1) + ", enters waiting room");
            waitingnum++;


        }
        //--------------------------------------------------------------------------------------------------------------------//
    }
    //--------------------------------------------------------------------------------------------------------------------//
}

//--------------------------------------------------------------------------------------------------------------------//


