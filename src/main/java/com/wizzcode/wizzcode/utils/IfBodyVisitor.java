package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class IfBodyVisitor extends VoidVisitorAdapter<List<Statement>>{
    
    
    
    @Override
 public void visit(IfStmt ifStmt, List<Statement> collector) {
         
        
        collector.add(ifStmt.getThenStmt());
     
        super.visit(ifStmt, collector);

               }
}
