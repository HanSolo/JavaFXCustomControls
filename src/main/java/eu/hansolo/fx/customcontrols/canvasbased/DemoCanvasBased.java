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

 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.geometry.Insets;
 import javafx.scene.image.Image;
 import javafx.scene.layout.Background;
 import javafx.scene.layout.BackgroundFill;
 import javafx.scene.layout.CornerRadii;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Priority;
 import javafx.scene.paint.Color;
 import javafx.stage.Stage;
 import javafx.scene.layout.StackPane;
 import javafx.scene.Scene;


 /**
  * User: hansolo
  * Date: 01.02.21
  * Time: 13:38
  */
 public class DemoCanvasBased extends Application {
     private Image         dukeImg;
     private Image         heartImg;
     private CanvasControl control1;
     private CanvasControl control2;
     private CanvasControl control3;

     @Override public void init() {
         dukeImg  = new Image(DemoCanvasBased.class.getResourceAsStream("duke.png"));
         heartImg = new Image(DemoCanvasBased.class.getResourceAsStream("heart.png"));

         control1 = new CanvasControl("We");
         control1.setPrefWidth(120);

         control2 = new CanvasControl("Love", heartImg);
         control2.setBackgroundColor(Color.RED.darker());
         control2.setPrefWidth(120);

         control3 = new CanvasControl("Java", dukeImg);
         control3.setBackgroundColor(Color.web("#5382A1"));
         control3.setPrefWidth(120);

         registerListeners();
     }

     private void registerListeners() {
         control1.setOnAction(e -> System.out.println("We button pressed"));
         control2.setOnAction(e -> System.out.println("Love button pressed"));
         control3.setOnAction(e -> System.out.println("Java button pressed"));
     }

     @Override public void start(Stage stage) {
         HBox.setHgrow(control1, Priority.ALWAYS);
         HBox.setHgrow(control2, Priority.ALWAYS);
         HBox.setHgrow(control3, Priority.ALWAYS);
         HBox pane = new HBox(20, control1, control2, control3);
         pane.setPadding(new Insets(20));
         pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

         Scene scene = new Scene(pane);

         stage.setTitle("Canvas based Control");
         stage.setScene(scene);
         stage.show();
     }

     @Override public void stop() {
         Platform.exit();
         System.exit(0);
     }

     public static void main(String[] args) {
         launch(args);
     }
 }
