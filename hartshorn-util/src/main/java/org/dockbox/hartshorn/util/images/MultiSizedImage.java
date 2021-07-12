/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.hartshorn.util.images;

import org.dockbox.hartshorn.util.HartshornUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

public class MultiSizedImage {

    private final BufferedImage image;
    private final int xSize;
    private final int ySize;
    private final Map<Integer[], BufferedImage> imageMap = HartshornUtils.emptyMap();

    public MultiSizedImage(BufferedImage image, int xSize, int ySize) {
        this.image = image;
        this.xSize = xSize;
        this.ySize = ySize;
        this.generateImages();
    }

    private void generateImages() {
        for (int i = 0; i < this.xSize; i++) {
            for (int j = 0; j < this.ySize; j++) {
                BufferedImage sub = this.resizedImage().getSubimage(i * 128, j * 128, 128, 128);
                Integer[] pos = new Integer[]{ i, j };
                this.imageMap.put(pos, sub);
            }
        }
    }

    private BufferedImage resizedImage() {
        BufferedImage bufferedImage = new BufferedImage(this.xSize * 128, this.ySize * 128, BufferedImage.TRANSLUCENT);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(this.image, 0, 0, this.xSize * 128, this.ySize * 128, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    public Map<Integer[], BufferedImage> imageMap() {
        return this.imageMap;
    }
}
