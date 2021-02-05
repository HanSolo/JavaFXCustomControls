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

 package eu.hansolo.fx.customcontrols.canvasbased;


 import eu.hansolo.fx.customcontrols.tools.Helper;
 import javafx.animation.AnimationTimer;
 import javafx.beans.DefaultProperty;
 import javafx.beans.property.BooleanProperty;
 import javafx.beans.property.BooleanPropertyBase;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.ObjectPropertyBase;
 import javafx.beans.property.StringProperty;
 import javafx.beans.property.StringPropertyBase;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.geometry.VPos;
 import javafx.scene.Node;
 import javafx.scene.canvas.Canvas;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.effect.BlurType;
 import javafx.scene.effect.DropShadow;
 import javafx.scene.effect.InnerShadow;
 import javafx.scene.image.Image;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.Region;
 import javafx.scene.paint.Color;
 import javafx.scene.paint.CycleMethod;
 import javafx.scene.paint.LinearGradient;
 import javafx.scene.paint.RadialGradient;
 import javafx.scene.paint.Stop;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Font;
 import javafx.scene.text.TextAlignment;

 import java.util.Random;
 import java.util.function.Consumer;


 /**
  * User: hansolo
  * Date: 01.02.21
  * Time: 13:38
  */
 @DefaultProperty("children")
 public class CanvasControl extends Region {
     private static final double                PREFERRED_WIDTH                 = 268;
     private static final double                PREFERRED_HEIGHT                = 85;
     private static final double                MINIMUM_WIDTH                   = 20;
     private static final double                MINIMUM_HEIGHT                  = 20;
     private static final double                MAXIMUM_WIDTH                   = 1024;
     private static final double                MAXIMUM_HEIGHT                  = 1024;
     private static final Color                 DEFAULT_BACKGROUND_COLOR        = Color.web("#3a609be6");
     private static final Color                 DEFAULT_HIGHLIGHT_COLOR         = Color.web("#ffffff80");
     private static final Color                 DEFAULT_FOREGROUND_COLOR        = Color.WHITESMOKE;
     private static final LinearGradient TOP_HIGHLIGHT_GRADIENT = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                                                                                                     new Stop(0.0, Color.rgb(255, 255, 255, 0.5)),
                                                                                                     new Stop(1.0, Color.rgb(255, 255, 255, 0.1)));
     private static final int                   NO_OF_PARTICLES                 = 30;
     private static final long                  UPDATE_INTERVAL                 = 100_000l;
     private        final Image                 particleImg;
     private        final double                imgOffsetX;
     private        final double                imgOffsetY;
     private              String                userAgentStyleSheet;
     private              double                aspectRatio;
     private              boolean               keepAspect;
     private              double                size;
     private              double                width;
     private              double                height;
     private              Canvas                canvas;
     private              GraphicsContext       ctx;
     private              Rectangle             clip;
     private              Pane                  pane;
     private              boolean               hovered;
     private              boolean               pressed;
     private              InnerShadow           innerShadow;
     private              DropShadow            dropShadow;
     private              StringProperty        text;
     private              ObjectProperty<Color> backgroundColor;
     private              ObjectProperty<Color> foregroundColor;
     private              BooleanProperty       active;
     private              ImageParticle[]       particles;
     private              long                  lastTimerCalled;
     private              AnimationTimer        timer;
     private              Consumer<ActionEvent> actionConsumer;

     
     // ******************** Constructors **************************************
     public CanvasControl() {
         this("", null);
     }
     public CanvasControl(final String text) {
         this(text, null);
     }
     public CanvasControl(final Image image) {
         this("", image);
     }
     public CanvasControl(final String text, final Image image) {
         if (null == image || image.getWidth() != image.getHeight()) {
             this.particleImg = new Image(CanvasControl.class.getResourceAsStream("bubble.png"));
         } else {
             this.particleImg = image;
         }
         this.imgOffsetX      = particleImg.getWidth() * (-0.5);
         this.imgOffsetY      = particleImg.getHeight() * (-0.5);
         this.aspectRatio     = PREFERRED_HEIGHT / PREFERRED_WIDTH;
         this.keepAspect      = true;
         this.hovered         = false;
         this.pressed         = false;
         this.text            = new StringPropertyBase(text) {
             @Override protected void invalidated() { redraw(); }
             @Override public Object getBean() { return CanvasControl.this; }
             @Override public String getName() { return "text"; }
         };
         this.innerShadow     = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 20, 0.0, 0, 0);
         this.dropShadow      = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.25), 5, 0.0, 0, 0);
         this.backgroundColor = new ObjectPropertyBase<>(DEFAULT_BACKGROUND_COLOR) {
             @Override protected void invalidated() { redraw(); }
             @Override public Object getBean() { return CanvasControl.this; }
             @Override public String getName() { return "backgroundColorTop"; }
         };
         this.foregroundColor = new ObjectPropertyBase<>(DEFAULT_FOREGROUND_COLOR) {
             @Override protected void invalidated() { redraw(); }
             @Override public Object getBean() { return CanvasControl.this; }
             @Override public String getName() { return "foregroundColor"; }
         };
         this.active          = new BooleanPropertyBase(false) {
             @Override protected void invalidated() {
                 if (get()) {
                     timer.start();
                 } else {
                     timer.stop();
                 }
             }
             @Override public Object getBean() { return CanvasControl.this;}
             @Override public String getName() { return "active"; }
         };
         this.particles       = new ImageParticle[NO_OF_PARTICLES];
         this.lastTimerCalled = System.nanoTime();
         this.timer           = new AnimationTimer() {
             @Override public void handle(final long now) {
                 if (now - lastTimerCalled > UPDATE_INTERVAL) {
                     redraw();
                     lastTimerCalled = now;
                 }
             }
         };
         for (int i = 0; i < NO_OF_PARTICLES; i++) {
             particles[i] = new ImageParticle(PREFERRED_WIDTH, PREFERRED_HEIGHT, particleImg);
         }
         initGraphics();
         registerListeners();
     }


     // ******************** Initialization ************************************
     private void initGraphics() {
         if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
             Double.compare(getHeight(), 0.0) <= 0) {
             if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                 setPrefSize(getPrefWidth(), getPrefHeight());
             } else {
                 setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
             }
         }

         getStyleClass().add("canvas-control");

         canvas = new Canvas(getPrefWidth(), getPrefHeight());
         canvas.setPickOnBounds(true);

         ctx = canvas.getGraphicsContext2D();
         ctx.setTextBaseline(VPos.CENTER);
         ctx.setTextAlign(TextAlignment.CENTER);

         clip = new Rectangle();
         canvas.setClip(clip);

         pane = new Pane(canvas);

         getChildren().setAll(pane);
     }

     private void registerListeners() {
         widthProperty().addListener(o -> resize());
         heightProperty().addListener(o -> resize());
         canvas.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
             hovered = true;
             setActive(true);
             redraw();
         });
         canvas.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
             hovered = false;
             redraw();
         });
         canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
             pressed = true;
             redraw();
             if (null == actionConsumer) { return; }
             actionConsumer.accept(new ActionEvent());
         });
         canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
             pressed = false;
             redraw();
         });
     }


     // ******************** Methods *******************************************
     @Override protected double computeMinWidth(final double height) { return MINIMUM_WIDTH; }
     @Override protected double computeMinHeight(final double width) { return MINIMUM_HEIGHT; }
     @Override protected double computePrefWidth(final double height) { return super.computePrefWidth(height); }
     @Override protected double computePrefHeight(final double width) { return super.computePrefHeight(width); }
     @Override protected double computeMaxWidth(final double height) { return MAXIMUM_WIDTH; }
     @Override protected double computeMaxHeight(final double width) { return MAXIMUM_HEIGHT; }

     @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

     public String getText() { return text.get(); }
     public void setText(final String text) { this.text.set(text); }
     public StringProperty textProperty() { return text; }

     public Color getBackgroundColor() { return backgroundColor.get(); }
     public void setBackgroundColor(final Color color) { backgroundColor.set(color); }
     public ObjectProperty<Color> backgroundColorProperty() { return backgroundColor; }

     public Color getForegroundColor() { return foregroundColor.get(); }
     public void setForegroundColor(final Color color) { foregroundColor.set(color); }
     public ObjectProperty<Color> foregroundColorProperty() { return foregroundColor; }

     public boolean isActive() { return active.get(); }
     public void setActive(final boolean active) { this.active.set(active); }
     public BooleanProperty activeProperty() { return active; }

     public void setOnAction(final Consumer<ActionEvent> actionConsumer)   { this.actionConsumer  = actionConsumer; }


     // ******************** Layout *******************************************
     @Override public void layoutChildren() {
         super.layoutChildren();
     }

     @Override public String getUserAgentStylesheet() {
         if (null == userAgentStyleSheet) { userAgentStyleSheet = CanvasControl.class.getResource("canvas-based.css").toExternalForm(); }
         return userAgentStyleSheet;
     }

     private void resize() {
         width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
         height = getHeight() - getInsets().getTop() - getInsets().getBottom();
         size   = width < height ? width : height;

         if (keepAspect) {
             if (aspectRatio * width > height) {
                 width = 1 / (aspectRatio / height);
             } else if (1 / (aspectRatio / height) > width) {
                 height = aspectRatio * width;
             }
         }

         if (width > 0 && height > 0) {
             pane.setMaxSize(width, height);
             pane.setPrefSize(width, height);
             pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

             clip.setX(1);
             clip.setY(1);
             clip.setWidth(width - 2);
             clip.setHeight(height - 2);
             clip.setArcWidth(height);
             clip.setArcHeight(height);

             canvas.setWidth(width);
             canvas.setHeight(height);

             innerShadow.setRadius(height * 0.25);
             dropShadow.setRadius(height * 0.01);
             dropShadow.setOffsetY(height * 0.025);

             for (ImageParticle bubble : particles) { bubble.adjustToSize(width, height); }

             redraw();
         }
     }

     private void redraw() {
         double cornerRadius          = height;
         Color  backgroundColorTop    = hovered ? getBackgroundColor().brighter() : getBackgroundColor();
         Color  backgroundColorBottom = hovered ? Color.hsb(backgroundColorTop.getHue(), backgroundColorTop.getSaturation(), Helper
             .clamp(0, 1, backgroundColorTop.getBrightness() * 1.5)).brighter() : Color.hsb(backgroundColorTop.getHue(), backgroundColorTop.getSaturation(), Helper
             .clamp(0, 1, backgroundColorTop.getBrightness() * 1.5));

         ctx.clearRect(0, 0, width, height);

         // Background
         ctx.save(); // inner shadow
         ctx.setEffect(innerShadow);
         if (pressed) {
             ctx.setFill(new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE,
                                            new Stop(0.0, Color.hsb(backgroundColorTop.getHue(), backgroundColorTop.getSaturation(), backgroundColorTop.getBrightness() * 0.7)),
                                            new Stop(1.0, Color.hsb(backgroundColorBottom.getHue(), backgroundColorBottom.getSaturation(), backgroundColorBottom.getBrightness() * 0.7))));
         } else {
             ctx.setFill(new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE,
                                            new Stop(0.0, backgroundColorTop),
                                            new Stop(1.0, backgroundColorBottom)));
         }
         ctx.fillRoundRect(1, 1, width - 2, height - 2, cornerRadius, cornerRadius);
         ctx.restore(); // shadow

         // Inner highlight
         ctx.setFill(new RadialGradient(0.0, 0.0, width * 0.5, height * 1.75, width * 0.5,false, CycleMethod.NO_CYCLE,
                                        new Stop(0.0, DEFAULT_HIGHLIGHT_COLOR),
                                        new Stop(1.0, Color.TRANSPARENT)));
         ctx.fillRoundRect((width - width * 0.85820896) * 0.5, height * 0.23529412, width * 0.85820896, height * 0.70588235, height * 0.70588235, height * 0.70588235);

         // Top highlight
         ctx.save(); // translate
         if (pressed) { ctx.translate(0, 1); }
         ctx.beginPath();
         ctx.moveTo(width * 0.825886194029851, height * 0.0588235294117647);
         ctx.bezierCurveTo(width * 0.892958955223881, height * 0.0585176470588235, width * 0.92440671641791, height * 0.277141176470588, width * 0.887195895522388, height * 0.278105882352941);
         ctx.bezierCurveTo(width * 0.886925373134328, height * 0.278117647058824, width * 0.887389925373134, height * 0.276729411764706, width * 0.500067164179104, height * 0.282352941176471);
         ctx.bezierCurveTo(width * 0.500009328358209, height * 0.282352941176471, width * 0.113149253731343, height * 0.278117647058824, width * 0.112880597014925, height * 0.278105882352941);
         ctx.bezierCurveTo(width * 0.075669776119403, height * 0.277141176470588, width * 0.107117537313433, height * 0.0585176470588235, width * 0.174190298507463, height * 0.0588235294117647);
         ctx.lineTo(width * 0.825886194029851, height * 0.0588235294117647);
         ctx.closePath();
         ctx.setFill(TOP_HIGHLIGHT_GRADIENT);
         ctx.fill();

         // Text
         ctx.save(); // text dropshadow
         ctx.setEffect(dropShadow);
         ctx.setFill(getForegroundColor());
         ctx.setFont(Font.font(height * 0.5));
         ctx.fillText(getText(), width * 0.5, height * 0.5, width * 0.9);
         ctx.restore(); // text dropshadow
         ctx.restore(); // translate

         // Particles
         if (isActive()) {
             for (int i = 0; i < NO_OF_PARTICLES; i++) {
                 ImageParticle particle = particles[i];
                 ctx.save(); // translate & scale
                 ctx.translate(particle.x, particle.y);
                 ctx.scale(particle.size, particle.size);
                 ctx.translate(imgOffsetX, imgOffsetY);
                 ctx.setGlobalAlpha(particle.opacity);
                 ctx.drawImage(particle.image, 0, 0);
                 ctx.restore(); // translate & scale

                 particle.update();
                 particle.active = hovered;
             }
         }
     }


     // ******************** Inner Classes ************************************
     class ImageParticle {
         private final Random  rnd             = new Random();
         private final double  velocityFactorX = 1.0;
         private final double  velocityFactorY = 1.0;
         private final Image   image;
         private       double  x;
         private       double  y;
         private       double  vx;
         private       double  vy;
         private       double  opacity;
         private       double  size;
         private       double  width;
         private       double  height;
         private       boolean active;


         // ******************** Constructor ***********************************
         public ImageParticle(final double width, final double height, final Image image) {
             this.width  = width;
             this.height = height;
             this.image  = image;
             this.active = true;
             init();
         }


         // ******************** Methods **************************************
         public void init() {
             // Position
             x = rnd.nextDouble() * width;
             y = height + image.getHeight();

             // Random Size
             size = (rnd.nextDouble() * 0.5) + 0.1;

             // Velocity
             vx = ((rnd.nextDouble() * 0.5) - 0.25) * velocityFactorX;
             vy = ((-(rnd.nextDouble() * 2) - 0.5) * size) * velocityFactorY;

             // Opacity
             opacity = (rnd.nextDouble() * 0.6) + 0.4;
         }

         public void adjustToSize(final double width, final double height) {
             this.width  = width;
             this.height = height;
             x = rnd.nextDouble() * width;
             y = height + image.getHeight();
         }

         public void update() {
             x += vx;
             y += vy;

             // Respawn particle if needed
             if(y < -image.getHeight()) {
                 if (active) { respawn(); }
             }
         }

         public void respawn() {
             x       = rnd.nextDouble() * width;
             y       = height + image.getHeight();
             opacity = (rnd.nextDouble() * 0.6) + 0.4;
         }
     }
 }
