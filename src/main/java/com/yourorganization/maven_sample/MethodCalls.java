package com.yourorganization.maven_sample;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class MethodCalls extends VoidVisitorAdapter<List<MethodCallExpr>> {
    @Override
    public void visit(MethodCallExpr n, List<MethodCallExpr> collector)
    {
        super.visit(n, collector);
        collector.add(n);
    }
}
