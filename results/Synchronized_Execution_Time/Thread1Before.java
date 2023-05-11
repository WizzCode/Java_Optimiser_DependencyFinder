public class Thread1Before extends Thread{
    beforeSynchronized t;
    Thread1Before(beforeSynchronized t){
        this.t=t;
    }
    public void run(){
        t.printTable(5);
    }

}