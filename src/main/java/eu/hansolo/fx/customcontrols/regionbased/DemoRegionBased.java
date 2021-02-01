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

package eu.hansolo.fx.customcontrols.regionbased;

import eu.hansolo.fx.customcontrols.regionbased.RegionControl.Type;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.util.function.Consumer;


/**
 * User: hansolo
 * Date: 01.02.21
 * Time: 08:18
 */
public class DemoRegionBased extends Application {
    private RegionControl redButton;
    private RegionControl yellowButton;
    private RegionControl greenButton;
    private HBox          buttonBox;


    @Override public void init() {
        redButton    = new RegionControl(Type.CLOSE);
        yellowButton = new RegionControl(Type.MINIMIZE);
        greenButton  = new RegionControl(Type.ZOOM);
        buttonBox    = new HBox(8, redButton, yellowButton, greenButton);

        registerListeners();
    }

    private void registerListeners() {
        redButton.setOnMousePressed((Consumer<MouseEvent>) e -> System.out.println("Close pressed"));
        redButton.setOnMouseReleased((Consumer<MouseEvent>) e -> System.out.println("Close released"));

        yellowButton.setOnMousePressed((Consumer<MouseEvent>) e -> System.out.println("Minimized pressed"));
        yellowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> System.out.println("Minimized released"));

        greenButton.setOnMousePressed((Consumer<MouseEvent>) e -> System.out.println("Zoom pressed"));
        greenButton.setOnMouseReleased((Consumer<MouseEvent>) e -> System.out.println("Zoom released"));

        buttonBox.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            redButton.setHovered(true);
            yellowButton.setHovered(true);
            greenButton.setHovered(true);
        });
        buttonBox.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            redButton.setHovered(false);
            yellowButton.setHovered(false);
            greenButton.setHovered(false);
        });
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(buttonBox);
        pane.setPadding(new Insets(8));

        Scene scene = new Scene(pane);

        stage.setTitle("Region based Control");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
