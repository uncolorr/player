package com.comandante.uncolor.vkmusic.services.music;

import com.comandante.uncolor.vkmusic.models.BaseMusic;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Shuffler {

    public static int generateShuffleSeed(){
        Random random = new Random();
        return random.nextInt(100);
    }

    public static void shuffle(List<BaseMusic> playlist, int shuffleSeed){
        Collections.shuffle(playlist, new Random(shuffleSeed));
    }

    public static void unshuffle(List<BaseMusic> playlist, int shuffleSeed){
        Random rnd = new Random(shuffleSeed);
        int[] seq = new int[playlist.size()];
        for (int i = seq.length; i >= 1; i--) {
            seq[i - 1] = rnd.nextInt(i);
        }
        for (int i = 0; i < seq.length; i++) {
            Collections.swap(playlist, i, seq[i]);
        }
    }
}
