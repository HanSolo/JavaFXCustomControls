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

import eu.hansolo.fx.customcontrols.controlskinbased.CustomControl.SkinType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class DemoControlSkinBased extends Application {
    private CustomControl control0;
    private CustomControl control1;


    @Override public void init() {
        control0 = new CustomControl();
        control0.setState(true);
        control0.setPrefSize(100, 100);
        control0.setColor(Color.LIME);

        control1 = new CustomControl(SkinType.SWITCH);
        control1.setState(true);
        control1.setColor(Color.web("#4bd865"));
        control1.stateProperty().addListener((o, ov, nv) -> control0.setState(nv));
    }

    @Override public void start(Stage stage) {
        VBox pane = new VBox(20, control0, control1);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane, 200, 200);
        scene.getStylesheets().add(DemoControlSkinBased.class.getResource("styles.css").toExternalForm());

        stage.setTitle("Control-Skin based Control");
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
