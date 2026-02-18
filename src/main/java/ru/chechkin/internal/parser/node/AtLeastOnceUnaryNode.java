package ru.chechkin.internal.parser.node;

import ru.chechkin.internal.parser.visitor.NodeVisitor;

public class AtLeastOnceUnaryNode extends UnaryNode {
	public AtLeastOnceUnaryNode(Node node) {
		super(node);
	}

	@Override
	public <R, S> R accept(NodeVisitor<R, S> visitor, S state) {
		return visitor.visitAtLeastOnceUnaryNode(this, state);
	}
}
