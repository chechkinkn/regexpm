package ru.chechkin.internal;

import ru.chechkin.internal.parser.RegexpParser;
import ru.chechkin.internal.parser.node.Node;
import ru.chechkin.internal.scanner.RegexpScanner;

public class Pattern {
    private final Node root;

    private Pattern(String pattern) {
        this.root = new RegexpParser(new RegexpScanner(pattern).getTokens()).parse();
    }

    public static Pattern compile(String pattern) {
        return new Pattern(pattern);
    }

    public Matcher matcher() {
        return new RegexpMatcher(root);
    }
}
