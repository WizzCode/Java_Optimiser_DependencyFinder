import java.util.StringTokenizer;

public class OptimiserInput {
    public static void main(String[] args) {
          OptimiserInput obj = new  OptimiserInput();
                 
          obj.input_avoidMethodCalls();
          obj.input_AvoidEmptyIfStatement();
          obj.input_avoidBooleanIfComparison();
          obj.input_catchPrimitivesInConstructor();
          obj.input_loopInvariantCodeMotion();
          obj.input_avoidSynchronizedInLoop();
          obj.input_avoidStringConcatenationInLoop();
          obj.input_AvoidUsingStringTokenizer();
    }

    public void input_avoidMethodCalls(){
        String str = "Hello";
        int i;
                while(str.isEmpty()){
                
		while(str.charAt(0)+str.charAt(1)=='y')	
	  	{
	  	    System.out.println(2);
                    		
		}
                
                for (i = 0; i < str.length(); i++)		
	  	{
	  	     	 System.out.println(2);
		}
                }
        
    }
    
    public void input_AvoidEmptyIfStatement(){
        if(4!=4){}
        if(2<7){
             if(4<8){}
         }    
    }
    
    public void input_avoidBooleanIfComparison(){
        boolean a = false;
        int b = 24;
        if(yesMethod()==true) System.out.println("True");
    
        boolean c = true;

        if((((yesMethod()==true)&&(c==false)))||(c==true)) System.out.println("True");
      
    }
    
    public static boolean yesMethod() {return true;}
    
    public void input_catchPrimitivesInConstructor(){
      Integer i = new Integer(23);
      Character j = new Character('a');
    }
    
    public void input_loopInvariantCodeMotion(){
        int i = 0;
        int[] a={1,2,3,4};
        int x = 2;
        int y =-7;
        
        for(i = 0; i < a.length; i++)
		{
			a[i] = x * Math.abs(y);		// VIOLATION
		}
        
        i=0;
        int n = 3;
        int z = 2;
        
        while (i < n) {
        x = y + z;
        a[i] = 6 * i + x * x;
        ++i;
        }
    }
    
    public void input_avoidStringConcatenationInLoop(){
        String result = "";
        String[] strings = {"first","second","third"};
        int i;
		for (i = 0; i < strings.length; i++) 
		{
		 result = result + strings[i];	
                 result = "hello";
		}
                i =0;
                while(i<strings.length){
                    if(2>3){
                      result = "hello" + strings[i];	  
                    }
                    i++;
                }
    }
          
    public void input_avoidSynchronizedInLoop(){
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

    public void input_AvoidUsingStringTokenizer()
    {
        String str = "This is a method that uses string tokenizer";
        StringTokenizer myTokens = new StringTokenizer(str);
        while(myTokens.hasMoreTokens())
        {
            System.out.println(myTokens.nextToken);
        }

    }

}