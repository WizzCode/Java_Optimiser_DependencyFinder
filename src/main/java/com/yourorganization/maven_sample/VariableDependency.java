package com.yourorganization.maven_sample;

import java.util.Arrays;

public class VariableDependency {
    public static void main(String[] args) {
        System.out.print("Hello");
        SymbolTableGenerator obj = new SymbolTableGenerator();
        obj.parseInputCode("SampleProgram.java");
      VariableAttributes obj2=  obj.variableDependencyInput();
      System.out.println("list of all variables ");
        for (int i = 0; i < obj2.variable_array.size();i++)
        {
            System.out.println(obj2.variable_array.get(i));
        }

        System.out.println("dependence dict ");
        System.out.println(Arrays.asList(obj2.dependence_dict));
    }
}
