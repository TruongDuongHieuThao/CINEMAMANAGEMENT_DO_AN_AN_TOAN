package com.theatermgnt.theatermgnt.common.util;

import com.github.slugify.Slugify;

public class SlugUtil {
    private static final Slugify slugify = Slugify.builder().build();

    /**
     * Generate a URL-friendly slug from the given text
     * @param text The text to slugify (e.g., movie title)
     * @return A URL-friendly slug
     */
    public static String generateSlug(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        return slugify.slugify(text);
    }

    /**
     * Generate a unique slug by appending a number if needed
     * @param baseText The text to slugify
     * @param counter The counter to append (0 means no counter)
     * @return A unique slug with counter if needed
     */
    public static String generateUniqueSlug(String baseText, int counter) {
        String slug = generateSlug(baseText);
        if (counter > 0) {
            slug = slug + "-" + counter;
        }
        return slug;
    }
}
