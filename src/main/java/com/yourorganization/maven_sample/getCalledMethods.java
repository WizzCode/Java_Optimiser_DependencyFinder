package com.yourorganization.maven_sample;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class getCalledMethods extends VoidVisitorAdapter<List<ObjectCreationExpr>> {
    @Override
    public void visit(ObjectCreationExpr n, List<ObjectCreationExpr> ocollect)
    {
        super.visit(n, ocollect);
        ocollect.add(n);
    }
}
