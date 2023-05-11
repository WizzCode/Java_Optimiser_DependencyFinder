public class Thread1After extends Thread{
    afterSynchronized t;
    Thread1After(afterSynchronized t){
        this.t=t;
    }
    public void run(){
        t.printTable(5);
    }

}