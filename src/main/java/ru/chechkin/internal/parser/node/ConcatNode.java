package ru.chechkin.internal.parser.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.chechkin.internal.parser.visitor.NodeVisitor;

import java.util.List;

@AllArgsConstructor
public class ConcatNode extends Node {
	@Getter
	private List<Node> concatNodes;

	@Override
	public <R, S> R accept(NodeVisitor<R, S> visitor, S state) {
		return visitor.visitConcatNode(this, state);
	}
}
