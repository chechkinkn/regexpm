package ru.chechkin.internal.scanner;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

/**
 * A thread‑safe scanner that tokenizes a regular expression pattern.
 * The token list is computed exactly once, at construction time.
 *
 * <p>Recognized tokens:
 * <ul>
 *   <li>Operators: '|' (PIPE), '+' (PLUS), '*' (STAR), '?' (QUESTION_MARK)</li>
 *   <li>Parentheses: '(' (LEFT_PAREN), ')' (RIGHT_PAREN)</li>
 *   <li>Alphanumeric strings (STRING)</li>
 *   <li>End of file (EOF)</li>
 * </ul>
 *
 * <p>Any other character causes a {@link ScannerException} to be thrown.
 */
public class RegexpScanner implements Scanner {

	private static final CharMatcher ALPHANUMERIC_MATCHER = CharMatcher.inRange('0', '9')
			.or(CharMatcher.inRange('a', 'z'))
			.or(CharMatcher.inRange('A', 'Z'));

	private final String pattern;
	private final List<Token> tokens;

	/**
	 * Constructs a scanner for the given pattern and immediately scans it.
	 *
	 * @param pattern the regular expression pattern to scan; must not be {@code null}
	 * @throws NullPointerException if {@code pattern} is {@code null}
	 * @throws ScannerException     if the pattern contains an unrecognized character
	 */
	public RegexpScanner(String pattern) {
		this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
		this.tokens = ImmutableList.copyOf(scanAllTokens());
	}

	/**
	 * Returns the list of tokens for the pattern.
	 * The result was computed at construction time and is immutable.
	 *
	 * @return an immutable list of tokens
	 */
	@Override
	public List<Token> getTokens() {
		return tokens;   // already immutable, no need for copying
	}

	private List<Token> scanAllTokens() {
		List<Token> tokens = new ArrayList<>();
		int current = 0;

		while (current < pattern.length()) {
			TokenAndIndex tokenAndInde = scanToken(current);
			tokens.add(tokenAndInde.token);
			current = tokenAndInde.nextIndex;
		}

		tokens.add(eofToken());
		return tokens;
	}


	private TokenAndIndex scanToken(int currentIndex) {
		char c = pattern.charAt(currentIndex);
		int nextIndex = currentIndex + 1;

		return switch (c) {
			case '|' -> new TokenAndIndex(
					Token.builder().type(Token.Type.SLASH).build(),
					nextIndex
			);
			case '+' -> new TokenAndIndex(
					Token.builder().type(Token.Type.PLUS).build(),
					nextIndex
			);
			case '*' -> new TokenAndIndex(
					Token.builder().type(Token.Type.STAR).build(),
					nextIndex
			);
			case '?' -> new TokenAndIndex(
					Token.builder().type(Token.Type.QUESTION_MARK).build(),
					nextIndex
			);
			case '(' -> new TokenAndIndex(
					Token.builder().type(Token.Type.LEFT_PAREN).build(),
					nextIndex
			);
			case ')' -> new TokenAndIndex(
					Token.builder().type(Token.Type.RIGHT_PAREN).build(),
					nextIndex
			);
			default -> {
				if (isAlphanumeric(c)) {
					yield scanString(currentIndex);
				}
				throw new ScannerException(
						"Unrecognized character: '" + c + "' at position " + currentIndex
				);
			}
		};
	}

	private TokenAndIndex scanString(int currentIndex) {
		int nextIndex = currentIndex + 1;
		while (nextIndex < pattern.length() && isAlphanumeric(pattern.charAt(nextIndex))) {
			nextIndex++;
		}
		String lexeme = pattern.substring(currentIndex, nextIndex);
		Token token = Token.builder()
				.type(Token.Type.STRING)
				.lexeme(lexeme)
				.build();
		return new TokenAndIndex(token, nextIndex);
	}

	private static Token eofToken() {
		return Token.builder()
				.type(Token.Type.EOF)
				.build();
	}

	private static boolean isAlphanumeric(char c) {
		return ALPHANUMERIC_MATCHER.matches(c);
	}

	private record TokenAndIndex(Token token, int nextIndex) {
	}

	public static class ScannerException extends RuntimeException {
		public ScannerException(String message) {
			super(message);
		}
	}
}
