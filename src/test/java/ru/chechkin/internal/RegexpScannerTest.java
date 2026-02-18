package ru.chechkin.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.chechkin.internal.scanner.RegexpScanner;
import ru.chechkin.internal.scanner.Token;

import java.util.List;
import java.util.stream.Stream;

class RegexpScannerTest {

	@ParameterizedTest
	@MethodSource("provideTestData")
	void getTokens(String pattern, List<Token> expectedTokens) {
		Assertions.assertEquals(
				expectedTokens,
				new RegexpScanner(pattern).getTokens()
		);
	}

	static Stream<Arguments> provideTestData() {
		return Stream.of(
				Arguments.of("(abc)*a?qwerty+", List.of(
						Token.builder()
								.type(Token.Type.LEFT_PAREN)
								.build(),
						Token.builder()
								.type(Token.Type.STRING)
								.lexeme("abc")
								.build(),
						Token.builder()
								.type(Token.Type.RIGHT_PAREN)
								.build(),
						Token.builder()
								.type(Token.Type.STAR)
								.build(),
						Token.builder()
								.type(Token.Type.STRING)
								.lexeme("a")
								.build(),
						Token.builder()
								.type(Token.Type.QUESTION_MARK)
								.build(),
						Token.builder()
								.type(Token.Type.STRING)
								.lexeme("qwerty")
								.build(),
						Token.builder()
								.type(Token.Type.PLUS)
								.build(),
						Token.builder()
								.type(Token.Type.EOF)
								.build()
				))
		);
	}
}