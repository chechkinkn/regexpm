package ru.chechkin.internal.parser.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.chechkin.internal.parser.visitor.NodeVisitor;

import java.util.List;

@AllArgsConstructor
public class AlternNode extends Node {
	@Getter
	private List<Node> alternatives;

	@Override
	public <R, S> R accept(NodeVisitor<R, S> visitor, S state) {
		return visitor.visitAlternNode(this, state);
	}
}
