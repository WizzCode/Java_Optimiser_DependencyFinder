
 public class beforeSynchronized
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
    }
}
