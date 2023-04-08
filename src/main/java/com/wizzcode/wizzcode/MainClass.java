package com.wizzcode.wizzcode;

import com.wizzcode.wizzcode.CodeDependencyFinder.Dependency;
import com.wizzcode.wizzcode.Optimisation.Optimiser;

import java.io.FileNotFoundException;


public class MainClass {
    public static void main(String[] args) throws FileNotFoundException, Exception{
        
        //Calling Dependencing Finder
        Dependency d_obj = new Dependency("src/main/resources/SampleProgramNew.java");
        d_obj.calldependency();
        
        //Calling Optimiser
        
//        Optimiser o_obj = new Optimiser("src/main/resources/OptimiserInput.java");
//        o_obj.calloptimiser();
        
    }
}
