public class OptimiserInput {
    public static void main(String[] args) {
        Table obj = new Table();//only one object
        MyThread1 t1=new MyThread1(obj);
        MyThread2 t2=new MyThread2(obj);
        t1.start();
        t2.start();

    }


    class Table
    {
        void printTable(int n){
            int i=1;

    while(i<=5){
 synchronized (this)
 {
     System.out.println(n);
     try{
         Thread.sleep(400);
     }
     catch(Exception e){System.out.println(e);}
     i++;
 }
    }









        }//end of the method
    }

    class MyThread1 extends Thread{
        Table t;
        MyThread1(Table t){
            this.t=t;
        }
        public void run(){
            t.printTable(5);
        }

    }
    class MyThread2 extends Thread{
        Table t;
        MyThread2(Table t){
            this.t=t;
        }
        public void run(){
            t.printTable(100);
        }
    }

}