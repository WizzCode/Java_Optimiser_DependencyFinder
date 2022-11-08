package com.yourorganization.maven_sample;

import java.util.*;
public class VariableDependency {
    public ArrayList<String> variable_array=new ArrayList<String>();
    public ArrayList<String> right=new ArrayList<String>();
    public HashMap<String,ArrayList<String>> dependence_dict = new HashMap<String,ArrayList<String>>();
    public static void main(String[] args) {
        
        VariableDependency vdobj = new VariableDependency();
        vdobj.vdInput();
        vdobj.variableDependencyAlgo();
        vdobj.variableDependencyMatrix();
    }
    
    public void vdInput(){
        SymbolTableGenerator obj = new SymbolTableGenerator();
        obj.parseInputCode("SumOfNumbers1.java");
        obj.variableDependencyInput();
        //        variable_array=obj.variableDependencyInput().variable_array;
//        right=obj.variableDependencyInput().right;
//        dependence_dict=obj.variableDependencyInput().dependence_dict;

//variable_array={x,b,c,a,y,z,p,r,q}
//right={b,c,x,a,y,p}
//dependence_dict={a:b,c,y:a,x,z:y,q:p}

          variable_array = new ArrayList<>(Arrays.asList("x","b","c","a","y","z","p","r","q"));
          right = new ArrayList<>(Arrays.asList("b","c","x","a","y","p"));
          ArrayList<String> value=new ArrayList<String>();
          value.add("b");
          value.add("c");
          dependence_dict.put("a",value);
          value=new ArrayList<String>();
          value.add("a");
          value.add("x");
          dependence_dict.put("y",value);
          value=new ArrayList<String>();
          value.add("y");
          dependence_dict.put("z",value);
          value=new ArrayList<String>();
          value.add("p");
          dependence_dict.put("q",value);
          
          
    }
    
    public void variableDependencyAlgo(){
        
        int n=variable_array.size();
        HashMap<String,String> variable_status = new HashMap<String,String>();
        
        System.out.println("\nDetermining Dependencies.......\n");
        System.out.println("Determining Status of Variables.......\n");
        System.out.println("Status: Description\n");
        System.out.println("Isolated: No variables depend on it and it has no interaction with other variables\n");
        System.out.println("Precedence: No variable depends on it but the variable itself depends on some variable\n");
        System.out.println("Intermediate: Has neither isolated nor precedence status\n");
        System.out.println("---------------------------------------------------------------------------------------------\n");
        for(int i=0;i<n;i++){
            String variable=variable_array.get(i);
            String status;
            if(!right.contains(variable)){
              if(!dependence_dict.containsKey(variable)) status = "Isolated";
              else status = "Precedence";
            }
            else status="Intermediate";
            variable_status.put(variable,status);
        }
        
        

        
        System.out.println("-----------------Variable Classification-----------------\n");
        
        Formatter fmt = new Formatter();  
        fmt.format("%15s %15s %21s\n", "Variable\t|", "Dependent on\t|", "Status of Variable"); 
        
        for (int i=0;i<n;i++) {  
            String variable=variable_array.get(i);
            
            List<String> dependencies = new ArrayList<String>();
            if(dependence_dict.containsKey(variable)) dependencies= dependence_dict.get(variable);
            String dependent_on;
            if(dependencies.isEmpty()) dependent_on="--";
            else dependent_on = String.join(",",dependencies );
            
            String status = variable_status.get(variable);
            
            
            fmt.format("%14s %13s %17s\n", variable +"\t|", dependent_on +"\t|",status );  
        }  
        
        System.out.println(fmt);  
    }
    
    public void variableDependencyMatrix(){
    }
}
