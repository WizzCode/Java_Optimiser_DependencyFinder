package com.yourorganization.maven_sample;

public class VariableDependency {
    public static void main(String[] args) {
        System.out.print("Hello");
        SymbolTableGenerator obj = new SymbolTableGenerator();
        obj.parseInputCode("SumOfNumbers1.java");
        obj.variableDependencyInput();
    }
}
