package com.yourorganization.maven_sample;

import java.io.FileNotFoundException;
import java.util.*;

public class Dependency {
    public ArrayList<String> attribute_array=new ArrayList<String>();
    public ArrayList<String> right=new ArrayList<String>();
    public HashMap<String,ArrayList<String>> dependence_dict = new HashMap<String,ArrayList<String>>();
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
