import java.util.StringTokenizer;

public class OptimiserInput {
    public static void main(String[] args) {
          OptimiserInput obj = new  OptimiserInput();
                 
          obj.input_avoidMethodCalls();
          obj.input_AvoidEmptyIfStatement();
          obj.input_avoidBooleanIfComparison();
          obj.input_catchPrimitivesInConstructor();
          obj.input_avoidSynchronizedInLoop();
          obj.input_avoidStringConcatenationInLoop();
          obj.input_AvoidUsingStringTokenizer();
          obj.input_AvoidUsingNewWithString();
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
        System.out.println("Hello world!");
        String myStr;
        boolean abc;
        if(abc==true)
        {

        }
        if(yes(1)==true)
        {
            System.out.println("yep");
            //do something
        }
        List<Integer> myList = new ArrayList();
        if(myList.size()>1)
        {
            // do something
        }
        if(myList.isEmpty()==true)
        {
            //do something else
        }
        if(myStr.isEmpty()>1)
        {

        }
        if(myStr.isEmpty()==true)
        {

        }

    }

    static boolean yes(int choice)
    {
        if(choice==1)
        {
            return true;
        }
        return false;
    }


    public void input_catchPrimitivesInConstructor(){
      Integer i = new Integer(23);
      Character j = new Character('a');
    }
    
    
    public void input_avoidStringConcatenationInLoop(){
        String result = "";
        String[] strings = {"first","second","third"};
        int i;
		for (i = 0; i < strings.length; i++) 
		{
		 result = result + strings[0];	
                 result = "hello";
		}
                i =0;
                while(i<strings.length){
                    if(2>3){
                      result = "hello" + strings[0];	  
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

    public void input_AvoidUsingNewWithString()
    {
        // without typecasting
        String s = "123";
        String x = new String("123");

        // for typecasted stuff

        Integer i_int = Integer.valueOf("123");
        Integer c_int = Integer.valueOf(new String("123"));

    }

}