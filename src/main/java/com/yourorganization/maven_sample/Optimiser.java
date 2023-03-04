package com.yourorganization.maven_sample;

import java.io.FileNotFoundException;

//This class contains different methods for optimisation.

public class Optimiser {
    SymbolTableGenerator obj = new SymbolTableGenerator();
    
    //This method is called in the Main class which has the main method and is the entry point of the project
    public void calloptimiser() throws FileNotFoundException, Exception{
        System.out.println("Calling Optimiser");
        obj.symbolsolverparsing();
        System.out.println(obj.compilationUnit); //Access compilation unit used for parsing
        
        //Different methods are called here 
        avoidMethodCalls();   
    }
    
   public void avoidMethodCalls(){
      System.out.println("This method is used for removing unnecessary method calls inside loops");
   }
   
   //Other methods of optimisation
}


