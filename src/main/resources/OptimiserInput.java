public class OptimiserInput {
    public static void main(String[] args) {
        //Code of Java for loop
        boolean a = false;
        int b = 24;
        if(yesMethod()==true&&(a==false)||((yesMethod()==false)&&(a==true)))
        {
            if(2<7){
                if(4<8){}
            }

            if(a==true){}
            if(a==true){}
        }

        String str = "Hello";

        while(2 < str.length())	/* VIOLATION*/
            while(2 < str.length()+1+str.charAt(0))	/* VIOLATION*/
            {
                System.out.println(2);
                int i=1;
                while(i<2){
                    i++;
                }

                while(2<str.length()){
                }

            }

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
                    while(i < str.length()){
                        System.out.println(i);
                    }
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