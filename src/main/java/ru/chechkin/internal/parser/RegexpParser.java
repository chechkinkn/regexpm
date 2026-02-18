package ru.chechkin.internal.parser;

import ru.chechkin.internal.parser.node.AlternNode;
import ru.chechkin.internal.parser.node.AtLeastOnceUnaryNode;
import ru.chechkin.internal.parser.node.AtMostOnceUnaryNode;
import ru.chechkin.internal.parser.node.ConcatNode;
import ru.chechkin.internal.parser.node.Node;
import ru.chechkin.internal.parser.node.RepeatableUnaryNode;
import ru.chechkin.internal.parser.node.StringNode;
import ru.chechkin.internal.scanner.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * program   → regexp EOF
 * regexp    → altern
 * altern    → concat (PIPE  concat )*
 * concat    → unary+
 * unary     → atom ( STAR | PLUS | QUESTION_MARK )?
 * atom      → STRING | LEFT_PAREN regexp RIGHT_PAREN
 */
public class RegexpParser implements Parser {
	private final List<Token> tokens;
	private final Node root;

	private int current = 0;

	public RegexpParser(List<Token> tokens) {
		this.tokens = tokens;
		this.root = parseInternal();
	}

	@Override
	public Node parse() {
		return root;
	}

	private Node parseInternal() {
		return regexp();
	}

	private Node regexp() {
		return altern();
	}

	private Node altern() {
		Node node = concat();

		List<Node> alternativeNodes = new ArrayList<>();

		while (match(Token.Type.SLASH)) {
			advance();
			alternativeNodes.add(concat());
		}

		if (!alternativeNodes.isEmpty()) {
			alternativeNodes.add(0, node);
			node = new AlternNode(alternativeNodes);
		}

		return node;
	}

	private Node concat() {
		Node node = unary();

		List<Node> concatNodes = new ArrayList<>();

		while (!isEof() && !isAlternative() && !isRightParen()) {
			concatNodes.add(unary());
		}

		if (!concatNodes.isEmpty()) {
			concatNodes.add(0, node);
			node = new ConcatNode(concatNodes);
		}

		return node;
	}

	private boolean isAlternative() {
		return peek().getType() == Token.Type.SLASH;
	}

	private boolean isRightParen() {
		return peek().getType() == Token.Type.RIGHT_PAREN;
	}

	private Node unary() {
		Node node = atom();

		Map<Token.Type, Function<Node, Node>> constructorByTokenType = Map.of(
				Token.Type.PLUS, AtLeastOnceUnaryNode::new,
				Token.Type.QUESTION_MARK, AtMostOnceUnaryNode::new,
				Token.Type.STAR, RepeatableUnaryNode::new
		);

		Token.Type[] requiredTypes = constructorByTokenType.keySet().toArray(Token.Type[]::new);

		if (anyMatch(requiredTypes)) {
			Token operator = advance();

			node = constructorByTokenType.get(operator.getType())
					.apply(node);
		}

		return node;
	}

	private Node atom() {
		Token token = advance();

		if (token.getType() == Token.Type.STRING) {
			return new StringNode(token.getLexeme());
		}

		if (token.getType() == Token.Type.LEFT_PAREN) {
			Node regexp = regexp();

			consume(Token.Type.RIGHT_PAREN, "Expected closing parenthesis ')'");

			return regexp;
		}

		throw new IllegalStateException("Unexpected token: " + token);
	}

	private void consume(Token.Type type, String errorMsg) {
		if (isEof()) {
			throw new IllegalStateException(errorMsg);
		}

		Token token = advance();

		if (token.getType() == type) {
			return;
		}

		throw new IllegalStateException(errorMsg);
	}

	private Token advance() {
		return tokens.get(current++);
	}

	private boolean anyMatch(Token.Type... types) {
		return Arrays.stream(types).anyMatch(this::match);
	}

	private boolean match(Token.Type type) {
		return !isEof() && peek().getType() == type;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean isEof() {
		return tokens.get(current).getType() == Token.Type.EOF;
	}
}
