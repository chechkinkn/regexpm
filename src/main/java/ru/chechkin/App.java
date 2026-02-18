package ru.chechkin;

import ru.chechkin.internal.Matcher;
import ru.chechkin.internal.Pattern;

public class App {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("(a*|c)c?");

        Matcher matcher = pattern.matcher();

        System.out.println(matcher.match("aaa"));
        System.out.println(matcher.match(""));
        System.out.println(matcher.match("a"));
        System.out.println(matcher.match("c"));
        System.out.println(matcher.match("ac"));
        System.out.println(matcher.match("a"));

        java.util.regex.Matcher m =  java.util.regex.Pattern.compile("(?<name>(a|ab))c")
            .matcher("abc");

        m.matches();

        System.out.println(m.group("name"));
    }
}
