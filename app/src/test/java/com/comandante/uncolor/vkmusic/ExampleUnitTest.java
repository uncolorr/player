package com.comandante.uncolor.vkmusic;

import com.comandante.uncolor.vkmusic.models.VkMusic;

import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        VkMusic vkMusic1 = new VkMusic();
        VkMusic vkMusic2 = new VkMusic();

        vkMusic1.setId(4533);
        vkMusic2.setId(4533);

        boolean isEquals = Objects.equals(vkMusic1, vkMusic2);
        assertEquals(isEquals, true);
    }
}