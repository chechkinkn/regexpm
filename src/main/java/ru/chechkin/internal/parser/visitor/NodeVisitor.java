package ru.chechkin.internal.parser.visitor;

import ru.chechkin.internal.parser.node.AlternNode;
import ru.chechkin.internal.parser.node.AtLeastOnceUnaryNode;
import ru.chechkin.internal.parser.node.AtMostOnceUnaryNode;
import ru.chechkin.internal.parser.node.ConcatNode;
import ru.chechkin.internal.parser.node.RepeatableUnaryNode;
import ru.chechkin.internal.parser.node.StringNode;

public interface NodeVisitor<R, S> {
	R visitAlternNode(AlternNode alternNode, S state);

	R visitConcatNode(ConcatNode concatNode, S state);

	R visitStringNode(StringNode stringNode, S state);

	R visitAtMostOnceUnaryNode(AtMostOnceUnaryNode atMostOnceUnaryNode, S state);

	R visitRepeatableUnaryNode(RepeatableUnaryNode repeatableUnaryNode, S state);

	R visitAtLeastOnceUnaryNode(AtLeastOnceUnaryNode atLeastOnceUnaryNode, S state);
}
