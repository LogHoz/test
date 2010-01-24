
/**
 * Substrate: grow crystal-like lines on a computational substrate
 *
 *       Lines like crystals grow on a computational substrate.  A  simple  per-
       pendicular growth rule creates intricate city-like structures.  Option-
       ally, cracks may also be circular, producing a cityscape more  familiar
       to places for which city planning is a distant, theoretical concern.

       Ported from the code by j.tarbell at http://complexification.net

       Copyright  ©  2003   by   J.   Tarbell   (complex@complexification.net,
       http://www.complexification.net).

       Ported      to      XScreensaver      2004      by     Mike     Kershaw
       (dragorn@kismetwireless.net)

AUTHOR
       J. Tarbell <complex@complexification.net>, Jun-03

       Mike Kershaw <dragorn@kismetwireless.net>, Oct-04


 * This is an Android implementation of the KDE game "knetwalk" by
 * Andi Peredri, Thomas Nagy, and Reinhold Kainhofer.
 *
 * © 2007-2010 Ian Cameron Smith <johantheghost@yahoo.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2
 *   as published by the Free Software Foundation (see COPYING).
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */


package org.hermit.substrate;


import java.util.Random;

import net.goui.util.MTRandom;


/**
 * A colour palette originally derived (apparently) from a Jackson
 * Pollock painting.
 */
public class PollockPalette
    implements Palette
{

    // ******************************************************************** //
    // Public Methods.
    // ******************************************************************** //
    
    /**
     * Get a random colour from this palette.
     * 
     * @return          A radomly selected colour.
     */
    public int getRandom() {
        return POLLOCK[MT_RANDOM.nextInt(POLLOCK_SIZE)];
    }

    
    // ******************************************************************** //
    // Class Data.
    // ******************************************************************** //
    
    // Random number generator.  We use a Mersenne Twister,
    // which is a high-quality and fast implementation of java.util.Random.
    private static final Random MT_RANDOM = new MTRandom();

    // The palette data.
    private static final int[] POLLOCK = {
            (0 << 16) | (0 << 8) | 0,
            (0 << 16) | (16 << 8) | 0,
            (104 << 16) | (104 << 8) | 112,
            (104 << 16) | (112 << 8) | 120,
            (104 << 16) | (88 << 8) | 88,
            (112 << 16) | (128 << 8) | 128,
            (120 << 16) | (120 << 8) | 128,
            (128 << 16) | (88 << 8) | 0,
            (144 << 16) | (104 << 8) | 72,
            (144 << 16) | (80 << 8) | 72,
            (144 << 16) | (88 << 8) | 24,
            (144 << 16) | (96 << 8) | 112,
            (152 << 16) | (104 << 8) | 112,
            (152 << 16) | (160 << 8) | 184,
            (152 << 16) | (48 << 8) | 16,
            (152 << 16) | (64 << 8) | 8,
            (16 << 16) | (0 << 8) | 0,
            (16 << 16) | (32 << 8) | 40,
            (160 << 16) | (112 << 8) | 120,
            (160 << 16) | (120 << 8) | 0,
            (160 << 16) | (120 << 8) | 72,
            (160 << 16) | (128 << 8) | 120,
            (160 << 16) | (144 << 8) | 120,
            (160 << 16) | (160 << 8) | 168,
            (160 << 16) | (56 << 8) | 16,
            (160 << 16) | (88 << 8) | 16,
            (168 << 16) | (144 << 8) | 112,
            (168 << 16) | (152 << 8) | 104,
            (168 << 16) | (152 << 8) | 72,
            (168 << 16) | (160 << 8) | 120,
            (168 << 16) | (168 << 8) | 128,
            (168 << 16) | (176 << 8) | 160,
            (168 << 16) | (40 << 8) | 24,
            (176 << 16) | (136 << 8) | 104,
            (176 << 16) | (144 << 8) | 32,
            (176 << 16) | (144 << 8) | 48,
            (176 << 16) | (160 << 8) | 152,
            (176 << 16) | (168 << 8) | 112,
            (176 << 16) | (168 << 8) | 144,
            (176 << 16) | (176 << 8) | 120,
            (176 << 16) | (176 << 8) | 152,
            (176 << 16) | (184 << 8) | 176,
            (176 << 16) | (192 << 8) | 184,
            (184 << 16) | (136 << 8) | 104,
            (184 << 16) | (184 << 8) | 168,
            (184 << 16) | (184 << 8) | 176,
            (192 << 16) | (152 << 8) | 136,
            (192 << 16) | (160 << 8) | 96,
            (192 << 16) | (176 << 8) | 120,
            (192 << 16) | (176 << 8) | 144,
            (192 << 16) | (192 << 8) | 144,
            (192 << 16) | (192 << 8) | 176,
            (200 << 16) | (160 << 8) | 120,
            (200 << 16) | (176 << 8) | 120,
            (200 << 16) | (176 << 8) | 96,
            (200 << 16) | (184 << 8) | 160,
            (200 << 16) | (192 << 8) | 152,
            (200 << 16) | (200 << 8) | 184,
            (208 << 16) | (176 << 8) | 120,
            (208 << 16) | (176 << 8) | 128,
            (208 << 16) | (176 << 8) | 176,
            (208 << 16) | (184 << 8) | 144,
            (208 << 16) | (192 << 8) | 160,
            (208 << 16) | (192 << 8) | 88,
            (208 << 16) | (200 << 8) | 160,
            (208 << 16) | (200 << 8) | 168,
            (208 << 16) | (200 << 8) | 176,
            (208 << 16) | (208 << 8) | 192,
            (216 << 16) | (192 << 8) | 112,
            (216 << 16) | (192 << 8) | 136,
            (216 << 16) | (192 << 8) | 160,
            (216 << 16) | (192 << 8) | 168,
            (216 << 16) | (192 << 8) | 176,
            (216 << 16) | (200 << 8) | 152,
            (216 << 16) | (200 << 8) | 160,
            (216 << 16) | (200 << 8) | 176,
            (216 << 16) | (208 << 8) | 176,
            (224 << 16) | (160 << 8) | 96,
            (224 << 16) | (176 << 8) | 128,
            (224 << 16) | (184 << 8) | 80,
            (224 << 16) | (200 << 8) | 160,
            (224 << 16) | (200 << 8) | 168,
            (224 << 16) | (200 << 8) | 88,
            (224 << 16) | (208 << 8) | 152,
            (224 << 16) | (208 << 8) | 160,
            (224 << 16) | (208 << 8) | 176,
            (224 << 16) | (216 << 8) | 160,
            (224 << 16) | (216 << 8) | 176,
            (224 << 16) | (216 << 8) | 184,
            (224 << 16) | (224 << 8) | 176,
            (224 << 16) | (224 << 8) | 184,
            (224 << 16) | (224 << 8) | 192,
            (232 << 16) | (184 << 8) | 120,
            (232 << 16) | (184 << 8) | 40,
            (232 << 16) | (192 << 8) | 120,
            (232 << 16) | (192 << 8) | 136,
            (232 << 16) | (200 << 8) | 128,
            (232 << 16) | (200 << 8) | 152,
            (232 << 16) | (200 << 8) | 72,
            (232 << 16) | (208 << 8) | 120,
            (232 << 16) | (208 << 8) | 80,
            (232 << 16) | (216 << 8) | 168,
            (232 << 16) | (216 << 8) | 192,
            (232 << 16) | (216 << 8) | 200,
            (232 << 16) | (224 << 8) | 128,
            (232 << 16) | (224 << 8) | 152,
            (232 << 16) | (224 << 8) | 176,
            (232 << 16) | (224 << 8) | 200,
            (232 << 16) | (232 << 8) | 216,
            (232 << 16) | (240 << 8) | 192,
            (232 << 16) | (240 << 8) | 216,
            (232 << 16) | (240 << 8) | 224,
            (240 << 16) | (200 << 8) | 104,
            (240 << 16) | (200 << 8) | 152,
            (240 << 16) | (208 << 8) | 152,
            (240 << 16) | (216 << 8) | 112,
            (240 << 16) | (216 << 8) | 144,
            (240 << 16) | (216 << 8) | 152,
            (240 << 16) | (216 << 8) | 192,
            (240 << 16) | (216 << 8) | 208,
            (240 << 16) | (224 << 8) | 128,
            (240 << 16) | (224 << 8) | 176,
            (240 << 16) | (224 << 8) | 184,
            (240 << 16) | (224 << 8) | 192,
            (240 << 16) | (232 << 8) | 160,
            (240 << 16) | (232 << 8) | 184,
            (240 << 16) | (232 << 8) | 192,
            (240 << 16) | (232 << 8) | 200,
            (240 << 16) | (232 << 8) | 208,
            (240 << 16) | (232 << 8) | 216,
            (240 << 16) | (240 << 8) | 192,
            (240 << 16) | (240 << 8) | 200,
            (240 << 16) | (240 << 8) | 208,
            (240 << 16) | (240 << 8) | 224,
            (240 << 16) | (248 << 8) | 168,
            (248 << 16) | (184 << 8) | 136,
            (248 << 16) | (184 << 8) | 40,
            (248 << 16) | (224 << 8) | 112,
            (248 << 16) | (224 << 8) | 160,
            (248 << 16) | (224 << 8) | 168,
            (248 << 16) | (224 << 8) | 176,
            (248 << 16) | (224 << 8) | 184,
            (248 << 16) | (224 << 8) | 192,
            (248 << 16) | (224 << 8) | 216,
            (248 << 16) | (224 << 8) | 80,
            (248 << 16) | (232 << 8) | 120,
            (248 << 16) | (232 << 8) | 184,
            (248 << 16) | (232 << 8) | 192,
            (248 << 16) | (232 << 8) | 208,
            (248 << 16) | (232 << 8) | 224,
            (248 << 16) | (240 << 8) | 184,
            (248 << 16) | (240 << 8) | 200,
            (248 << 16) | (240 << 8) | 208,
            (248 << 16) | (240 << 8) | 216,
            (248 << 16) | (248 << 8) | 208,
            (248 << 16) | (248 << 8) | 248,
            (255 << 16) | (152 << 8) | 40,
            (255 << 16) | (200 << 8) | 40,
            (255 << 16) | (208 << 8) | 112,
            (255 << 16) | (208 << 8) | 184,
            (255 << 16) | (208 << 8) | 40,
            (255 << 16) | (216 << 8) | 160,
            (255 << 16) | (216 << 8) | 168,
            (255 << 16) | (216 << 8) | 176,
            (255 << 16) | (224 << 8) | 120,
            (255 << 16) | (232 << 8) | 104,
            (255 << 16) | (232 << 8) | 120,
            (255 << 16) | (232 << 8) | 144,
            (255 << 16) | (232 << 8) | 152,
            (255 << 16) | (232 << 8) | 176,
            (255 << 16) | (232 << 8) | 184,
            (255 << 16) | (232 << 8) | 192,
            (255 << 16) | (232 << 8) | 200,
            (255 << 16) | (232 << 8) | 208,
            (255 << 16) | (240 << 8) | 152,
            (255 << 16) | (240 << 8) | 160,
            (255 << 16) | (240 << 8) | 184,
            (255 << 16) | (240 << 8) | 192,
            (255 << 16) | (240 << 8) | 200,
            (255 << 16) | (240 << 8) | 208,
            (255 << 16) | (240 << 8) | 216,
            (255 << 16) | (240 << 8) | 232,
            (255 << 16) | (240 << 8) | 248,
            (255 << 16) | (248 << 8) | 176,
            (255 << 16) | (248 << 8) | 192,
            (255 << 16) | (248 << 8) | 200,
            (255 << 16) | (248 << 8) | 208,
            (255 << 16) | (248 << 8) | 216,
            (255 << 16) | (248 << 8) | 224,
            (255 << 16) | (255 << 8) | 200,
            (255 << 16) | (255 << 8) | 208,
            (255 << 16) | (255 << 8) | 216,
            (48 << 16) | (32 << 8) | 8,
            (56 << 16) | (40 << 8) | 16,
            (56 << 16) | (56 << 8) | 40,
            (56 << 16) | (64 << 8) | 48,
            (72 << 16) | (72 << 8) | 88,
            (80 << 16) | (16 << 8) | 0,
            (80 << 16) | (88 << 8) | 96,
            (88 << 16) | (104 << 8) | 104,
            (88 << 16) | (104 << 8) | 88,
            (88 << 16) | (40 << 8) | 0,
            (88 << 16) | (80 << 8) | 72,
            (88 << 16) | (88 << 8) | 56,
            (96 << 16) | (112 << 8) | 112,
            (96 << 16) | (56 << 8) | 16,
    };
    private static final int POLLOCK_SIZE = POLLOCK.length;

}

