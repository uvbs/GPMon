package com.jinsung.adoda.gpmon;

import com.jinsung.adoda.gpmon.utils.DateUtil;

import org.junit.Test;

import java.lang.Exception;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void gestBeforeDates() throws Exception {
        String[] dates = DateUtil.getBeforeDatesStr(DateUtil.getToday(), 3);
        for (int i = 0 ; i < dates.length; i++) {
            assertTrue(DateUtil.isValidDateStr(dates[i]));
            if (i == dates.length-1)
                assertEquals(dates[i], DateUtil.getToday());
        }
    }
}