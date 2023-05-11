public class Thread2After {
    afterSynchronized t;
    Thread2After(afterSynchronized t){
        this.t=t;
    }
    public void run(){
        t.printTable(100);
    }
}
