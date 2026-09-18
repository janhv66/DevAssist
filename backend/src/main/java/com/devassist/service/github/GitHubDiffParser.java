package com.devassist.service.github;

import java.util.ArrayList;
import java.util.List;

public class GitHubDiffParser {

    public List<ParsedDiff> parse(String diff) {
        List<ParsedDiff> files = new ArrayList<>();

        String[] lines = diff.split("\n");

        String currentFile = null;
        List<ChangedLine> changedLines = new ArrayList<>();

        int currentLine = 0;

        for (String line : lines) {

            if (line.startsWith("+++ b/")) {
                if (currentFile != null) {
                    files.add(new ParsedDiff(
                            currentFile,
                            buildCode(changedLines),
                            List.copyOf(changedLines)
                    ));
                }

                currentFile = line.substring(6);
                changedLines = new ArrayList<>();
                continue;
            }

            if (line.startsWith("@@")) {
                currentLine = parseNewFileLine(line);
                continue;
            }

            if (currentFile == null) {
                continue;
            }

            if (line.startsWith("+") && !line.startsWith("+++")) {

                String code = line.substring(1);

                changedLines.add(
                        new ChangedLine(
                                currentLine,
                                code,
                                true
                        )
                );

                currentLine++;

            } else if (!line.startsWith("-")) {

                String code = line;

                changedLines.add(
                        new ChangedLine(
                                currentLine,
                                code,
                                false
                        )
                );

                currentLine++;
            }
        }

        if (currentFile != null) {
            files.add(new ParsedDiff(
                    currentFile,
                    buildCode(changedLines),
                    List.copyOf(changedLines)
            ));
        }

        return files;
    }

    private String buildCode(List<ChangedLine> lines) {
        StringBuilder code = new StringBuilder();

        for (ChangedLine line : lines) {
            code.append(line.content()).append("\n");
        }

        return code.toString();
    }

    private int parseNewFileLine(String hunkHeader) {
        int plusIndex = hunkHeader.indexOf("+");

        if (plusIndex == -1) {
            return 1;
        }

        String hunk = hunkHeader.substring(plusIndex + 1);

        int commaIndex = hunk.indexOf(",");

        String startLine;

        if (commaIndex == -1) {
            startLine = hunk;
        } else {
            startLine = hunk.substring(0, commaIndex);
        }

        try {
            return Integer.parseInt(startLine);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public record ParsedDiff(
            String filePath,
            String code,
            List<ChangedLine> changedLines
    ) {}

    public record ChangedLine(
            int lineNumber,
            String content,
            boolean added
    ) {}
}