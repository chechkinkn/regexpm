package ru.chechkin.internal.parser.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.chechkin.internal.parser.visitor.NodeVisitor;

@AllArgsConstructor
public class StringNode extends Node {
	@Getter
	private final String lexeme;

	@Override
	public <R, S> R accept(NodeVisitor<R, S> visitor, S state) {
		return visitor.visitStringNode(this, state);
	}
}
