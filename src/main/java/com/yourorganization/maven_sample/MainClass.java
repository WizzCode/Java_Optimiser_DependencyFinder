package com.yourorganization.maven_sample;

import java.io.FileNotFoundException;


public class MainClass {
    public static void main(String[] args) throws FileNotFoundException, Exception{
        
        //Calling Dependencing Finder
//        Dependency d_obj = new Dependency(); 
//        d_obj.calldependency();
        
        //Calling Optimiser
        
        Optimiser o_obj = new Optimiser(); 
        o_obj.calloptimiser();
        
    }
}
