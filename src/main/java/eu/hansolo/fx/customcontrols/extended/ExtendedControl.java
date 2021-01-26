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

package eu.hansolo.fx.customcontrols.extended;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;


public class ExtendedControl extends TextField {
    private static final StyleablePropertyFactory<ExtendedControl> FACTORY                       = new StyleablePropertyFactory<>(TextField.getClassCssMetaData());
    private static final Color                                     DEFAULT_MATERIAL_DESIGN_COLOR = Color.web("#009688");
    private static final Color                                     DEFAULT_PROMPT_TEXT_COLOR     = Color.web("#757575");
    private static final double                                    STD_FONT_SIZE                 = 13;
    private static final double                                    SMALL_FONT_SIZE               = 10;
    private static final double                                    TOP_OFFSET_Y                  = 4;
    private static final int                                       ANIMATION_DURATION            = 60;
    private static final CssMetaData<ExtendedControl, Color>       MATERIAL_DESIGN_COLOR         = FACTORY.createColorCssMetaData("-material-design-color", s -> s.materialDesignColor, DEFAULT_MATERIAL_DESIGN_COLOR, false);
    private static final CssMetaData<ExtendedControl, Color>       PROMPT_TEXT_COLOR             = FACTORY.createColorCssMetaData("-prompt-text-color", s -> s.promptTextColor, DEFAULT_PROMPT_TEXT_COLOR, false);
    private static       String                                    userAgentStyleSheet;
    private        final StyleableProperty<Color>                  materialDesignColor;
    private        final StyleableProperty<Color>                  promptTextColor;
    private              Text                                      promptText;
    private              HBox                                      text;
    private              DoubleProperty                            fontSize;
    private              Timeline                                  timeline;


    // ******************** Constructors **************************************
    public ExtendedControl() {
        this("");
    }
    public ExtendedControl(final String TEXT) {
        super(TEXT);

        materialDesignColor = new SimpleStyleableObjectProperty<>(MATERIAL_DESIGN_COLOR, this, "materialDesignColor");
        promptTextColor     = new SimpleStyleableObjectProperty<>(PROMPT_TEXT_COLOR, this, "promptTextColor");

        fontSize            = new SimpleDoubleProperty(ExtendedControl.this, "fontSize", getFont().getSize());
        timeline            = new Timeline();

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        getStyleClass().addAll("material-field");

        final String FONT_FAMILY = getFont().getFamily();
        final int    LENGTH      = getText().length();

        promptText = new Text(getPromptText());
        promptText.getStyleClass().add("prompt-text");

        text = new HBox(promptText);
        text.getStyleClass().add("material-field");

        if (!isEditable() || isDisabled() || LENGTH > 0) {
            promptText.setFont(Font.font(FONT_FAMILY, SMALL_FONT_SIZE));
            text.setTranslateY(-STD_FONT_SIZE - TOP_OFFSET_Y);
        } else {
            promptText.setFont(Font.font(FONT_FAMILY, STD_FONT_SIZE));
        }

        getChildren().addAll(text);
    }

    private void registerListeners() {
        textProperty().addListener(o -> handleTextAndFocus(isFocused()));
        promptTextProperty().addListener(o -> promptText.setText(getPromptText()));
        focusedProperty().addListener(o -> handleTextAndFocus(isFocused()));
        promptTextColorProperty().addListener(o -> promptText.setFill(getPromptTextColor()));
        fontSize.addListener(o -> promptText.setFont(Font.font(fontSize.get())));
        timeline.setOnFinished(evt -> {
            final int LENGTH = null == getText() ? 0 : getText().length();
            if (LENGTH > 0 && text.getTranslateY() >= 0) {
                text.setTranslateY(-STD_FONT_SIZE - TOP_OFFSET_Y);
                fontSize.set(SMALL_FONT_SIZE);
            }
        });
    }


    // ******************** CSS Stylable Properties ***************************
    public Color getMaterialDesignColor() { return materialDesignColor.getValue(); }
    public void setMaterialDesignColor(final Color color) { materialDesignColor.setValue(color); }
    public ObjectProperty<Color> materialDesignColorProperty() { return (ObjectProperty<Color>) materialDesignColor; }

    public Color getPromptTextColor() { return promptTextColor.getValue(); }
    public void setPromptTextColor(final Color color) { promptTextColor.setValue(color); }
    public ObjectProperty<Color> promptTextColorProperty() { return (ObjectProperty<Color>) promptTextColor; }


    // ******************** Misc **********************************************
    private void handleTextAndFocus(final boolean isFocused) {
        final int LENGTH = null == getText() ? 0 : getText().length();

        KeyFrame kf0;
        KeyFrame kf1;

        KeyValue kvTextY0;
        KeyValue kvTextY1;
        KeyValue kvTextFontSize0;
        KeyValue kvTextFontSize1;
        KeyValue kvPromptTextFill0;
        KeyValue kvPromptTextFill1;

        if (isFocused | LENGTH > 0 || isDisabled() || !isEditable()) {
            if (Double.compare(text.getTranslateY(), -STD_FONT_SIZE - TOP_OFFSET_Y) != 0) {
                kvTextY0            = new KeyValue(text.translateYProperty(), 0);
                kvTextY1            = new KeyValue(text.translateYProperty(), -STD_FONT_SIZE - TOP_OFFSET_Y);
                kvTextFontSize0     = new KeyValue(fontSize, STD_FONT_SIZE);
                kvTextFontSize1     = new KeyValue(fontSize, SMALL_FONT_SIZE);
                kvPromptTextFill0   = new KeyValue(promptTextColorProperty(), DEFAULT_PROMPT_TEXT_COLOR);
                kvPromptTextFill1   = new KeyValue(promptTextColorProperty(), isFocused ? getMaterialDesignColor() : DEFAULT_PROMPT_TEXT_COLOR);

                kf0 = new KeyFrame(Duration.ZERO, kvTextY0, kvTextFontSize0, kvPromptTextFill0);
                kf1 = new KeyFrame(Duration.millis(ANIMATION_DURATION), kvTextY1, kvTextFontSize1, kvPromptTextFill1);

                timeline.getKeyFrames().setAll(kf0, kf1);
                timeline.play();
            }
        } else {
            if (Double.compare(text.getTranslateY(), 0) != 0) {
                kvTextY0            = new KeyValue(text.translateYProperty(), text.getTranslateY());
                kvTextY1            = new KeyValue(text.translateYProperty(), 0);
                kvTextFontSize0     = new KeyValue(fontSize, SMALL_FONT_SIZE);
                kvTextFontSize1     = new KeyValue(fontSize, STD_FONT_SIZE);
                kvPromptTextFill0   = new KeyValue(promptTextColorProperty(), getMaterialDesignColor());
                kvPromptTextFill1   = new KeyValue(promptTextColorProperty(), DEFAULT_PROMPT_TEXT_COLOR);

                kf0 = new KeyFrame(Duration.ZERO, kvTextY0, kvTextFontSize0, kvPromptTextFill0);
                kf1 = new KeyFrame(Duration.millis(ANIMATION_DURATION), kvTextY1, kvTextFontSize1, kvPromptTextFill1);

                timeline.getKeyFrames().setAll(kf0, kf1);
                timeline.play();
            }
        }
    }


    // ******************** Style related *************************************
    @Override public String getUserAgentStylesheet() {
        if (null == userAgentStyleSheet) { userAgentStyleSheet = getClass().getResource("extended.css").toExternalForm(); }
        return userAgentStyleSheet;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() { return FACTORY.getCssMetaData(); }
    @Override public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() { return FACTORY.getCssMetaData(); }
}
