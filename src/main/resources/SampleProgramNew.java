public class SampleProgramNew{
    public static void main(String args[]){
        A obj = new A();
        System.out.println(obj.abc);
        obj.f1();
        obj.f2();
        int val1= 4;
        int val2 = obj.f3(val1);
        System.out.println(val2);
        obj.f4();
        System.out.println(val2);
    }
}

class A {
String abc="class A variable";
int val2=3;
public void f1(){
    String abc;
    abc = "f1 function variable outside if";
    System.out.println(abc);   
    if (true){
          abc = "f1 function variable modified inside if";
    }
    abc="new";
    System.out.println(abc); 
}

public void f2(){
    String abc = "f2 function variable";
    System.out.println(abc);   
}

public int f3(int val1){
     int val2;
     val2=val1 +2;
     return val2;
}
public void f4(){
     val2=val2+2;
     
}

}
