package com.wizzcode.wizzcode.utils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.*;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedType;

public class JavaParserUtils {
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

    public String getQualifiedName(Node node){
        if(node instanceof NameExpr){
            ResolvedValueDeclaration rvd = ((NameExpr) node).resolve();
            if(rvd.isField()){
                ResolvedFieldDeclaration rfd = rvd.asField();
                Optional<FieldDeclaration> fdec = rfd.toAst();
                ClassOrInterfaceDeclaration ciDec = (ClassOrInterfaceDeclaration) fdec.get().getParentNode().get();
                String nodeQualifiedName = ciDec.getNameAsString() + "." + ((NameExpr) node).getNameAsString();
                return nodeQualifiedName;
            }
            return ((NameExpr) node).getNameAsString();
            
        }

        else if(node instanceof MethodDeclaration){
            ResolvedMethodDeclaration resolvedMethod = ((MethodDeclaration) node).resolve();
            String qualifiedName = resolvedMethod.getQualifiedSignature();
            return qualifiedName;
        }

        else if(node instanceof MethodCallExpr){
            ResolvedMethodDeclaration methodDeclaration = ((MethodCallExpr) node).resolve();
            return methodDeclaration.getQualifiedName();
        }
        
        else if(node instanceof FieldAccessExpr){
            ResolvedType resolvedType = ((FieldAccessExpr)node).getScope().calculateResolvedType();
            String nodeQualifiedName = resolvedType.describe() + "." + ((FieldAccessExpr) node).getNameAsString();
            return nodeQualifiedName;
            
        }

        else if(node instanceof ObjectCreationExpr){
            System.out.println("OBJECT CREATION:"+((ObjectCreationExpr) node).getType().toString());
            return ((ObjectCreationExpr) node).getType().toString();
        }

        return "";
    }

}
