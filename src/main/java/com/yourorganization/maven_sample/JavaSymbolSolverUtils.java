package com.yourorganization.maven_sample;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.*;

public class JavaSymbolSolverUtils {
    //filePath should be of the form "src/main/resources/FileName.java"
    public CompilationUnit astUsingJavaSymbolSolver(String filePath) throws IOException{
        Path pathToSource = Paths.get(filePath);
        Reader fr = new FileReader(pathToSource.toString());
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        ParserConfiguration config = new ParserConfiguration().
                setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
        JavaParser parser = new JavaParser(config);
        CompilationUnit cu = parser.parse(
                ParseStart.COMPILATION_UNIT, new StreamProvider(fr)).
                getResult().get();

//        YamlPrinter printer = new YamlPrinter(true);
//        System.out.println(printer.output(cu));

        return cu;
    }

    public void getQualifiedNameMethodCall(CompilationUnit cu){
        ArrayList<String> qualifiedNames = new ArrayList<String>();
        cu.findAll(MethodCallExpr.class ).forEach(methodCall -> {
            ResolvedMethodDeclaration methodDeclaration = methodCall.resolve();
            System.out.println("Method: " + methodDeclaration.getQualifiedName());
            System.out.println("Method " + methodDeclaration.getQualifiedSignature());
            System.out.println("Method " + methodCall.calculateResolvedType());
            qualifiedNames.add(methodDeclaration.getQualifiedName());
        });
//        return qualifiedNames;
    }
}
