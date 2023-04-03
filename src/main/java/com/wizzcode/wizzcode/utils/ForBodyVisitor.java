package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class ForBodyVisitor extends VoidVisitorAdapter<List<Statement>>{
    @Override
 public void visit(ForStmt forStmt, List<Statement> collector) {
      
        collector.add(forStmt.getBody());
        super.visit(forStmt, collector);
        }

}
