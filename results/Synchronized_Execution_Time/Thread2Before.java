public class Thread2Before {
    beforeSynchronized t;
    Thread2Before(beforeSynchronized t){
        this.t=t;
    }
    public void run(){
        t.printTable(100);
    }
}
