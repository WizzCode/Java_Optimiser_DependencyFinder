package com.yourorganization.maven_sample;

import java.io.FileNotFoundException;
import java.util.List;


public class MainClass {
    public static void main(String[] args) throws FileNotFoundException, Exception{
        
        //Calling Dependencing Finder
//        Dependency d_obj = new Dependency("src/main/resources/OptimiserInput.java");
//        d_obj.calldependency();
        
        //Calling Optimiser
        
        Optimiser o_obj = new Optimiser("src/main/resources/OptimiserInput.java");
        o_obj.calloptimiser();
        
    }
}
