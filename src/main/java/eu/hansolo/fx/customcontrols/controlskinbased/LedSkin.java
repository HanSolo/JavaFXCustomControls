/*
 * Copyright (c) 2021 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.customcontrols.controlskinbased;

import javafx.beans.InvalidationListener;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;


public class LedSkin extends SkinBase<CustomControl> implements Skin<CustomControl> {
    private static final double               PREFERRED_WIDTH  = 16;
    private static final double               PREFERRED_HEIGHT = 16;
    private static final double               MINIMUM_WIDTH    = 8;
    private static final double               MINIMUM_HEIGHT   = 8;
    private static final double               MAXIMUM_WIDTH    = 1024;
    private static final double               MAXIMUM_HEIGHT   = 1024;
    private              double               size;
    private              Region               frame;
    private              Region               main;
    private              Region               highlight;
    private              InnerShadow          innerShadow;
    private              DropShadow           glow;
    private              CustomControl        control;
    private              InvalidationListener sizeListener;
    private              InvalidationListener colorListener;
    private              InvalidationListener stateListener;


    // ******************** Constructors **************************************
    public LedSkin(final CustomControl control) {
        super(control);
        this.control  = control;
        sizeListener  = o -> handleControlPropertyChanged("RESIZE");
        colorListener = o -> handleControlPropertyChanged("COLOR");
        stateListener = o -> handleControlPropertyChanged("STATE");
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(control.getPrefWidth(), 0.0) <= 0 || Double.compare(control.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(control.getWidth(), 0.0) <= 0 || Double.compare(control.getHeight(), 0.0) <= 0) {
            if (control.getPrefWidth() > 0 && control.getPrefHeight() > 0) {
                control.setPrefSize(control.getPrefWidth(), control.getPrefHeight());
            } else {
                control.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        frame = new Region();
        frame.getStyleClass().setAll("frame");

        main = new Region();
        main.getStyleClass().setAll("main");
        main.setStyle(String.join("", "-color: ", control.getColor().toString().replace("0x", "#"), ";"));

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 8, 0, 0, 0);

        glow = new DropShadow(BlurType.TWO_PASS_BOX, control.getColor(), 20, 0, 0, 0);
        glow.setInput(innerShadow);

        highlight = new Region();
        highlight.getStyleClass().setAll("highlight");

        getChildren().addAll(frame, main, highlight);
    }

    private void registerListeners() {
        control.widthProperty().addListener(sizeListener);
        control.heightProperty().addListener(sizeListener);
        control.colorProperty().addListener(colorListener);
        control.stateProperty().addListener(stateListener);
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double height, final double top, final double right, final double bottom, final double left)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double width, final double top, final double right, final double bottom, final double left)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double height, final double top, final double right, final double bottom, final double left) { return super.computePrefWidth(height, top, right, bottom, left); }
    @Override protected double computePrefHeight(final double width, final double top, final double right, final double bottom, final double left) { return super.computePrefHeight(width, top, right, bottom, left); }
    @Override protected double computeMaxWidth(final double width, final double top, final double right, final double bottom, final double left)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double width, final double top, final double right, final double bottom, final double left)  { return MAXIMUM_HEIGHT; }

    protected void handleControlPropertyChanged(final String property) {
        if ("RESIZE".equals(property)) {
            resize();
        } else if ("COLOR".equals(property)) {
            main.setStyle(String.join("", "-color: ", (control.getColor()).toString().replace("0x", "#"), ";"));
            resize();
        } else if ("STATE".equals(property)) {
            main.setEffect(control.getState() ? glow : innerShadow);
        }
    }

    @Override public void dispose() {
        control.widthProperty().removeListener(sizeListener);
        control.heightProperty().removeListener(sizeListener);
        control.colorProperty().removeListener(colorListener);
        control.stateProperty().removeListener(stateListener);
        control = null;
    }


    // ******************** Layout ********************************************
    @Override public void layoutChildren(final double x, final double y, final double width, final double height) {
        super.layoutChildren(x, y, width, height);
    }

    private void resize() {
        double width  = control.getWidth() - control.getInsets().getLeft() - control.getInsets().getRight();
        double height = control.getHeight() - control.getInsets().getTop() - control.getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            innerShadow.setRadius(0.07 * size);
            glow.setRadius(0.36 * size);
            glow.setColor(control.getColor());

            frame.setMaxSize(size, size);

            main.setMaxSize(0.72 * size, 0.72 * size);
            main.relocate(0.14 * size, 0.14 * size);
            main.setEffect(control.getState() ? glow : innerShadow);

            highlight.setMaxSize(0.58 * size, 0.58 * size);
            highlight.relocate(0.21 * size, 0.21 * size);
        }
    }
}
