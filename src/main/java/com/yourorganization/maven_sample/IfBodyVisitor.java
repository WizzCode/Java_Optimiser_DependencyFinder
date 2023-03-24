package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class IfBodyVisitor extends VoidVisitorAdapter<List<Statement>>{
    
    
    
    @Override
 public void visit(IfStmt ifStmt, List<Statement> collector) {
         
        
        collector.add(ifStmt.getThenStmt());
     
        super.visit(ifStmt, collector);

               }
}
