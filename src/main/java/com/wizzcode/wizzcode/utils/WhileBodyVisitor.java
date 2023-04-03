package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class WhileBodyVisitor extends VoidVisitorAdapter<List<Statement>>{
    @Override
 public void visit(WhileStmt whileStmt, List<Statement> collector) {
         
       collector.add(whileStmt.getBody());
        super.visit(whileStmt, collector);
        }
 


}
