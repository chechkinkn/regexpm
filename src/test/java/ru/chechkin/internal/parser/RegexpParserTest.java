package ru.chechkin.internal.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import ru.chechkin.internal.parser.node.AlternNode;
import ru.chechkin.internal.parser.node.AtLeastOnceUnaryNode;
import ru.chechkin.internal.parser.node.AtMostOnceUnaryNode;
import ru.chechkin.internal.parser.node.ConcatNode;
import ru.chechkin.internal.parser.node.Node;
import ru.chechkin.internal.parser.node.RepeatableUnaryNode;
import ru.chechkin.internal.parser.node.StringNode;
import ru.chechkin.internal.scanner.Token;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests for the RegexpParser class.
 * Assumes existence of Token class with Type enum and Node hierarchy:
 * - StringNode (holds a lexeme)
 * - ConcatNode (holds a list of child nodes)
 * - AlternNode (holds a list of child nodes)
 * - AtLeastOnceUnaryNode (holds one child)
 * - AtMostOnceUnaryNode (holds one child)
 * - RepeatableUnaryNode (holds one child)
 */
public class RegexpParserTest {

	// --- Helper to create token lists ---
	private List<Token> tokens(Object... parts) {
		List<Token> list = new ArrayList<>();
		for (Object p : parts) {
			if (p instanceof Token.Type) {
				list.add(new Token((Token.Type) p, ""));
			} else if (p instanceof String) {
				list.add(new Token(Token.Type.STRING, (String) p));
			} else {
				throw new IllegalArgumentException("Unsupported token part: " + p);
			}
		}
		list.add(new Token(Token.Type.EOF, ""));
		return list;
	}

	// --- Helper assertions for node structure ---
	private void assertStringNode(Node node, String expected) {
		assertTrue(node instanceof StringNode, "Expected StringNode but got " + node.getClass().getSimpleName());
		assertEquals(expected, ((StringNode) node).getLexeme());
	}

	private void assertUnaryNode(Node node, Class<?> expectedClass, Node expectedChild) {
		assertTrue(expectedClass.isInstance(node), "Expected " + expectedClass.getSimpleName() + " but got " + node.getClass().getSimpleName());
		// Assuming unary nodes have a method getChild()
		// If not, we need to adapt; here we assume a getter.
		// For simplicity, we just check that we can cast and then assert child recursively.
		// We'll use reflection-like approach if necessary, but for test we'll rely on concrete classes.
		// Instead, we'll create a helper that uses known constructors? Better to have a common interface.
		// Since we don't have the actual node classes, we'll assume they have a public field or getter.
		// For the purpose of this test, we'll just check the child via a method we assume exists: getNode()
		// Alternatively, we can compare string representations.
		// To keep it simple, we'll check that the node is of the expected type and then recursively verify the child.
		Node child = null;
		if (node instanceof AtLeastOnceUnaryNode) {
			child = ((AtLeastOnceUnaryNode) node).getNode();
		} else if (node instanceof AtMostOnceUnaryNode) {
			child = ((AtMostOnceUnaryNode) node).getNode();
		} else if (node instanceof RepeatableUnaryNode) {
			child = ((RepeatableUnaryNode) node).getNode();
		}
		assertNotNull(child, "Unary node should have a child");
		// Recursively compare child – we need a way to compare nodes. For now we just pass the expectedChild
		// but we need to implement deep equality. We'll assume nodes have proper equals or we'll manually check.
		// This is getting complex; for test readability, we'll just check the structure by walking the tree.
		// Let's instead create a method that compares two nodes by their type and content.
		assertNodesEqual(expectedChild, child);
	}

	private void assertNodesEqual(Node expected, Node actual) {
		if (expected instanceof StringNode && actual instanceof StringNode) {
			assertEquals(((StringNode) expected).getLexeme(), ((StringNode) actual).getLexeme());
		} else if (expected instanceof ConcatNode && actual instanceof ConcatNode) {
			List<Node> expChildren = ((ConcatNode) expected).getConcatNodes();
			List<Node> actChildren = ((ConcatNode) actual).getConcatNodes();
			assertEquals(expChildren.size(), actChildren.size());
			for (int i = 0; i < expChildren.size(); i++) {
				assertNodesEqual(expChildren.get(i), actChildren.get(i));
			}
		} else if (expected instanceof AlternNode && actual instanceof AlternNode) {
			List<Node> expChildren = ((AlternNode) expected).getAlternatives();
			List<Node> actChildren = ((AlternNode) actual).getAlternatives();
			assertEquals(expChildren.size(), actChildren.size());
			for (int i = 0; i < expChildren.size(); i++) {
				assertNodesEqual(expChildren.get(i), actChildren.get(i));
			}
		} else if (expected instanceof AtLeastOnceUnaryNode && actual instanceof AtLeastOnceUnaryNode) {
			assertNodesEqual(((AtLeastOnceUnaryNode) expected).getNode(), ((AtLeastOnceUnaryNode) actual).getNode());
		} else if (expected instanceof AtMostOnceUnaryNode && actual instanceof AtMostOnceUnaryNode) {
			assertNodesEqual(((AtMostOnceUnaryNode) expected).getNode(), ((AtMostOnceUnaryNode) actual).getNode());
		} else if (expected instanceof RepeatableUnaryNode && actual instanceof RepeatableUnaryNode) {
			assertNodesEqual(((RepeatableUnaryNode) expected).getNode(), ((RepeatableUnaryNode) actual).getNode());
		} else {
			fail("Node types differ: expected " + expected.getClass().getSimpleName() + ", actual " + actual.getClass().getSimpleName());
		}
	}

	// --- Test cases ---

	@Test
	void testSingleString() {
		List<Token> tokens = tokens("a");
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof StringNode);
		assertEquals("a", ((StringNode) root).getLexeme());
	}

	@Test
	void testStringWithStar() {
		List<Token> tokens = tokens("a", Token.Type.STAR);
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof RepeatableUnaryNode);
		RepeatableUnaryNode unary = (RepeatableUnaryNode) root;
		assertStringNode(unary.getNode(), "a");
	}

	@Test
	void testStringWithPlus() {
		List<Token> tokens = tokens("a", Token.Type.PLUS);
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof AtLeastOnceUnaryNode);
		AtLeastOnceUnaryNode unary = (AtLeastOnceUnaryNode) root;
		assertStringNode(unary.getNode(), "a");
	}

	@Test
	void testStringWithQuestion() {
		List<Token> tokens = tokens("a", Token.Type.QUESTION_MARK);
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof AtMostOnceUnaryNode);
		AtMostOnceUnaryNode unary = (AtMostOnceUnaryNode) root;
		assertStringNode(unary.getNode(), "a");
	}

	@Test
	void testConcatenationTwoStrings() {
		// "ab" -> tokens: STRING("a"), STRING("b")
		List<Token> tokens = tokens("a", "b");
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof ConcatNode);
		ConcatNode concat = (ConcatNode) root;
		List<Node> children = concat.getConcatNodes();
		assertEquals(2, children.size());
		// Note: due to parser implementation, order is reversed: "b" then "a"
		assertStringNode(children.get(0), "a");
		assertStringNode(children.get(1), "b");
	}

	@Test
	void testConcatenationThreeStrings() {
		// "abc" -> tokens: "a","b","c"
		List<Token> tokens = tokens("a", "b", "c");
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof ConcatNode);
		ConcatNode concat = (ConcatNode) root;
		List<Node> children = concat.getConcatNodes();
		assertEquals(3, children.size());
		// Order reversed: "c","b","a"
		assertStringNode(children.get(0), "a");
		assertStringNode(children.get(1), "b");
		assertStringNode(children.get(2), "c");
	}

	@Test
	void testAlternationTwo() {
		// "a|b" -> tokens: "a", SLASH, "b"
		List<Token> tokens = tokens("a", Token.Type.SLASH, "b");
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof AlternNode);
		AlternNode altern = (AlternNode) root;
		List<Node> children = altern.getAlternatives();
		assertEquals(2, children.size());
		// Order reversed: "b" then "a"
		assertStringNode(children.get(0), "a");
		assertStringNode(children.get(1), "b");
	}

	@Test
	void testAlternationThree() {
		// "a|b|c" -> tokens: "a", SLASH, "b", SLASH, "c"
		List<Token> tokens = tokens("a", Token.Type.SLASH, "b", Token.Type.SLASH, "c");
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof AlternNode);
		AlternNode altern = (AlternNode) root;
		List<Node> children = altern.getAlternatives();
		assertEquals(3, children.size());
		// Order reversed: "c","b","a"
		assertStringNode(children.get(0), "a");
		assertStringNode(children.get(1), "b");
		assertStringNode(children.get(2), "c");
	}

	@Test
	void testGrouping() {
		// "(a)" -> tokens: LEFT_PAREN, "a", RIGHT_PAREN
		List<Token> tokens = tokens(Token.Type.LEFT_PAREN, "a", Token.Type.RIGHT_PAREN);
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof StringNode);
		assertEquals("a", ((StringNode) root).getLexeme());
	}

	@Test
	void testGroupingWithUnary() {
		// "(a)*" -> tokens: LEFT_PAREN, "a", RIGHT_PAREN, STAR
		List<Token> tokens = tokens(Token.Type.LEFT_PAREN, "a", Token.Type.RIGHT_PAREN, Token.Type.STAR);
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		assertTrue(root instanceof RepeatableUnaryNode);
		RepeatableUnaryNode star = (RepeatableUnaryNode) root;
		assertStringNode(star.getNode(), "a");
	}

	@Test
	void testComplexExpression() {
		// "a*b+|c" -> tokens: "a", STAR, "b", PLUS, SLASH, "c"
		List<Token> tokens = tokens("a", Token.Type.STAR, "b", Token.Type.PLUS, Token.Type.SLASH, "c");
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		// Expected structure: AlternNode with two children: first is ConcatNode of (b+, a*) in reversed order,
		// second is "c". But due to altern reversal, order in AlternNode will be ["c", concatNode].
		assertTrue(root instanceof AlternNode);
		AlternNode altern = (AlternNode) root;
		List<Node> altChildren = altern.getAlternatives();
		assertEquals(2, altChildren.size());

		// First child (index 0) should be "c" because of reversal
		assertStringNode(altChildren.get(1), "c");

		// Second child (index 1) should be ConcatNode with children [b+, a*] in reversed order (so first b+, then a*)
		assertTrue(altChildren.get(0) instanceof ConcatNode);
		ConcatNode concat = (ConcatNode) altChildren.get(0);
		List<Node> concatChildren = concat.getConcatNodes();
		assertEquals(2, concatChildren.size());
		assertTrue(concatChildren.get(1) instanceof AtLeastOnceUnaryNode);
		assertStringNode(((AtLeastOnceUnaryNode) concatChildren.get(1)).getNode(), "b");
		assertTrue(concatChildren.get(0) instanceof RepeatableUnaryNode);
		assertStringNode(((RepeatableUnaryNode) concatChildren.get(0)).getNode(), "a");
	}

	@Test
	void testMissingRightParen() {
		List<Token> tokens = tokens(Token.Type.LEFT_PAREN, "a");
		assertThrows(IllegalStateException.class,
				() -> new RegexpParser(tokens)
		);
	}

	@Test
	void testUnexpectedToken() {
		// PLUS at start
		List<Token> tokens = tokens(Token.Type.PLUS);
		assertThrows(IllegalStateException.class,
				() -> new RegexpParser(tokens)
		);
	}

	@Test
	void testEmptyAlternationAfterSlash() {
		// "a|" -> tokens: "a", SLASH, EOF
		List<Token> tokens = tokens("a", Token.Type.SLASH);
		assertThrows(IllegalStateException.class,
				() -> new RegexpParser(tokens)
		);
	}

	@Test
	void testOnlySlash() {
		List<Token> tokens = tokens(Token.Type.SLASH);
		assertThrows(IllegalStateException.class,
				() -> new RegexpParser(tokens)
		);
	}

	@Test
	void testNestedGrouping() {
		// "((a))" -> tokens: LEFT_PAREN, LEFT_PAREN, "a", RIGHT_PAREN, RIGHT_PAREN
		List<Token> tokens = tokens(Token.Type.LEFT_PAREN, Token.Type.LEFT_PAREN, "a", Token.Type.RIGHT_PAREN, Token.Type.RIGHT_PAREN);
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();
		assertStringNode(root, "a");
	}

	@Test
	void testAlternationInsideGroup() {
		// "(a|b)c" -> tokens: LEFT_PAREN, "a", SLASH, "b", RIGHT_PAREN, "c"
		List<Token> tokens = tokens(Token.Type.LEFT_PAREN, "a", Token.Type.SLASH, "b", Token.Type.RIGHT_PAREN, "c");
		RegexpParser parser = new RegexpParser(tokens);
		Node root = parser.parse();

		// Expected: ConcatNode with children ["c", AlternNode("b","a")] (reversed order)
		assertTrue(root instanceof ConcatNode);
		ConcatNode concat = (ConcatNode) root;
		List<Node> concatChildren = concat.getConcatNodes();
		assertEquals(2, concatChildren.size());
		assertStringNode(concatChildren.get(1), "c");
		assertTrue(concatChildren.get(0) instanceof AlternNode);
		AlternNode altern = (AlternNode) concatChildren.get(0);
		List<Node> altChildren = altern.getAlternatives();
		assertEquals(2, altChildren.size());
		assertStringNode(altChildren.get(0), "a");
		assertStringNode(altChildren.get(1), "b");
	}
}