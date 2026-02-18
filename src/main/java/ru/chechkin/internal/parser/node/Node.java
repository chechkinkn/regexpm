package ru.chechkin.internal.parser.node;

import ru.chechkin.internal.parser.visitor.NodeVisitor;

public abstract class Node {
	public abstract <R, S> R accept(NodeVisitor<R, S> visitor, S state);
}
