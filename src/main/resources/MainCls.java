public class MainCls extends Thread{
    public static void main(String args[]){
        A obj = new A();
        String a = "5"+obj.abc;
        obj.f1();
        obj.f2();
        int val1= 4;
        int val2 = obj.f3();
        int val3;
        val3 = 2+obj.f3();
        Square pobj = new Square();
        pobj.display();
        Adder aobj = new Adder();
        int a1 = 11;
        int a2 = 11;
        int add_int = aobj.add(a1,a2);
        double d1= 12.3;
        double d2 = 12.6;
        double add_doub = aobj.add(d1,d2);
        MainCls thread = new MainCls();
        thread.start();
        System.out.println("This code is outside of the thread");
    }
    public void run() {
        int a=9;
        System.out.println("This code is running in a thread");
    }
}

class A {
String abc = "class A variable";
int val2;
public void f1(){
    String abc;
    int val4 = val2+2;
    abc = "f1 function variable outside if";
    if (true){
          abc = "f1 function variable modified inside if";
    }
    abc="new";
}

public void f2(){
    String abc = "f2 function variable";
}

public int f3(){
     int val2=2;
     val2=val2+2;
     return val2;
}


}

class Shape{  
 float salary=40000;  
 void set(){

System.out.println ("this is a shape");

}
}  

class Square extends Shape{  
 int bonus=10000;  
 public void display(){  
   Square p=new Square();  
   System.out.println("Square salary is:"+p.salary);  
   System.out.println("Bonus of Square is:"+p.bonus);  
}  
  void set(){

System.out.println ("this is a square");
//Variable overriding
}
  
}

class Adder{  
public int add(int a, int b){
    return a+b;
}  
public double add(double a, double b){
    return a+b;
}  
}  



