package com.didww.sdk.prices;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// An RFC 4180 record reader: comma-separated fields, double-quoted fields that
// may hold commas, line breaks and doubled quotes; LF, CRLF or CR line ends (a
// line break inside a quoted field reads as LF). Empty lines are skipped and
// spaces are kept. A quote inside an unquoted field, text after a closing quote
// and an unterminated quoted field raise an IOException.
final class CsvRecordReader {
    private final BufferedReader reader;
    private int line;
    private int recordLine;

    CsvRecordReader(BufferedReader reader) {
        this.reader = reader;
    }

    // The line on which the last record returned starts (1-based).
    int recordLine() {
        return recordLine;
    }

    // The next record, or null at the end of the input.
    List<String> read() throws IOException {
        String text;
        do {
            text = reader.readLine();
            if (text == null) {
                return null;
            }
            line++;
        } while (text.isEmpty());

        RecordBuilder builder = new RecordBuilder();
        recordLine = line;
        while (!builder.parseLine(text, line)) {
            text = reader.readLine();
            if (text == null) {
                throw error(recordLine, "extraneous or missing \" in quoted-field");
            }
            line++;
        }
        return builder.fields;
    }

    static IOException error(int line, String message) {
        return new IOException("Price file: parse error on line " + line + ": " + message);
    }

    private enum State {
        FIELD_START,
        UNQUOTED,
        QUOTED,
        QUOTE_IN_QUOTED
    }

    private static final class RecordBuilder {
        private final List<String> fields = new ArrayList<>();
        private final StringBuilder field = new StringBuilder();
        private State state = State.FIELD_START;
        private boolean continued;

        // Parses one line; returns false while a quoted field continues on the next line.
        boolean parseLine(String text, int lineNumber) throws IOException {
            if (continued) {
                field.append('\n');
            }
            for (int i = 0; i < text.length(); i++) {
                accept(text.charAt(i), lineNumber);
            }
            if (state == State.QUOTED) {
                continued = true;
                return false;
            }
            endField();
            return true;
        }

        private void accept(char c, int lineNumber) throws IOException {
            switch (state) {
                case FIELD_START:
                    if (c == '"') {
                        state = State.QUOTED;
                    } else {
                        state = State.UNQUOTED;
                        acceptUnquoted(c, lineNumber);
                    }
                    break;
                case UNQUOTED:
                    acceptUnquoted(c, lineNumber);
                    break;
                case QUOTED:
                    if (c == '"') {
                        state = State.QUOTE_IN_QUOTED;
                    } else {
                        field.append(c);
                    }
                    break;
                default:
                    if (c == '"') {
                        field.append('"');
                        state = State.QUOTED;
                    } else if (c == ',') {
                        endField();
                    } else {
                        throw error(lineNumber, "extraneous or missing \" in quoted-field");
                    }
                    break;
            }
        }

        private void acceptUnquoted(char c, int lineNumber) throws IOException {
            if (c == ',') {
                endField();
            } else if (c == '"') {
                throw error(lineNumber, "bare \" in non-quoted field");
            } else {
                field.append(c);
            }
        }

        private void endField() {
            fields.add(field.toString());
            field.setLength(0);
            state = State.FIELD_START;
        }
    }
}
