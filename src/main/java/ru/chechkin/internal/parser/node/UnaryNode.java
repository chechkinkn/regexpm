package ru.chechkin.internal.parser.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class UnaryNode extends Node {
	@Getter
	private Node node;
}
