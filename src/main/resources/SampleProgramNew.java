public class SampleProgramNew extends Thread{
    public static void main(String args[]){
        A obj = new A();
        System.out.println(obj.abc);
        String a = "5"+obj.abc;
        obj.f1();
        obj.f2();
        int val1= 4;
        int val2 = obj.f3();
        int val3;
        val3 = 2+obj.f3();
        Programmer pobj = new Programmer();
        pobj.display();
        SampleProgramNew thread = new SampleProgramNew();
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

//Inheritance Example

class Employee{  
 float salary=40000;  
 void job(){

System.out.println ("this is an employee");

}
}  

class Programmer extends Employee{  
 int bonus=10000;  
 public void display(){  
   Programmer p=new Programmer();  
   System.out.println("Programmer salary is:"+p.salary);  
   System.out.println("Bonus of Programmer is:"+p.bonus);  
}  
  void job(){

System.out.println ("this is a programmer");

}
  
}