package com.ssafy.questory.common.config;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class P6spySqlFormatter extends JdbcEventListener implements MessageFormattingStrategy {

    // 필요하면 여기로 조절
    private static final long SLOW_MS = 200;         // 200ms 이상이면 SLOW 표시
    private static final boolean HIDE_RESULTSET_DUMP = true; // resultset의 바인딩 덤프 숨길지

    @Override
    public void onAfterGetConnection(ConnectionInformation connectionInformation, SQLException e) {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(getClass().getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {

        if (!StringUtils.hasText(sql)) return "";

        // resultset에서 "1='1', 2='x' ..." 같은 덤프는 SQL이 아니라서 그대로 두거나 숨김
        if ("resultset".equalsIgnoreCase(category)) {
            if (HIDE_RESULTSET_DUMP && looksLikeResultsetDump(sql)) {
                return ""; // 아예 숨김
            }
            return box(category, elapsed, sql.strip());
        }

        String formatted = formatSqlSafely(sql);
        String prefix = elapsed >= SLOW_MS ? "🐢 SLOW " : "";
        return box(prefix + category, elapsed, formatted);
    }

    private String box(String category, long elapsed, String body) {
        return """
                
                =============================== SQL LOG ===============================
                Category : %s
                Time     : %d ms
                
                %s
                =======================================================================
                """.formatted(category, elapsed, body);
    }

    /**
     * 문자열/식별자 안은 손대지 않고, 밖에서만 키워드 줄바꿈/정렬
     */
    private String formatSqlSafely(String sql) {
        String compact = compactWhitespace(sql);

        // SQL을 "보호 구간"과 "일반 구간"으로 분리해서, 일반 구간에만 포매팅 적용
        List<Token> tokens = tokenizePreservingQuotedSections(compact);

        StringBuilder out = new StringBuilder();
        for (Token t : tokens) {
            if (t.protectedSection) {
                out.append(t.text);
            } else {
                out.append(applyKeywordNewlines(t.text));
            }
        }

        return out.toString().strip();
    }

    private String compactWhitespace(String sql) {
        // 줄바꿈/탭 등을 일단 공백으로 정리 (나중에 우리가 원하는 줄바꿈을 넣음)
        return sql.replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
                .replaceAll("\\s+", " ")
                .strip();
    }

    private static final Pattern WORD_SELECT    = Pattern.compile("(?i)\\bselect\\b");
    private static final Pattern WORD_FROM      = Pattern.compile("(?i)\\bfrom\\b");
    private static final Pattern WORD_WHERE     = Pattern.compile("(?i)\\bwhere\\b");
    private static final Pattern WORD_AND       = Pattern.compile("(?i)\\band\\b");
    private static final Pattern WORD_OR        = Pattern.compile("(?i)\\bor\\b");
    private static final Pattern WORD_VALUES    = Pattern.compile("(?i)\\bvalues\\b");
    private static final Pattern WORD_SET       = Pattern.compile("(?i)\\bset\\b");
    private static final Pattern WORD_JOIN      = Pattern.compile("(?i)\\bjoin\\b");
    private static final Pattern WORD_LEFT_JOIN = Pattern.compile("(?i)\\bleft\\s+join\\b");
    private static final Pattern WORD_RIGHT_JOIN= Pattern.compile("(?i)\\bright\\s+join\\b");
    private static final Pattern WORD_INNER_JOIN= Pattern.compile("(?i)\\binner\\s+join\\b");
    private static final Pattern WORD_GROUP_BY  = Pattern.compile("(?i)\\bgroup\\s+by\\b");
    private static final Pattern WORD_ORDER_BY  = Pattern.compile("(?i)\\border\\s+by\\b");
    private static final Pattern WORD_INSERT_INTO = Pattern.compile("(?i)\\binsert\\s+into\\b");
    private static final Pattern WORD_UPDATE    = Pattern.compile("(?i)\\bupdate\\b");
    private static final Pattern WORD_DELETE    = Pattern.compile("(?i)\\bdelete\\b");

    private String applyKeywordNewlines(String s) {
        // "left join" 같은 복합 키워드를 먼저 처리
        s = WORD_LEFT_JOIN.matcher(s).replaceAll("\nLEFT JOIN");
        s = WORD_RIGHT_JOIN.matcher(s).replaceAll("\nRIGHT JOIN");
        s = WORD_INNER_JOIN.matcher(s).replaceAll("\nINNER JOIN");

        s = WORD_SELECT.matcher(s).replaceAll("\nSELECT");
        s = WORD_FROM.matcher(s).replaceAll("\nFROM");
        s = WORD_WHERE.matcher(s).replaceAll("\nWHERE");
        s = WORD_JOIN.matcher(s).replaceAll("\nJOIN");
        s = WORD_GROUP_BY.matcher(s).replaceAll("\nGROUP BY");
        s = WORD_ORDER_BY.matcher(s).replaceAll("\nORDER BY");

        s = WORD_INSERT_INTO.matcher(s).replaceAll("\nINSERT INTO");
        s = WORD_VALUES.matcher(s).replaceAll("\nVALUES");
        s = WORD_UPDATE.matcher(s).replaceAll("\nUPDATE");
        s = WORD_SET.matcher(s).replaceAll("\nSET");
        s = WORD_DELETE.matcher(s).replaceAll("\nDELETE");

        // 조건절 정렬
        s = WORD_AND.matcher(s).replaceAll("\n  AND");
        s = WORD_OR.matcher(s).replaceAll("\n  OR");

        // 앞쪽 첫 줄바꿈 제거
        return s.replaceFirst("^\\s*\\n", "");
    }

    /**
     * '...', "...", `...` 내부는 보호 구간으로 분리
     */
    private List<Token> tokenizePreservingQuotedSections(String sql) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder buf = new StringBuilder();

        boolean inSingle = false; // '
        boolean inDouble = false; // "
        boolean inBack   = false; // `
        boolean protectedNow = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            // escape 처리: \' or \" 같은 경우
            if ((inSingle || inDouble) && c == '\\' && i + 1 < sql.length()) {
                buf.append(c).append(sql.charAt(i + 1));
                i++;
                continue;
            }

            // 토글 진입/이탈
            if (!inDouble && !inBack && c == '\'') {
                // 구간이 바뀌기 직전 flush
                flush(tokens, buf, protectedNow);
                inSingle = !inSingle;
                protectedNow = inSingle || inDouble || inBack;
                buf.append(c);
                continue;
            }
            if (!inSingle && !inBack && c == '"') {
                flush(tokens, buf, protectedNow);
                inDouble = !inDouble;
                protectedNow = inSingle || inDouble || inBack;
                buf.append(c);
                continue;
            }
            if (!inSingle && !inDouble && c == '`') {
                flush(tokens, buf, protectedNow);
                inBack = !inBack;
                protectedNow = inSingle || inDouble || inBack;
                buf.append(c);
                continue;
            }

            buf.append(c);
        }

        flush(tokens, buf, protectedNow);
        return tokens;
    }

    private void flush(List<Token> tokens, StringBuilder buf, boolean protectedSection) {
        if (buf.length() == 0) return;
        tokens.add(new Token(buf.toString(), protectedSection));
        buf.setLength(0);
    }

    private boolean looksLikeResultsetDump(String s) {
        // 대충 "1 = '...'" 형태가 있으면 덤프라고 판단
        String t = s.stripLeading();
        return t.matches("^\\d+\\s*=.*") || t.contains(" = '") || t.contains(" = NULL");
    }

    private static class Token {
        final String text;
        final boolean protectedSection;

        private Token(String text, boolean protectedSection) {
            this.text = text;
            this.protectedSection = protectedSection;
        }
    }
}