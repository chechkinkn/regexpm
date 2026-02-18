package ru.chechkin.internal.scanner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

@Builder
@AllArgsConstructor
public class Token {
    public enum Type {
        SLASH, PLUS, STAR, QUESTION_MARK, STRING, LEFT_PAREN, RIGHT_PAREN, EOF
    }

    @Getter
    private Type type;

    @Getter
    private String lexeme;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Token token)) {
            return false;
        }
        return type == token.type && Optional.ofNullable(lexeme)
            .map(currentLexeme -> Objects.equals(currentLexeme, token.lexeme))
            .orElse(true);

    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(
            "Token{"
        );

        builder.append("type=").append(type);

        Optional.ofNullable(lexeme)
            .ifPresent(lexeme -> builder.append(", lexeme=").append(lexeme));

        builder.append('}');

        return builder.toString();
    }
}
