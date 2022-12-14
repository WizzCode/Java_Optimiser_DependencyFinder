package com.yourorganization.maven_sample;

import java.io.FileNotFoundException;
import java.util.*;

public class Dependency {
    public static void main(String[] args) throws FileNotFoundException {

        Dependency vdobj = new Dependency();
        vdobj.dependencyinput();
    }
    
    public void dependencyinput() throws FileNotFoundException{
        
        SymbolTableGenerator obj = new SymbolTableGenerator();
        obj.symbolsolverparsing();
        obj.attributes();
        
 
    }
}
