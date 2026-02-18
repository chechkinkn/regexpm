package ru.chechkin.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.chechkin.internal.parser.node.AlternNode;
import ru.chechkin.internal.parser.node.AtLeastOnceUnaryNode;
import ru.chechkin.internal.parser.node.AtMostOnceUnaryNode;
import ru.chechkin.internal.parser.node.ConcatNode;
import ru.chechkin.internal.parser.node.Node;
import ru.chechkin.internal.parser.node.RepeatableUnaryNode;
import ru.chechkin.internal.parser.node.StringNode;
import ru.chechkin.internal.parser.visitor.NodeVisitor;

import java.util.Objects;

public class RegexpMatcher implements Matcher, NodeVisitor<Boolean, RegexpMatcher.RegexpMatchingContext> {

	@AllArgsConstructor
	public static class RegexpMatchingContext {
		@Getter
		private final String text;
		@Getter
		private int currentIndex;
	}

	public final Node node;

	public RegexpMatcher(Node node) {
		this.node = node;
	}


	@Override
	public boolean match(String text) {
		if (text == null) {
			return false;
		}

		RegexpMatchingContext ctx = new RegexpMatchingContext(text, 0);

		return match(node, ctx) && ctx.currentIndex >= ctx.text.length();
	}

	public boolean match(Node node, RegexpMatchingContext context) {
		return node.accept(this, context);
	}

	@Override
	public Boolean visitAlternNode(AlternNode alternNode, RegexpMatchingContext state) {
		int maxLength = -1;

		boolean isAnyMatch = false;

		int prevIndex = state.currentIndex;

		for (Node alternative: alternNode.getAlternatives()) {
			boolean isMatch = match(alternative, state);

			isAnyMatch = isAnyMatch || isMatch;

			if (isMatch) {
				maxLength = Math.max(maxLength, state.currentIndex);
			}

			state.currentIndex = prevIndex;
		}

		if (isAnyMatch) {
			state.currentIndex = maxLength;
		}

		return isAnyMatch;
	}

	@Override
	public Boolean visitConcatNode(ConcatNode concatNode, RegexpMatchingContext state) {
		return concatNode.getConcatNodes().stream()
				.allMatch(concat -> match(concat, state));
	}

	@Override
	public Boolean visitStringNode(StringNode stringNode, RegexpMatchingContext state) {
		boolean match = true;

		String lexeme = stringNode.getLexeme();

		for (int idx = 0; idx < lexeme.length(); idx++) {
			if (state.currentIndex + idx >= state.text.length()) {
				match = false;
				break;
			}
			match = Objects.equals(lexeme.charAt(idx), state.text.charAt(state.currentIndex + idx));
		}

		state.currentIndex += lexeme.length();

		return match;
	}

	@Override
	public Boolean visitAtMostOnceUnaryNode(AtMostOnceUnaryNode atMostOnceUnaryNode, RegexpMatchingContext state) {
		Node node = atMostOnceUnaryNode.getNode();

		int prevIndex = state.currentIndex;

		if (match(node, state)) {
			prevIndex = state.currentIndex;
		}

		state.currentIndex = prevIndex;
		return true;
	}

	@Override
	public Boolean visitRepeatableUnaryNode(RepeatableUnaryNode repeatableUnaryNode, RegexpMatchingContext state) {
		int prevIndex = state.currentIndex;

		Node node = repeatableUnaryNode.getNode();

		while (match(node, state)) {
			prevIndex = state.currentIndex;
		}

		state.currentIndex = prevIndex;
		return true;
	}

	@Override
	public Boolean visitAtLeastOnceUnaryNode(AtLeastOnceUnaryNode atLeastOnceUnaryNode, RegexpMatchingContext state) {
		Node node = atLeastOnceUnaryNode.getNode();

		if (!match(node, state)) {
			return false;
		}

		int prevIndex = state.currentIndex;

		while (match(node, state)) {
			prevIndex = state.currentIndex;
		}

		state.currentIndex = prevIndex;
		return true;
	}

}
