package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.timeago.PatternsHolder;
import org.schabi.newpipe.extractor.utils.Parser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A helper class that is meant to be used by services that need to parse upload dates in the
 * format '2 days ago' or similar.
 */
public class TimeAgoParser {
    private final PatternsHolder patternsHolder;
    private final OffsetDateTime now;

    /**
     * Creates a helper to parse upload dates in the format '2 days ago'.
     * <p>
     * Instantiate a new {@link TimeAgoParser} every time you extract a new batch of items.
     * </p>
     *
     * @param patternsHolder An object that holds the "time ago" patterns, special cases, and the
     *                       language word separator.
     */
    public TimeAgoParser(final PatternsHolder patternsHolder) {
        this.patternsHolder = patternsHolder;
        now = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Parses a textual date in the format '2 days ago' into a Calendar representation which is then
     * wrapped in a {@link DateWrapper} object.
     * <p>
     * Beginning with days ago, the date is considered as an approximation.
     *
     * @param textualDate The original date as provided by the streaming service
     * @return The parsed time (can be approximated)
     * @throws ParsingException if the time unit could not be recognized
     */
    public DateWrapper parse(final String textualDate) throws ParsingException {
        for (final Map.Entry<ChronoUnit, Map<String, Integer>> caseUnitEntry
                : patternsHolder.specialCases().entrySet()) {
            final ChronoUnit chronoUnit = caseUnitEntry.getKey();
            for (final Map.Entry<String, Integer> caseMapToAmountEntry
                    : caseUnitEntry.getValue().entrySet()) {
                final String caseText = caseMapToAmountEntry.getKey();
                final Integer caseAmount = caseMapToAmountEntry.getValue();

                if (textualDate.contains(caseText)) {
                    return getResultFor(caseAmount, chronoUnit);
                }
            }
        }

        return getResultFor(parseTimeAgoAmount(textualDate), parseChronoUnit(textualDate));
    }

    private int parseTimeAgoAmount(final String textualDate) {
        try {
            return Integer.parseInt(textualDate.replaceAll("\\D+", ""));
        } catch (final NumberFormatException ignored) {
            // If there is no valid number in the textual date,
            // assume it is 1 (as in 'a second ago').
            return 1;
        }
    }

    private ChronoUnit parseChronoUnit(final String textualDate) throws ParsingException {

        if (patternsHolder.wordSeparator().isEmpty()) {
            return patternsHolder.asMap().entrySet().stream().filter(e -> e.getValue().stream()
                .anyMatch(agoPhrase -> textualDate.contains(agoPhrase)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() ->
                        new ParsingException("Unable to parse the date: " + textualDate));
        }

        String date = textualDate.toLowerCase();
        List<String> words = new ArrayList<>();
        String word = getNextWord(date);
        while (!word.isEmpty()) {
            words.add(word);
            date = date.substring(word.length());
            word = getNextWord(date);
        }

        return patternsHolder.asMap().entrySet().stream()
                .filter(e -> e.getValue().stream()
                        .anyMatch(agoPhrase -> textualDateMatches(words, agoPhrase) || textualDate.equals(agoPhrase)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() ->
                        new ParsingException("Unable to parse the date: " + textualDate));
    }

    public static int getNextNonBlankIndex(final String s){
        int left = 0;
        final int len = s.length();
        // Includes numbers...
        final int[] spaces = {9, 32, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 160, 5760, 6158, 8192, 8193, 8194, 8195, 8196, 8197, 8198, 8199, 8200, 8201, 8202, 8239, 8287, 12288};
        while (left < len){
            int c = (int) s.charAt(left);
            if (Arrays.binarySearch(spaces, c) < 0)
                break;
            left++;
        }
        return left;
    }

    public static int getNextBlankIndex(final String s){
        int left = 0;
        final int len = s.length();
        // Includes numbers...
        final int[] spaces = {9, 32, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 160, 5760, 6158, 8192, 8193, 8194, 8195, 8196, 8197, 8198, 8199, 8200, 8201, 8202, 8239, 8287, 12288};
        while (left < len){
            int c = (int) s.charAt(left);
            if (Arrays.binarySearch(spaces, c) >= 0)
                break;
            left++;
        }
        return left;
    }

    public static String getNextWord(final String s){
        final int len = s.length();
        final int left = getNextNonBlankIndex(s);
        if (left == len)
            return "";
        if (left + 1 == len)
            return "" + s.charAt(left);
        final int right = getNextBlankIndex(s.substring(left+1)) + left + 1;
        return s.substring(left, right);
    }

    private boolean textualDateMatches(final List<String> words, final String agoPhrase) {
        for (String word : words){
            if (agoPhrase.equals(word))
                return true;
        }
        return false;
    }

    private DateWrapper getResultFor(final int timeAgoAmount, final ChronoUnit chronoUnit) {
        OffsetDateTime offsetDateTime = now;
        boolean isApproximation = false;

        switch (chronoUnit) {
            case SECONDS:
            case MINUTES:
            case HOURS:
                offsetDateTime = offsetDateTime.minus(timeAgoAmount, chronoUnit);
                break;

            case DAYS:
            case WEEKS:
            case MONTHS:
                offsetDateTime = offsetDateTime.minus(timeAgoAmount, chronoUnit);
                isApproximation = true;
                break;

            case YEARS:
                // minusDays is needed to prevent `PrettyTime` from showing '12 months ago'.
                offsetDateTime = offsetDateTime.minusYears(timeAgoAmount).minusDays(1);
                isApproximation = true;
                break;
        }

        if (isApproximation) {
            offsetDateTime = offsetDateTime.truncatedTo(ChronoUnit.HOURS);
        }

        return new DateWrapper(offsetDateTime, isApproximation);
    }
}
