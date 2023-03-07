public class OptimiserInput {  
public static void main(String[] args) {  
    //Code of Java for loop  
    boolean a = false;
    int b = 24;
    if(yesMethod()==true)
    {
         if(2<7){
             if(4<8){}
         }
         
         if(a==true){}
    }
    
    String str = "Hello";
   
		for (int i = 0; i < str.length(); i++)		/* VIOLATION*/
	  	{
	  	    System.out.println(i);
	  	    
			
		}
                
    if(7>2){
        for (int i = 0; i < str.length(); i++)		/* VIOLATION*/
	  	{
	  	    System.out.println(i);
	  	    for(int j = 0; j<i; i++){
                        System.out.println(i);
                    }
                    if(2>3)
                    {
                    }
			
		}
       
    }
    
}

public static boolean yesMethod()
{
return true;
}
}  