package com.wizzcode.wizzcode.utils;

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
