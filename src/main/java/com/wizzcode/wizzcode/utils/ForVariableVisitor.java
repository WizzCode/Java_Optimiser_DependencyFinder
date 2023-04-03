package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class ForVariableVisitor extends VoidVisitorAdapter<List<List<Expression>>>{
    @Override
 public void visit(ForStmt forStmt, List<List<Expression>> collector) {
      
        collector.add(forStmt.getUpdate());
        super.visit(forStmt, collector);
        }
 


}
